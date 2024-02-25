package com.xiang;

import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class Transformer implements ClassFileTransformer {

    public byte[] transform(ClassLoader classLoader, String className, Class<?> c,
                            ProtectionDomain pd, byte[] b) throws IllegalClassFormatException {

        if (!className.equals("java/io/ObjectOutputStream$BlockDataOutputStream")) {
            return null;
        }
        CtClass ctClass;
        try {
            ctClass = ClassPool.getDefault().get("java.io.ObjectOutputStream$BlockDataOutputStream");
            ModifyWriteUTFBody(ctClass);
            ModifyGetUTFLen(ctClass);
            return ctClass.toBytecode();
        } catch (Exception ex) {
//            System.out.println(ex.getLocalizedMessage());
        }
        return null;
    }

    private void ModifyGetUTFLen(CtClass ctClass) throws CannotCompileException, NotFoundException {
        String methodName = "getUTFLength";
        CtMethod ctMethod = ctClass.getDeclaredMethod(methodName);

        String newMethodName = methodName + "$old";
        ctMethod.setName(newMethodName);

        String newMethodBody = " " +
                "long getUTFLength(String s) {" +
                "    int len = s.length();" +
                "    long utflen = 0;" +
                "    for (int off = 0; off < len; ) {" +
                "        int csize = Math.min(len - off, CHAR_BUF_SIZE);" +
                "        s.getChars(off, off + csize, cbuf, 0);" +
                "        for (int cpos = 0; cpos < csize; cpos++) {" +
                "            char c = cbuf[cpos];" +
                "            if (c >= 0x0001 && c <= 0x007F) {" +
                "                utflen += 2;" +
                "            } else if (c > 0x07FF) {" +
                "                utflen += 3;" +
                "            } else {" +
                "                utflen += 2;" +
                "            }" +
                "        }" +
                "        off += csize;" +
                "    }" +
                "    return utflen;" +
                "}";

        CtMethod newMethod = CtNewMethod.make(newMethodBody, ctClass);
        ctClass.addMethod(newMethod);
    }

    private void ModifyWriteUTFBody(CtClass ctClass) throws NotFoundException, CannotCompileException {
        String methodName = "writeUTFBody";
        CtMethod ctmethod = ctClass.getDeclaredMethod(methodName);

        String newMethodName = methodName + "$old";
        ctmethod.setName(newMethodName);

        String newMethodBody = " " +
                "private void writeUTFBody(String s) throws java.io.IOException {" +
                "    int limit = MAX_BLOCK_SIZE - 3;" +
                "    int len = s.length();" +
                "    for (int off = 0; off < len; ) {" +
                "        int csize = Math.min(len - off, CHAR_BUF_SIZE);" +
                "        s.getChars(off, off + csize, cbuf, 0);" +
                "        for (int cpos = 0; cpos < csize; cpos++) {" +
                "            char c = cbuf[cpos];" +
                "            if (pos <= limit) {" +
                "                if (c <= 0x007F && c != 0) {" +
                "                    buf[pos + 1] = (byte) (0x80 | ((c >> 0) & 0x3F));" +
                "                    buf[pos + 0] = (byte) (0xC0 | ((c >> 6) & 0x1F));" +
                "                    pos += 2;" +
                "                } else if (c > 0x07FF) {" +
                "                    buf[pos + 2] = (byte) (0x80 | ((c >> 0) & 0x3F));" +
                "                    buf[pos + 1] = (byte) (0x80 | ((c >> 6) & 0x3F));" +
                "                    buf[pos + 0] = (byte) (0xE0 | ((c >> 12) & 0x0F));" +
                "                    pos += 3;" +
                "                } else {" +
                "                    buf[pos + 1] = (byte) (0x80 | ((c >> 0) & 0x3F));" +
                "                    buf[pos + 0] = (byte) (0xC0 | ((c >> 6) & 0x1F));" +
                "                    pos += 2;" +
                "                }" +
                "            } else { " +
                "                if (c <= 0x007F && c != 0) {" +
                "                    write(0xC0 | ((c >> 6) & 0x1F));" +
                "                    write(0x80 | ((c >> 0) & 0x3F));" +
                "                } else if (c > 0x07FF) {" +
                "                    write(0xE0 | ((c >> 12) & 0x0F));" +
                "                    write(0x80 | ((c >> 6) & 0x3F));" +
                "                    write(0x80 | ((c >> 0) & 0x3F));" +
                "                } else {" +
                "                    write(0xC0 | ((c >> 6) & 0x1F));" +
                "                    write(0x80 | ((c >> 0) & 0x3F));" +
                "                }" +
                "            }" +
                "        }" +
                "        off += csize;" +
                "    }" +
                "}";

        CtMethod newMethod = CtNewMethod.make(newMethodBody, ctClass);
        ctClass.addMethod(newMethod);
    }
}

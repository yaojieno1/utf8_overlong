package com.xiang;

import java.lang.instrument.Instrumentation;

public class AgentEntry {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new Transformer(), true);
    }


    public static void premain(String agentArgs, Instrumentation inst) {
        inst.addTransformer(new Transformer(), true);
    }
}

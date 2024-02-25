# utf8_overlong
UTF8 Overlong Encoding Hacker for Ysoserial

## Introduce
`Ysoserial` 的增强插件，使用 `UTF8 Overlong Encoding` 攻击方式修改 `Ysoserial` 序列化流中的可见字符串，实现 `WAF` 绕过

## Build
```shell
mvn clean package 
```

## Usage
```shell
java -javaagent:./ufo8-1.0-SNAPSHOT.jar -jar ./ysoserial-0.0.6-SNAPSHOT-all.jar 
```

## Reference
- [探索Java反序列化绕WAF新姿势](https://t.zsxq.com/17LkqCzk8)
- [UTF-8 Overlong Encoding导致的安全问题](https://www.leavesongs.com/PENETRATION/utf-8-overlong-encoding.html)

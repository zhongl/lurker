package me.zhongl.stub;

import java.lang.instrument.Instrumentation;
import java.util.Map;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class SuperAgent {
    private final Map<String, String> conf;
    private final Instrumentation     inst;

    public SuperAgent(Map<String, String> conf, Instrumentation inst) {
        this.conf = conf;
        this.inst = inst;
    }

    public void apply() {
        System.out.println(conf + " & " + inst);
    }
}

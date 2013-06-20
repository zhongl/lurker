package me.zhongl.stub;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class InstrumentationAgent {
    private final Instrumentation inst;

    public InstrumentationAgent(Instrumentation inst) {
        this.inst = inst;
    }

    public void apply() {
        System.out.println(inst);
    }
}

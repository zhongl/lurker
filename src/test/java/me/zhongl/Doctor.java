package me.zhongl;

import java.lang.instrument.Instrumentation;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Doctor {
    private final Instrumentation inst;

    public Doctor(Instrumentation inst) {this.inst = inst;}

    public void apply() {
        System.out.println(inst);
    }
}

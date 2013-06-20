package me.zhongl;

import java.lang.instrument.Instrumentation;
import java.util.Map;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class Bootstrap {
    private final Map<String, String> conf;
    private final Doctor              doctor;

    public Bootstrap(Map<String, String> conf, Instrumentation inst) {
        this.conf = conf;
        doctor = new Doctor(inst);
    }

    public void apply() {
        System.out.println(conf);
        doctor.apply();
    }
}

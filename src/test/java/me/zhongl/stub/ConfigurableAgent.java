package me.zhongl.stub;

import java.util.Map;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class ConfigurableAgent {
    private final Map<String, String> conf;

    public ConfigurableAgent(Map<String, String> conf) {
        this.conf = conf;
    }

    public void apply() {
        System.out.println(conf);
    }
}

package me.zhongl;

import com.github.dreamhead.moco.HttpServer;
import com.github.dreamhead.moco.Runnable;
import org.junit.Test;

import java.net.URL;

import static com.github.dreamhead.moco.Moco.*;
import static com.github.dreamhead.moco.Runner.running;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public class LurkerTest {


    @Test
    public void should_get_classpath_urls() throws Exception {
        final String redirect = "/real/classpath";
        final String content = "http://localhost:12306/a.jar";

        final HttpServer server = httpserver(12306);
        server.get(by(uri("/classpath"))).redirectTo(redirect);
        server.get(by(uri(redirect))).response(content);

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                final URL[] urls = Lurker.urls("http://localhost:12306/classpath");
                assertThat(urls.length, is(1));
                assertThat(urls[0], is(new URL(content)));
            }
        });
    }

    @Test
    public void should_complain_invalid_url() throws Exception {
        final String content = "ttp://localhost:12306/a.jar";

        final HttpServer server = httpserver(12306);
        server.get(by(uri("/classpath"))).response(content);

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {

                try {
                    Lurker.urls("http://localhost:12306/classpath");
                    fail("Should complain invalid url");
                } catch (IllegalStateException ignored) { }
            }
        });
    }

    @Test
    public void should_complain_bad_request() throws Exception {
        final String classpath = "http://localhost:12306/classpath";

        final HttpServer server = httpserver(12306);

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                try {
                    Lurker.loadClass("", classpath);
                    fail("Should complain bad request");
                } catch (IllegalStateException ignored) { }
            }
        });
    }

}

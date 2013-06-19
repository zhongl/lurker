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
    public void should_parse_arguments() throws Exception {
        Lurker.parse("Bootstrap,localhost:12306,http://");
        fail();
    }

    @Test
    public void should_load_class_from_local() throws Exception {
        final String name = "me.zhongl.Bootstrap";
        final Class<?> aClass = Lurker.loadClass(name, "file:./src/test/resources/classpath");
        assertThat(aClass.getName(), is(name));
    }


    @Test
    public void should_get_classpath_urls() throws Exception {
        final String redirect = "/real/classpath";
        final String content = "http://localhost:12306/test.jar";

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
    public void should_load_class() throws Exception {
        final String content = "http://localhost:12306/jar/test.jar";

        final HttpServer server = httpserver(12306);
        server.get(by(uri("/classpath"))).response(content);
        server.get(by(uri("/jar/test.jar")))
              .response(header("Content-Type", "application/java-archive"),
                        content(pathResource("test.jar")));

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                Lurker.loadClass("me.zhongl.Bootstrap", "http://localhost:12306/classpath");
            }
        });
    }

    @Test
    public void should_complain_invalid_url() throws Exception {
        final String content = "ttp://localhost:12306/test.jar";

        final HttpServer server = httpserver(12306);
        server.get(by(uri("/classpath"))).response(content);

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {

                try {
                    Lurker.urls("http://localhost:12306/classpath");
                    fail();
                } catch (IllegalStateException ignored) { }
            }
        });
    }

    @Test
    public void should_complain_bad_request() throws Exception {

        final HttpServer server = httpserver(12306);

        running(server, new Runnable() {
            @Override
            public void run() throws Exception {
                try {
                    Lurker.loadClass("", "http://localhost:12306/classpath");
                    fail();
                } catch (IllegalStateException ignored) { }
            }
        });
    }

}

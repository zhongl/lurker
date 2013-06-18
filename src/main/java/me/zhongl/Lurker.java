package me.zhongl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public final class Lurker {
    private Lurker() {}

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        bootstrap(args, inst);
    }

    public static void premain(String args, Instrumentation inst) throws Exception {
        bootstrap(args, inst);
    }

    static void bootstrap(String args, Instrumentation inst) throws Exception {
        String name = "";
        String url = "";
        String classpath = "";

        loadClass(name, classpath).getConstructor(String.class, Instrumentation.class).newInstance(url, inst);
    }

    static Class<?> loadClass(String name, String classpath) throws ClassNotFoundException {
        try {
            return URLClassLoader.newInstance(urls(classpath)).loadClass(name);
        } catch (Exception e) {
            throw illegalStateWhenGet(classpath, e);
        }
    }

    static URL[] urls(String classpath) throws IOException {
        final InputStream input = url(classpath).openStream();

        try {
            return urls(input, classpath);
        } catch (IOException e) {
            throw illegalStateWhenGet(classpath, e);
        } finally {
            input.close();
        }
    }

    private static IllegalStateException illegalStateWhenGet(String url, Exception e) {
        return new IllegalStateException("Failed to get classpath from " + url + ", cause by " + e);
    }

    private static URL[] urls(InputStream input, String classpath) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        final ArrayList<URL> urls = new ArrayList<URL>();

        while (true) {
            final String line = reader.readLine();
            if (line == null) break;
            urls.add(url(line));
        }

        if (urls.isEmpty()) throw new IllegalStateException("Get none urls from " + classpath);

        return urls.toArray(new URL[urls.size()]);
    }

    private static URL url(String line) {
        try {
            return new URL(line);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid url [" + line + "], cause by " + e);
        }
    }
}

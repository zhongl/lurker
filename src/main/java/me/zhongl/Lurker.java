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
        String[] arguments = parse(args);
        String name = arguments[0];
        String url = arguments[1];
        String classpath = arguments[2];

        final Class<?> aClass = loadClass(name, classpath);

        try {
            final Object o = aClass.getConstructor(String.class, Instrumentation.class).newInstance(url, inst);
            aClass.getMethod("apply").invoke(o);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid bootstrap class from " + classpath, e);
        }
    }

    static String[] parse(String args) {
        return new String[0];  // TODO
    }

    static Class<?> loadClass(String name, String classpath) {
        try {
            return URLClassLoader.newInstance(urls(classpath)).loadClass(name);
        } catch (IllegalStateException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load bootstrap class from " + classpath, e);
        }
    }

    static URL[] urls(String classpath) throws IOException {
        final InputStream input = url(classpath).openStream();

        try {
            return urls(input, classpath);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to get classpath from " + classpath, e);
        } finally {
            input.close();
        }
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

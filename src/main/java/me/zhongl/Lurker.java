package me.zhongl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

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

    static void bootstrap(String classpathUrl, final Instrumentation inst) throws Exception {
        final URL url = new URL(classpathUrl);
        final Map<String, String> queryMap = map(url.getQuery());

        String name = queryMap.remove("bootstrap");
        if (name == null) throw new IllegalStateException("Missing bootstrap class name.");

        final Class<?> aClass = loadClass(name, classpathUrl);

        Object o = newInstanceBy(creators(aClass, inst, queryMap));

        try {
            aClass.getMethod("apply").invoke(o);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid bootstrap class from " + classpathUrl, e);
        }

    }

    private static Iterator<Creator> creators(final Class<?> aClass, final Instrumentation inst, final Map<String, String> conf) {
        return Arrays.asList(
                new Creator() {
                    @Override
                    public Object newInstance() throws Exception {
                        return aClass.getConstructor(Map.class, Instrumentation.class).newInstance(conf, inst);
                    }
                },
                new Creator() {

                    @Override
                    public Object newInstance() throws Exception {
                        return aClass.getConstructor(Instrumentation.class).newInstance(inst);
                    }
                },
                new Creator() {
                    @Override
                    public Object newInstance() throws Exception {
                        return aClass.getConstructor(Map.class).newInstance(conf);
                    }
                },
                new Creator() {
                    @Override
                    public Object newInstance() throws Exception {
                        return aClass.newInstance();
                    }
                }
        ).iterator();
    }

    private static Object newInstanceBy(Iterator<Creator> creators) {
        while (true) {
            try {
                return creators.next().newInstance();
            } catch (Exception e) {
                if (!creators.hasNext()) throw new IllegalStateException("Invalid bootstrap class constructor.", e);
            }
        }
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

    private static Map<String, String> map(String query) {
        Map<String, String> map = new HashMap<String, String>();
        for (String p : query.split("&")) {
            final String[] pair = p.split("=", 2);
            map.put(pair[0], pair[1]);
        }
        return map;
    }

    private static URL[] urls(InputStream input, String classpath) throws IOException {
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        final ArrayList<URL> urls = new ArrayList<URL>();

        while (true) {
            final String line = reader.readLine();
            if (line == null) break;
            urls.add(url(line));
        }

        if (urls.isEmpty()) {
            System.err.println("WARN: Get none urls from " + classpath);
            return new URL[0];
        }

        return urls.toArray(new URL[urls.size()]);
    }

    private static URL url(String line) {
        try {
            return new URL(line);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("Invalid url [" + line + "], cause by " + e);
        }
    }

    private interface Creator {
        Object newInstance() throws Exception;
    }

}

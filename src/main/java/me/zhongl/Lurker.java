package me.zhongl;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.io.*;
import java.lang.instrument.Instrumentation;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public final class Lurker {

    private Lurker() {}

    public static void main(String[] args) throws Exception {
        switch (args.length) {
            case 0:
                selectAndAttachVM();
                break;
            case 1:
                if ("-help".equals(args[0])) {
                    printUsage();
                } else {
                    loadLurkerTo(VirtualMachine.attach(args[0]));
                }
                break;
            default:
                System.out.println("Invalid arguments: " + Arrays.toString(args));
                printUsage();
                System.exit(-1);
        }
    }

    public static void agentmain(String args, Instrumentation inst) throws Exception {
        bootstrap(args, inst);
    }

    public static void premain(String args, Instrumentation inst) throws Exception {
        bootstrap(args, inst);
    }

    static void bootstrap(String classpathUrl, Instrumentation inst) throws Exception {
        final URL url = new URL(classpathUrl);

        final Map<String, String> queryMap = map(url.getQuery());

        String name = queryMap.get("bootstrap");
        String address = queryMap.containsKey("address") ? queryMap.get("address") : url.getHost() + ':' + url.getPort();

        final Class<?> aClass = loadClass(name, classpathUrl);

        try {
            final Object o = aClass.getConstructor(String.class, Instrumentation.class).newInstance(address, inst);
            aClass.getMethod("apply").invoke(o);
        } catch (Exception e) {
            throw new IllegalStateException("Invalid bootstrap class from " + classpathUrl, e);
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

    private static void selectAndAttachVM() throws Exception {
        System.out.println("Running attachable java virtual machines are:");

        final List<VirtualMachineDescriptor> vms = VirtualMachine.list();
        for (int i = 0; i < vms.size(); i++) {
            final VirtualMachineDescriptor vmd = vms.get(i);
            System.out.println(i + ": " + vmd.id() + '\t' + vmd.displayName());
        }

        System.out.print("Please select vm by index [0-" + (vms.size() - 1) + "]:");

        final char index = (char) System.in.read();
        final int i = Integer.parseInt(String.valueOf(index));
        final VirtualMachineDescriptor vmd = vms.get(i);

        System.out.println("Attaching java virtual machine: " + vmd.id());
        loadLurkerTo(VirtualMachine.attach(vmd));
    }

    private static String getVersion(JarFile jar) throws IOException {
        return jar.getManifest().getMainAttributes().getValue(Attributes.Name.SIGNATURE_VERSION);
    }

    private static JarFile thisJarFile() throws Exception {
        final CodeSource source = Lurker.class.getProtectionDomain().getCodeSource();
        return new JarFile(new File(source.getLocation().toURI()));
    }

    private static void printUsage() throws Exception {
        System.out.println("Version:" + getVersion(thisJarFile()) + "\nUsage: Lurker [-help|pid]");
    }

    private static void loadLurkerTo(VirtualMachine vm) {
        // TODO
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

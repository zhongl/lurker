package me.zhongl;

import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @author <a href="mailto:zhong.lunfu@gmail.com">zhongl<a>
 */
public final class Main {

    private Main() {}

    public static void main(String[] args) throws Exception {
        switch (args.length) {
            case 2:
                loadLurkerTo(VirtualMachine.attach(args[0]), args[1]);
                break;
            default:
                System.out.println("Invalid arguments: " + Arrays.toString(args));
                printUsage();
                System.exit(-1);
        }
    }


    private static String getVersion(JarFile jar) throws IOException {
        return jar.getManifest().getMainAttributes().getValue(Attributes.Name.SIGNATURE_VERSION);
    }

    private static JarFile thisJarFile() throws Exception {
        return new JarFile(new File(agentJarUrl().toURI()));
    }

    private static URL agentJarUrl() {
        return Main.class.getProtectionDomain().getCodeSource().getLocation();
    }

    private static void printUsage() throws Exception {
        System.out.println("Version:" + getVersion(thisJarFile()) +
                           "\nUsage: lurker <jvm pid> <url>" +
                           "\nMore information please see http://github.com/zhongl/lurker");
    }

    private static void loadLurkerTo(VirtualMachine vm, String url) throws Exception {
        vm.loadAgent(agentJarUrl().getFile(), url);
        vm.detach();
    }

}

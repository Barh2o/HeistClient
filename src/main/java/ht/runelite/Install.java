package ht.heist.runelite;

import ht.heist.HeistClient;
import ht.heist.services.hotswapper.PluginClassLoader;
import ht.heist.heistclient.Main;
import ht.heist.Static;
import ht.heist.model.Guice;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

public class Install {
    /**
     * Don't remove, call is injected see @{PluginManagerMixin::loadCorePlugins}
     *
     * @param original list of plugin classes to load
     */
    public void injectBuiltInPlugins(List<Class<?>> original) {
        try {
            File builtIns = loadBuildIns().toFile();
            PluginClassLoader classLoader = new PluginClassLoader(builtIns, ht.heist.heistclient.Main.CLASSLOADER);
            original.addAll(classLoader.getPluginClasses());

            // Explicitly add our new plugins using the plugin classloader
            tryToAddPlugin(original, "ht.heist.plugins.HeistHUDPlugin", classLoader);
            tryToAddPlugin(original, "ht.heist.plugins.questing.CooksAssistantPlugin", classLoader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryToAddPlugin(List<Class<?>> original, String className, ClassLoader classLoader) {
        try {
            Class<?> clazz = Class.forName(className, true, classLoader);
            if (!original.contains(clazz)) {
                original.add(clazz);
                ht.heist.Logger.info("Successfully injected plugin: " + className);
                System.out.println("[Heist] Successfully injected plugin: " + className);
            }
        } catch (ClassNotFoundException e) {
            ht.heist.Logger.warn("Could not find plugin for injection: " + className);
            System.err.println("[Heist] Could not find plugin: " + className);
        } catch (Exception e) {
            ht.heist.Logger.error("Error during plugin injection: " + className, e);
            e.printStackTrace();
        }
    }

    public static void install() {
        Guice injector = Static.getRuneLite().getInjector();
        Static.set(injector.getBinding("net.runelite.api.Client"), "RL_CLIENT");
    }

    private Path loadBuildIns() {
        File tempJar = getBuiltIns();
        if (tempJar == null) {
            System.err.println("Failed to load built-in plugins.");
            System.exit(1);
        }
        return tempJar.toPath();
    }

    private static File getBuiltIns() {
        String resource = "plugins.jarData";
        try {
            File tempJar = File.createTempFile(resource, ".jar");
            tempJar.deleteOnExit();

            try (InputStream jarStream = HeistClient.class.getResourceAsStream(resource);
                    FileOutputStream fos = new FileOutputStream(tempJar)) {

                if (jarStream == null) {
                    System.err.println("Could not find embedded " + resource + " in resources");
                    return null;
                }

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = jarStream.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            return tempJar;

        } catch (Exception e) {
            System.err.println("Failed to load embedded JAR: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}

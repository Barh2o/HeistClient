package ht.heist.heistclient;

import ht.heist.Logger;
import ht.heist.Static;
import ht.heist.logging.LogFileManager;
import ht.heist.HeistClientOptions;
import ht.heist.bootstrap.RLUpdater;
import ht.heist.classloader.RLClassLoader;
import ht.heist.util.MappingProvider;
import ht.heist.util.asm.SignerMapper;
import ht.heist.runelite.Install;
import ht.heist.runelite.jvm.JvmParams;
import ht.heist.injector.Injector;
import ht.heist.injector.RLInjector;
import ht.heist.patch.PatchGenerator;
import ht.heist.patch.PatchApplier;
import ht.heist.model.Libs;
import ht.heist.services.AutoLogin;
import ht.heist.services.CatFacts;
import ht.heist.services.WorldSetter;
import ht.heist.services.proxy.ProxyManager;
import ht.heist.util.LauncherCom;

import javax.swing.*;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static ht.heist.heistclient.Versioning.isRunningFromShadedJar;

public class Main {
    public static final Path REPOSITORY_DIR = Path.of(Static.VITA_DIR.toString(), "repository2");
    private static URL[] URLS = null;
    public static Libs LIBS;
    public static RLClassLoader CLASSLOADER;
    public static RLClassLoader CTX_CLASSLOADER;

    public static void main(String[] args) throws Exception
    {
        LogFileManager.initialize();
        HeistClientOptions optionsParser = Static.getCliArgs();
        args = optionsParser.parse(args);
        optionsParser._checkAudio();
        if(!optionsParser.isSafeLaunch())
        {
            System.err.println("Safe launch not satisfied, HeistClient will not start.");
            System.exit(0);
        }
        if(optionsParser.getProxy() != null)
        {
            ProxyManager.process(optionsParser.getProxy());
        }
        if(optionsParser.getLegacyLogin() != null)
        {
            AutoLogin.setCredentials(optionsParser.getLegacyLogin());
        }
        if(optionsParser.getJagexLogin() != null)
        {
            AutoLogin.setCredentials(optionsParser.getJagexLogin());
        }
        if(optionsParser.getTargetBootstrap() != null)
        {
            System.setProperty("forced.runelite.version", optionsParser.getTargetBootstrap());
        }
        if(optionsParser.getWorld() > 0)
        {
            WorldSetter.setWorld(optionsParser.getWorld());
        }
        Files.createDirectories(REPOSITORY_DIR);
        JvmParams.set();
        RLUpdater.run();
        loadArtifacts();
        SignerMapper.map();
        loadClassLoader();

        if(optionsParser.isRunInjector())
        {
            // IDE/Dev mode: Run full ASM injection pipeline and generate patches
            PatchGenerator.enableCapture();
            Injector.patch();
            RLInjector.patch();
            try {
                String resourcesPath = "src/main/resources";
                PatchGenerator.writePatchesZip(resourcesPath);
                System.out.println("[Main] Patch generation complete: " + PatchGenerator.getStatistics());
            } catch (Exception e) {
                System.err.println("[Main] Failed to write patches.zip: " + e.getMessage());
                e.printStackTrace();
            }
        }
        else
        {
            PatchApplier.applyPatches();
        }

        MappingProvider.getMappings().clear();
        if(optionsParser.getPort() != null)
        {
            LauncherCom.sendReadySignal(Integer.parseInt(optionsParser.getPort()), "Done");
        }
        CLASSLOADER.launch(args);
        Install.install();
        Logger.norm("HeistClient started. - Did you know... " + CatFacts.get(-1));
    }

    private static void loadArtifacts()
    {
        try
        {
            File[] jarfiles = REPOSITORY_DIR.toFile().listFiles(f ->
                    f.getName().endsWith(".jar") &&
                            !f.getName().contains("guice") &&
                            !f.getName().contains("javax") &&
                            !f.getName().contains("guava") &&
                            !f.getName().contains("logback-core") &&
                            !f.getName().contains("logback-classic") &&
                            !f.getName().contains("slf4j-api")
            );
            if(jarfiles == null)
                throw new Exception();
            URLS = new URL[jarfiles.length];
            for (int i = 0; i < jarfiles.length; i++)
            {
                URLS[i] = jarfiles[i].toURI().toURL();
            }

            LIBS = new Libs(URLS);
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    private static void loadClassLoader() {
        CLASSLOADER = new RLClassLoader(URLS);
        CTX_CLASSLOADER = new RLClassLoader(URLS);
        if(!isRunningFromShadedJar())
            UIManager.put("ClassLoader", CLASSLOADER);
        Thread.currentThread().setContextClassLoader(CLASSLOADER);
        Static.set(CLASSLOADER, "CLASSLOADER");
    }

    public static boolean isMinMode() {
        HeistClientOptions optionsParser = Static.getCliArgs();
        return optionsParser.isNoPlugins() || optionsParser.isMin();
    }
}

package ht.heist;

import ht.heist.heistclient.Main;
import ht.heist.heistclient.SelfUpdate;
import ht.heist.heistclient.Versioning;
import java.io.File;
import java.net.URISyntaxException;
import static ht.heist.util.JVMLauncher.launchInNewJVM;
import static ht.heist.heistclient.Versioning.getLiveRuneliteVersion;
import static ht.heist.heistclient.Versioning.getHeistClientVersion;

public class HeistClient {
    public static void main(String[] args) {
        try {
            if (isSafeLaunch(args)) {
                Main.main(args);
                return;
            }
            String currentVersion = getHeistClientVersion();
            if (Versioning.isRunningFromShadedJar()) {
                String liveRlVersion = getLiveRuneliteVersion();
                if (!currentVersion.startsWith(liveRlVersion)) {
                    System.out.println("HeistClient version " + currentVersion + " is out of date. Latest version is "
                            + liveRlVersion + ".");
                    Static.getCliArgs().parse(args);
                    new SelfUpdate().checkAndUpdate();
                    System.err.println("Warning: You are running HeistClient version " + currentVersion
                            + " but the latest version is " + liveRlVersion + ". Please update to the latest version.");
                    return;
                }
                String latestVitaRelease = Versioning.getLatestHeistClientReleaseTag();
                if (latestVitaRelease != null && !currentVersion.equals(latestVitaRelease)) {
                    System.out.println("HeistClient version " + currentVersion + " is out of date. Latest version is "
                            + latestVitaRelease + ".");
                    Static.getCliArgs().parse(args);
                    SelfUpdate.showUpdateAvailableDialog(currentVersion, latestVitaRelease, true);
                    System.err.println("Warning: You are running HeistClient version " + currentVersion
                            + " but the latest version is " + latestVitaRelease
                            + ". Please update to the latest version.");
                    return;
                }
            }
            System.out.println("HeistClient version " + currentVersion + " is up to date.");
            String[] newArgs = new String[args.length + 1];
            newArgs[0] = "-safeLaunch";
            System.arraycopy(args, 0, newArgs, 1, args.length);
            launchInNewJVM("ht.heist.heistclient.Main", buildFullClasspath(), newArgs);
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String buildFullClasspath() throws URISyntaxException {
        String currentClasspath = System.getProperty("java.class.path");
        File sourceLocation = new File(HeistClient.class.getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI());
        String myLocation = sourceLocation.getAbsolutePath();
        if (!currentClasspath.contains(myLocation)) {
            return myLocation + File.pathSeparator + currentClasspath;
        }
        return currentClasspath;
    }

    private static boolean isSafeLaunch(String[] args) {
        for (String arg : args) {
            if (arg.equals("-safeLaunch")) {
                return true;
            }
        }
        return false;
    }
}

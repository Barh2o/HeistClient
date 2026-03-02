import java.util.*;
import java.util.stream.*;
import static java.lang.System.out;
import ht.heist.Static;
import com.google.inject.Injector;

// RuneLite API classes (should be available from JARs)
import net.runelite.api.*;
import net.runelite.api.coords.*;
import net.runelite.api.events.*;
import net.runelite.api.widgets.*;
import ht.heist.data.wrappers.*;
import ht.heist.api.game.sailing.*;
import ht.heist.data.locatables.sailing.*;

import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;

import net.runelite.api.gameval.*;
import net.runelite.client.events.*;
import net.runelite.client.game.*;

import ht.heist.*;
import ht.heist.services.*;
import ht.heist.services.pathfinder.*;
import ht.heist.api.*;
import ht.heist.api.entities.*;
import ht.heist.api.widgets.*;
import ht.heist.api.game.*;
import ht.heist.api.threaded.*;
import ht.heist.queries.*;
import ht.heist.util.*;
import ht.heist.data.*;

import ht.heist.util.handler.*;
import ht.heist.util.handler.script.*;
import ht.heist.api.handlers.*;
import net.runelite.client.eventbus.*;

// HeistClient classes (should be available since they're compiled)
import ht.heist.services.GameManager;

public class %CLASS_NAME% {
    private static ClassLoader getContextClassLoader() {
        // Use the thread's context classloader - should be the RLClassLoader when running inside HeistClient
        return Thread.currentThread().getContextClassLoader();
    }

    @SuppressWarnings("unchecked")
    private static <T> T inject(String className) {
        try {
            // Get injector from Static class directly
            Injector injector = Static.getInjector();

            // Load the target class
            Class<?> targetClass = getContextClassLoader().loadClass(className);

            // Get instance from injector using proper Guice API
            return (T) injector.getInstance(targetClass);
        } catch (ClassNotFoundException e) {
            System.err.println("Class not found: " + className);
            System.err.println("Available classloader: " + getContextClassLoader().getClass().getName());
            System.err.println("Classloader toString: " + getContextClassLoader().toString());
            return null;
        } catch (Exception e) {
            System.err.println("Failed to inject " + className + ": " + e.getClass().getSimpleName() + " - " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> T loadClass(String className) {
        try {
            return (T) getContextClassLoader().loadClass(className);
        } catch (Exception e) {
            System.err.println("Failed to load class " + className + ": " + e.getMessage());
            return null;
        }
    }

    public void run() {
        %USER_CODE%
    }
}

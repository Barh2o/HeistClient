package ht.heist.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;

/** Finds the real Task class: Runnable with Object fields */
public class JarInspector {
    public static void main(String[] args) throws Exception {
        String userHome = System.getProperty("user.home");
        Path jarPath = Paths.get(userHome, ".runelite", "HeistClient", "repository2",
                "injected-client-1.12.18.jar");

        Map<String, ClassNode> classes = new LinkedHashMap<>();
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (!entry.getName().endsWith(".class"))
                    continue;
                try (InputStream is = jar.getInputStream(entry)) {
                    ClassReader cr = new ClassReader(is);
                    ClassNode cn = new ClassNode();
                    cr.accept(cn, ClassReader.SKIP_FRAMES | ClassReader.SKIP_DEBUG);
                    classes.put(cn.name, cn);
                }
            }
        }

        System.out.println("=== Runnable classes with java/lang/Object fields ===");
        for (ClassNode cn : classes.values()) {
            if (cn.interfaces == null || !cn.interfaces.contains("java/lang/Runnable"))
                continue;
            long objFields = cn.fields.stream().filter(f -> f.desc.equals("Ljava/lang/Object;")).count();
            if (objFields >= 1) {
                System.out.printf("CLASS %-12s super=%-20s fields=%d objFields=%d%n",
                        cn.name, cn.superName, cn.fields.size(), objFields);
                for (FieldNode fn : cn.fields) {
                    System.out.printf("  FIELD %-20s desc=%-30s static=%b%n", fn.name, fn.desc,
                            (fn.access & 0x0008) != 0);
                }
            }
        }

        System.out.println("\n=== ev subclasses with small fields (PacketBufferNode candidates) ===");
        for (ClassNode cn : classes.values()) {
            if (!"ev".equals(cn.superName))
                continue;
            System.out.printf("CLASS %-12s fields=%d%n", cn.name, cn.fields.size());
            for (FieldNode fn : cn.fields) {
                System.out.printf("  FIELD %-20s desc=%-30s static=%b%n", fn.name, fn.desc, (fn.access & 0x0008) != 0);
            }
        }
    }
}

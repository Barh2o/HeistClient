package ht.heist.util;

import com.google.gson.GsonBuilder;
import ht.heist.util.dto.JClass;
import ht.heist.util.dto.JField;
import ht.heist.util.dto.JMethod;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Scans the injected-client JAR to produce injector/mappings.json.
 *
 * The injected-client JAR has classes with obfuscated 2-letter names (e.g.
 * "xh") but
 * many of them implement deobfuscated RuneLite API interfaces (e.g.
 * net/runelite/api/PacketBuffer).
 * We use these interfaces + structural fingerprints to produce the mapping
 * file.
 */
public class MappingGenerator {

    public static void main(String[] args) throws Exception {
        String userHome = System.getProperty("user.home");
        Path jarPath = Paths.get(userHome, ".runelite", "HeistClient", "repository2",
                "injected-client-1.12.18.jar");
        if (!Files.exists(jarPath)) {
            jarPath = Paths.get(userHome, ".runelite", "repository2",
                    "injected-client-1.12.18.jar");
        }
        if (!Files.exists(jarPath)) {
            throw new FileNotFoundException("injected-client jar not found");
        }

        System.out.println("[MappingGenerator] Loading JAR: " + jarPath);
        Map<String, ClassNode> classes = loadJar(jarPath);
        System.out.println("[MappingGenerator] Loaded " + classes.size() + " classes");

        List<JClass> mappings = new ArrayList<>();
        identifyClient(classes, mappings);
        identifyPacketBuffer(classes, mappings);
        identifyBuffer(classes, mappings);
        identifyIsaacCipher(classes, mappings);
        identifyPacketBufferNode(classes, mappings);
        identifyClientPacket(classes, mappings);
        identifyServerPacket(classes, mappings);
        identifyPacketWriter(classes, mappings);
        identifyGameEngine(classes, mappings);
        identifyTask(classes, mappings);
        identifyMouseHandler(classes, mappings);
        identifyAccountType(classes, mappings);
        identifyObjectComposition(classes, mappings);
        identifyItemComposition(classes, mappings);
        identifyLanguage(classes, mappings);
        identifyLogin(classes, mappings);
        identifyLoginHash(classes, mappings);
        identifyJagexAccounts(classes, mappings);
        identifyRandomDat(classes, mappings);

        System.out.println("[MappingGenerator] Identified " + mappings.size() + " classes");

        // MappingProvider uses
        // HeistClient.class.getResourceAsStream("injector/mappings.json")
        // which resolves relative to HeistClient's package (ht/heist/), so the file
        // must be at ht/heist/injector/mappings.json in the classpath.
        String outputDir = "src/main/resources/ht/heist/injector";
        Files.createDirectories(Paths.get(outputDir));
        Path outputPath = Paths.get(outputDir, "mappings.json");
        String json = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create()
                .toJson(mappings);
        Files.writeString(outputPath, json);
        System.out.println("[MappingGenerator] Written to " + outputPath.toAbsolutePath());

        for (JClass c : mappings) {
            System.out.printf("  %-25s -> %-30s (%d fields, %d methods)%n",
                    c.getName(), c.getObfuscatedName(), c.getFields().size(), c.getMethods().size());
        }
    }

    // ---------------------------------------------------------------
    // JAR loader
    // ---------------------------------------------------------------
    private static Map<String, ClassNode> loadJar(Path jarPath) throws IOException {
        Map<String, ClassNode> result = new LinkedHashMap<>();
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
                    result.put(cn.name, cn);
                }
            }
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Builder helpers
    // ---------------------------------------------------------------
    private static JClass build(String mappedName, ClassNode cn) {
        JClass jc = new JClass();
        jc.setName(mappedName);
        jc.setObfuscatedName(cn.name);
        return jc;
    }

    private static String fd(String obfName) {
        return "L" + obfName + ";";
    }

    private static void addFieldByDesc(JClass jc, ClassNode cn, String mappedName, String descriptor) {
        for (FieldNode fn : cn.fields) {
            if (fn.desc.equals(descriptor)) {
                JField f = new JField();
                f.setName(mappedName);
                f.setObfuscatedName(fn.name);
                f.setOwner(jc.getName());
                f.setOwnerObfuscatedName(jc.getObfuscatedName());
                f.setDescriptor(fn.desc);
                f.setStatic((fn.access & 0x0008) != 0);
                jc.getFields().add(f);
                return;
            }
        }
        System.err.println("[MappingGenerator] WARN: field '" + mappedName + "' desc='" + descriptor + "' not found in "
                + cn.name);
    }

    private static void addFieldByType(JClass jc, ClassNode cn, String mappedName, String typeDesc, boolean isStatic) {
        for (FieldNode fn : cn.fields) {
            if (fn.desc.equals(typeDesc) && (((fn.access & 0x0008) != 0) == isStatic)) {
                JField f = new JField();
                f.setName(mappedName);
                f.setObfuscatedName(fn.name);
                f.setOwner(jc.getName());
                f.setOwnerObfuscatedName(jc.getObfuscatedName());
                f.setDescriptor(fn.desc);
                f.setStatic(isStatic);
                jc.getFields().add(f);
                return;
            }
        }
    }

    private static void addStaticStringField(JClass jc, ClassNode cn, String mappedName, int index) {
        List<FieldNode> sFields = new ArrayList<>();
        for (FieldNode fn : cn.fields)
            if (fn.desc.equals("Ljava/lang/String;") && (fn.access & 0x0008) != 0)
                sFields.add(fn);
        if (index < sFields.size()) {
            JField f = new JField();
            f.setName(mappedName);
            f.setObfuscatedName(sFields.get(index).name);
            f.setOwner(jc.getName());
            f.setOwnerObfuscatedName(jc.getObfuscatedName());
            f.setDescriptor("Ljava/lang/String;");
            f.setStatic(true);
            jc.getFields().add(f);
        }
    }

    private static void addStaticIntField(JClass jc, ClassNode cn, String mappedName, int index) {
        List<FieldNode> iFields = new ArrayList<>();
        for (FieldNode fn : cn.fields)
            if (fn.desc.equals("I") && (fn.access & 0x0008) != 0)
                iFields.add(fn);
        if (index < iFields.size()) {
            JField f = new JField();
            f.setName(mappedName);
            f.setObfuscatedName(iFields.get(index).name);
            f.setOwner(jc.getName());
            f.setOwnerObfuscatedName(jc.getObfuscatedName());
            f.setDescriptor("I");
            f.setStatic(true);
            jc.getFields().add(f);
        }
    }

    private static void addGlobalFieldByDesc(Map<String, ClassNode> classes, JClass jc, String mappedName,
            String descriptor) {
        for (ClassNode cn : classes.values()) {
            for (FieldNode fn : cn.fields) {
                if ((fn.access & 0x0008) != 0 && fn.desc.equals(descriptor)) {
                    JField f = new JField();
                    f.setName(mappedName);
                    f.setObfuscatedName(fn.name);
                    f.setOwner(jc.getName());
                    f.setOwnerObfuscatedName(cn.name);
                    f.setDescriptor(fn.desc);
                    f.setStatic(true);
                    jc.getFields().add(f);
                    return;
                }
            }
        }
        System.err.println(
                "[MappingGenerator] WARN: global field '" + mappedName + "' desc='" + descriptor + "' not found");
    }

    // ---------------------------------------------------------------
    // Find helpers (return obfuscated class name)
    // ---------------------------------------------------------------
    private static String findByInterface(Map<String, ClassNode> classes, String iface) {
        for (ClassNode cn : classes.values())
            if (cn.interfaces != null && cn.interfaces.contains(iface))
                return cn.name;
        return null;
    }

    private static String findClientObfName(Map<String, ClassNode> classes) {
        return findByInterface(classes, "net/runelite/api/Client");
    }

    private static String findPacketBufferObfName(Map<String, ClassNode> classes) {
        return findByInterface(classes, "net/runelite/api/PacketBuffer");
    }

    private static String findGameEngineObfName(Map<String, ClassNode> classes) {
        return findByInterface(classes, "net/runelite/api/GameEngine");
    }

    private static String findBufferObfName(Map<String, ClassNode> classes) {
        // Buffer is the superclass of PacketBuffer
        String pbObf = findPacketBufferObfName(classes);
        if (pbObf == null)
            return null;
        ClassNode pb = classes.get(pbObf);
        return pb != null ? pb.superName : null;
    }

    private static String findClientPacketObfName(Map<String, ClassNode> classes) {
        // ClientPacket: implements net/runelite/api/ClientPacket, or is a tiny class
        // with 2 int fields
        String byIface = findByInterface(classes, "net/runelite/api/ClientPacket");
        if (byIface != null)
            return byIface;
        // Fallback: tiny class with exactly 2 int fields
        for (ClassNode cn : classes.values()) {
            if (!cn.superName.equals("java/lang/Object"))
                continue;
            if (cn.interfaces != null && !cn.interfaces.isEmpty())
                continue;
            long intFields = cn.fields.stream().filter(f -> f.desc.equals("I")).count();
            if (intFields == 2 && cn.fields.size() == 2 && cn.methods.size() <= 3)
                return cn.name;
        }
        return null;
    }

    private static String findServerPacketObfName(Map<String, ClassNode> classes) {
        String byIface = findByInterface(classes, "net/runelite/api/ServerPacket");
        if (byIface != null)
            return byIface;
        String cpObf = findClientPacketObfName(classes);
        // Another tiny class with 2 int fields that is not ClientPacket
        for (ClassNode cn : classes.values()) {
            if (cn.name.equals(cpObf))
                continue;
            if (!cn.superName.equals("java/lang/Object"))
                continue;
            if (cn.interfaces != null && !cn.interfaces.isEmpty())
                continue;
            long intFields = cn.fields.stream().filter(f -> f.desc.equals("I")).count();
            if (intFields == 2 && cn.fields.size() == 2 && cn.methods.size() <= 3)
                return cn.name;
        }
        return null;
    }

    private static String findPacketBufferNodeObfName(Map<String, ClassNode> classes) {
        String cpObf = findClientPacketObfName(classes);
        String pbObf = findPacketBufferObfName(classes);
        String bufObf = findBufferObfName(classes);
        if (cpObf == null)
            return null;
        for (ClassNode cn : classes.values()) {
            boolean hasCp = cn.fields.stream().anyMatch(f -> f.desc.equals(fd(cpObf)));
            if (!hasCp)
                continue;
            // Also check for PacketBuffer or Buffer type field alongside ClientPacket
            boolean hasPb = (pbObf != null && cn.fields.stream().anyMatch(f -> f.desc.equals(fd(pbObf))))
                    || (bufObf != null && cn.fields.stream().anyMatch(f -> f.desc.equals(fd(bufObf))));
            if (hasPb)
                return cn.name;
        }
        // Final fallback: any small class (<=15 fields) with a ClientPacket field that
        // is not Client
        String clientObnFallback = findClientObfName(classes);
        for (ClassNode cn : classes.values()) {
            if (cn.name.equals(clientObnFallback))
                continue; // Client has many fields
            boolean hasCp = cn.fields.stream().anyMatch(f -> f.desc.equals(fd(cpObf)));
            if (hasCp && cn.fields.size() <= 15)
                return cn.name;
        }
        return null;
    }

    private static String findIsaacCipherObfName(Map<String, ClassNode> classes) {
        String pbObf = findPacketBufferObfName(classes);
        for (ClassNode cn : classes.values()) {
            if (pbObf != null && cn.name.equals(pbObf))
                continue;
            if (!cn.superName.equals("java/lang/Object"))
                continue;
            if (cn.interfaces != null && !cn.interfaces.isEmpty())
                continue;
            // Must have int[] field
            boolean hasIntArray = cn.fields.stream().anyMatch(f -> f.desc.equals("[I"));
            if (!hasIntArray)
                continue;
            // Must have a method that returns an int (nextInt equivalent) — instance method
            boolean hasNextInt = cn.methods.stream()
                    .anyMatch(m -> m.desc.equals("()I") && !m.name.equals("<init>") && (m.access & 0x0008) == 0);
            if (hasNextInt)
                return cn.name;
        }
        return null;
    }

    // ---------------------------------------------------------------
    // Class identifiers
    // ---------------------------------------------------------------

    private static void identifyClient(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findClientObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: Client not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("Client", cn);
        System.out.println("[MappingGenerator] Client -> " + cn.name);

        // Static fields
        String pwObf = findPacketWriterObfName(classes);
        String mhObf = findMouseHandlerObfName(classes);
        if (pwObf != null)
            addGlobalFieldByDesc(classes, jc, "packetWriter", fd(pwObf));
        if (mhObf != null)
            addGlobalFieldByDesc(classes, jc, "MouseHandler_instance", fd(mhObf));
        // mouseLastPressedTimeMillis — static long on Client
        addFieldByType(jc, cn, "mouseLastPressedTimeMillis", "J", true);
        // heading — static int
        addFieldByType(jc, cn, "heading", "I", true);

        // getPacketBufferNode — method that takes (ClientPacket, IsaacCipher) ->
        // PacketBufferNode
        String cpObf = findClientPacketObfName(classes);
        String icObf = findIsaacCipherObfName(classes);
        String pbnObf = findPacketBufferNodeObfName(classes);
        if (cpObf != null && icObf != null && pbnObf != null) {
            for (MethodNode mn : cn.methods) {
                if (mn.desc.contains(cpObf) && mn.desc.contains(icObf) && mn.desc.endsWith(fd(pbnObf) + ")")) {
                    // actually the return is pbn
                }
                if (mn.desc.startsWith("(L" + cpObf + ";L" + icObf)) {
                    JMethod m = new JMethod();
                    m.setName("getPacketBufferNode");
                    m.setObfuscatedName(mn.name);
                    m.setOwner("Client");
                    m.setOwnerObfuscatedName(cn.name);
                    m.setDescriptor(mn.desc);
                    jc.getMethods().add(m);
                    break;
                }
            }
        }

        out.add(jc);
    }

    private static String findPacketWriterObfName(Map<String, ClassNode> classes) {
        String isaacObf = findIsaacCipherObfName(classes);
        if (isaacObf == null)
            return null;
        // PacketWriter has an INSTANCE field of type IsaacCipher (dy)
        for (ClassNode cn : classes.values()) {
            boolean hasIsaacInstance = cn.fields.stream()
                    .anyMatch(f -> f.desc.equals(fd(isaacObf)) && (f.access & 0x0008) == 0);
            if (hasIsaacInstance)
                return cn.name;
        }
        return null;
    }

    private static String findMouseHandlerObfName(Map<String, ClassNode> classes) {
        for (ClassNode cn : classes.values()) {
            if (cn.interfaces == null)
                continue;
            boolean isMouseListener = cn.interfaces.stream().anyMatch(i -> i.contains("Mouse"));
            if (!isMouseListener)
                continue;
            boolean hasLong = cn.fields.stream().anyMatch(f -> f.desc.equals("J"));
            long intCount = cn.fields.stream().filter(f -> f.desc.equals("I")).count();
            if (hasLong && intCount >= 3)
                return cn.name;
        }
        return null;
    }

    private static void identifyPacketBuffer(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findPacketBufferObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: PacketBuffer not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("PacketBuffer", cn);
        System.out.println("[MappingGenerator] PacketBuffer -> " + cn.name);
        out.add(jc);
    }

    private static void identifyBuffer(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findBufferObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: Buffer not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("Buffer", cn);
        System.out.println("[MappingGenerator] Buffer -> " + cn.name);
        // array field: first [B or [I
        cn.fields.stream().filter(f -> f.desc.equals("[B") || f.desc.equals("[I")).findFirst().ifPresent(fn -> {
            JField f = new JField();
            f.setName("array");
            f.setObfuscatedName(fn.name);
            f.setOwner("Buffer");
            f.setOwnerObfuscatedName(cn.name);
            f.setDescriptor(fn.desc);
            jc.getFields().add(f);
        });
        // offset field: first I
        cn.fields.stream().filter(f -> f.desc.equals("I")).findFirst().ifPresent(fn -> {
            JField f = new JField();
            f.setName("offset");
            f.setObfuscatedName(fn.name);
            f.setOwner("Buffer");
            f.setOwnerObfuscatedName(cn.name);
            f.setDescriptor("I");
            jc.getFields().add(f);
        });
        out.add(jc);
    }

    private static void identifyIsaacCipher(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findIsaacCipherObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: IsaacCipher not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("IsaacCipher", cn);
        System.out.println("[MappingGenerator] IsaacCipher -> " + cn.name);
        out.add(jc);
    }

    private static void identifyPacketBufferNode(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findPacketBufferNodeObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: PacketBufferNode not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("PacketBufferNode", cn);
        System.out.println("[MappingGenerator] PacketBufferNode -> " + cn.name);
        String cpObf = findClientPacketObfName(classes);
        String pbObf = findPacketBufferObfName(classes);
        if (cpObf != null)
            addFieldByDesc(jc, cn, "clientPacket", fd(cpObf));
        if (pbObf != null)
            addFieldByDesc(jc, cn, "packetBuffer", fd(pbObf));
        out.add(jc);
    }

    private static void identifyClientPacket(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findClientPacketObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: ClientPacket not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("ClientPacket", cn);
        System.out.println("[MappingGenerator] ClientPacket -> " + cn.name);
        List<FieldNode> ints = new ArrayList<>();
        for (FieldNode fn : cn.fields)
            if (fn.desc.equals("I"))
                ints.add(fn);
        if (ints.size() >= 2) {
            JField id = new JField();
            id.setName("id");
            id.setObfuscatedName(ints.get(0).name);
            id.setOwner("ClientPacket");
            id.setOwnerObfuscatedName(cn.name);
            id.setDescriptor("I");
            jc.getFields().add(id);
            JField len = new JField();
            len.setName("length");
            len.setObfuscatedName(ints.get(1).name);
            len.setOwner("ClientPacket");
            len.setOwnerObfuscatedName(cn.name);
            len.setDescriptor("I");
            jc.getFields().add(len);
        }
        out.add(jc);
    }

    private static void identifyServerPacket(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findServerPacketObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: ServerPacket not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("ServerPacket", cn);
        System.out.println("[MappingGenerator] ServerPacket -> " + cn.name);
        List<FieldNode> ints = new ArrayList<>();
        for (FieldNode fn : cn.fields)
            if (fn.desc.equals("I"))
                ints.add(fn);
        if (ints.size() >= 2) {
            JField id = new JField();
            id.setName("id");
            id.setObfuscatedName(ints.get(0).name);
            id.setOwner("ServerPacket");
            id.setOwnerObfuscatedName(cn.name);
            id.setDescriptor("I");
            jc.getFields().add(id);
            JField len = new JField();
            len.setName("length");
            len.setObfuscatedName(ints.get(1).name);
            len.setOwner("ServerPacket");
            len.setOwnerObfuscatedName(cn.name);
            len.setDescriptor("I");
            jc.getFields().add(len);
        }
        out.add(jc);
    }

    private static void identifyPacketWriter(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findPacketWriterObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: PacketWriter not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("PacketWriter", cn);
        System.out.println("[MappingGenerator] PacketWriter -> " + cn.name);

        String isaacObf = findIsaacCipherObfName(classes);
        String spObf = findServerPacketObfName(classes);
        String pbObf = findPacketBufferObfName(classes);
        String pbnObf = findPacketBufferNodeObfName(classes);

        if (isaacObf != null)
            addFieldByDesc(jc, cn, "isaacCipher", fd(isaacObf));
        if (spObf != null)
            addFieldByDesc(jc, cn, "serverPacket", fd(spObf));
        addFieldByType(jc, cn, "serverPacketLength", "I", false);
        if (pbObf != null)
            addFieldByDesc(jc, cn, "serverPacketBuffer", fd(pbObf));

        // addNode: void method that takes a PacketBufferNode
        if (pbnObf != null) {
            for (MethodNode mn : cn.methods) {
                if (mn.desc.startsWith("(L" + pbnObf + ";") && mn.desc.endsWith("V")) {
                    JMethod m = new JMethod();
                    m.setName("addNode");
                    m.setObfuscatedName(mn.name);
                    m.setOwner("PacketWriter");
                    m.setOwnerObfuscatedName(cn.name);
                    m.setDescriptor(mn.desc);
                    jc.getMethods().add(m);
                    break;
                }
            }
        }

        // constructChat: method with String arg
        for (MethodNode mn : cn.methods) {
            if (mn.desc.contains("Ljava/lang/String;") && !mn.name.equals("<init>") && !mn.name.equals("<clinit>")) {
                JMethod m = new JMethod();
                m.setName("constructChat");
                m.setObfuscatedName(mn.name);
                m.setOwner("PacketWriter");
                m.setOwnerObfuscatedName(cn.name);
                m.setDescriptor(mn.desc);
                jc.getMethods().add(m);
                break;
            }
        }

        out.add(jc);
    }

    private static void identifyGameEngine(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findGameEngineObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: GameEngine not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("GameEngine", cn);
        System.out.println("[MappingGenerator] GameEngine -> " + cn.name);
        cn.fields.stream().filter(f -> f.desc.equals("Z")).findFirst().ifPresent(fn -> {
            JField f = new JField();
            f.setName("graphicsGuard");
            f.setObfuscatedName(fn.name);
            f.setOwner("GameEngine");
            f.setOwnerObfuscatedName(cn.name);
            f.setDescriptor("Z");
            jc.getFields().add(f);
        });
        out.add(jc);
    }

    private static void identifyTask(Map<String, ClassNode> classes, List<JClass> out) {
        String gameEngineObf = findGameEngineObfName(classes);
        // Pick the simplest Runnable that isn't GameEngine
        ClassNode best = null;
        for (ClassNode cn : classes.values()) {
            if (cn.interfaces == null || !cn.interfaces.contains("java/lang/Runnable"))
                continue;
            if (cn.name.equals(gameEngineObf))
                continue;
            if (!cn.superName.equals("java/lang/Object"))
                continue;
            if (cn.fields.size() > 10)
                continue;
            if (best == null || cn.fields.size() < best.fields.size())
                best = cn;
        }
        if (best == null) {
            System.err.println("[MappingGenerator] ERROR: Task not found");
            return;
        }
        JClass jc = build("Task", best);
        System.out.println("[MappingGenerator] Task -> " + best.name);
        String clientObf = findClientObfName(classes);
        if (clientObf != null) {
            final ClassNode finalBest = best;
            best.fields.stream().filter(f -> f.desc.equals(fd(clientObf))).findFirst().ifPresent(fn -> {
                JField f = new JField();
                f.setName("client");
                f.setObfuscatedName(fn.name);
                f.setOwner("Task");
                f.setOwnerObfuscatedName(finalBest.name);
                f.setDescriptor(fn.desc);
                jc.getFields().add(f);
            });
        }
        out.add(jc);
    }

    private static void identifyMouseHandler(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findMouseHandlerObfName(classes);
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: MouseHandler not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("MouseHandler", cn);
        System.out.println("[MappingGenerator] MouseHandler -> " + cn.name);

        List<FieldNode> longs = new ArrayList<>(), ints = new ArrayList<>();
        for (FieldNode fn : cn.fields) {
            if (fn.desc.equals("J"))
                longs.add(fn);
            else if (fn.desc.equals("I"))
                ints.add(fn);
        }
        if (!longs.isEmpty()) {
            JField f = new JField();
            f.setName("MouseHandler_lastPressedTimeMillis");
            f.setObfuscatedName(longs.get(0).name);
            f.setOwner("MouseHandler");
            f.setOwnerObfuscatedName(cn.name);
            f.setDescriptor("J");
            jc.getFields().add(f);
        }
        if (ints.size() >= 2) {
            JField fx = new JField();
            fx.setName("MouseHandler_x");
            fx.setObfuscatedName(ints.get(0).name);
            fx.setOwner("MouseHandler");
            fx.setOwnerObfuscatedName(cn.name);
            fx.setDescriptor("I");
            jc.getFields().add(fx);
            JField fy = new JField();
            fy.setName("MouseHandler_y");
            fy.setObfuscatedName(ints.get(1).name);
            fy.setOwner("MouseHandler");
            fy.setOwnerObfuscatedName(cn.name);
            fy.setDescriptor("I");
            jc.getFields().add(fy);
        }
        out.add(jc);
    }

    private static void identifyAccountType(Map<String, ClassNode> classes, List<JClass> out) {
        for (ClassNode cn : classes.values()) {
            long staticFinalInt = cn.fields.stream().filter(f -> f.desc.equals("I") && (f.access & 0x0018) == 0x0018)
                    .count();
            if (staticFinalInt >= 3 && cn.fields.size() <= 8 && cn.methods.size() <= 5) {
                JClass jc = build("AccountType", cn);
                System.out.println("[MappingGenerator] AccountType -> " + cn.name);
                out.add(jc);
                return;
            }
        }
        System.err.println("[MappingGenerator] ERROR: AccountType not found");
    }

    private static void identifyObjectComposition(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findByInterface(classes, "net/runelite/api/ObjectComposition");
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: ObjectComposition not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("ObjectComposition", cn);
        System.out.println("[MappingGenerator] ObjectComposition -> " + cn.name);
        out.add(jc);
    }

    private static void identifyItemComposition(Map<String, ClassNode> classes, List<JClass> out) {
        String obf = findByInterface(classes, "net/runelite/api/ItemComposition");
        if (obf == null) {
            System.err.println("[MappingGenerator] ERROR: ItemComposition not found");
            return;
        }
        ClassNode cn = classes.get(obf);
        JClass jc = build("ItemComposition", cn);
        System.out.println("[MappingGenerator] ItemComposition -> " + cn.name);
        addFieldByDesc(jc, cn, "groundActions", "[Ljava/lang/String;");
        out.add(jc);
    }

    private static void identifyLanguage(Map<String, ClassNode> classes, List<JClass> out) {
        // Language: small class with static final int constants, not AccountType
        String accountTypeObf = null;
        for (JClass c : new ArrayList<JClass>()) {
        } // placeholder
        for (ClassNode cn : classes.values()) {
            if (cn.fields.size() < 2 || cn.fields.size() > 6)
                continue;
            long staticFinalInt = cn.fields.stream().filter(f -> f.desc.equals("I") && (f.access & 0x0018) == 0x0018)
                    .count();
            if (staticFinalInt < 2)
                continue;
            if (cn.methods.size() > 3)
                continue;
            JClass jc = build("Language", cn);
            System.out.println("[MappingGenerator] Language -> " + cn.name);
            out.add(jc);
            return;
        }
        System.err.println("[MappingGenerator] ERROR: Language not found");
    }

    private static void identifyLogin(Map<String, ClassNode> classes, List<JClass> out) {
        for (ClassNode cn : classes.values()) {
            long staticIntFields = cn.fields.stream().filter(f -> f.desc.equals("I") && (f.access & 0x0008) != 0)
                    .count();
            if (staticIntFields < 3)
                continue;
            boolean hasSetMethod = cn.methods.stream().anyMatch(m -> m.desc.equals("(I)V") && (m.access & 0x0008) != 0);
            if (!hasSetMethod)
                continue;
            JClass jc = build("Login", cn);
            System.out.println("[MappingGenerator] Login -> " + cn.name);
            List<FieldNode> sInts = new ArrayList<>();
            for (FieldNode fn : cn.fields)
                if (fn.desc.equals("I") && (fn.access & 0x0008) != 0)
                    sInts.add(fn);
            addStaticIntField(jc, cn, "accountTypeCheck", 0);
            addStaticIntField(jc, cn, "legacyType", 1);
            addStaticIntField(jc, cn, "jagexType", 2);
            cn.methods.stream().filter(m -> m.desc.equals("(I)V") && (m.access & 0x0008) != 0).findFirst()
                    .ifPresent(mn -> {
                        JMethod m = new JMethod();
                        m.setName("setLoginIndex");
                        m.setObfuscatedName(mn.name);
                        m.setOwner("Login");
                        m.setOwnerObfuscatedName(cn.name);
                        m.setDescriptor(mn.desc);
                        m.setStatic(true);
                        jc.getMethods().add(m);
                    });
            out.add(jc);
            return;
        }
        System.err.println("[MappingGenerator] ERROR: Login not found");
    }

    private static void identifyLoginHash(Map<String, ClassNode> classes, List<JClass> out) {
        String gameEngineObf = findGameEngineObfName(classes);
        String clientObf = findClientObfName(classes);
        for (ClassNode cn : classes.values()) {
            if (cn.name.equals(gameEngineObf))
                continue; // GameEngine also has static [J fields
            if (cn.name.equals(clientObf))
                continue; // Client also has static [J fields
            long longArrayFields = cn.fields.stream().filter(f -> f.desc.equals("[J") && (f.access & 0x0008) != 0)
                    .count();
            if (longArrayFields >= 2) {
                JClass jc = build("LoginHash", cn);
                System.out.println("[MappingGenerator] LoginHash -> " + cn.name);
                List<FieldNode> laFields = new ArrayList<>();
                for (FieldNode fn : cn.fields)
                    if (fn.desc.equals("[J") && (fn.access & 0x0008) != 0)
                        laFields.add(fn);
                JField f1 = new JField();
                f1.setName("packedCallStack1");
                f1.setObfuscatedName(laFields.get(0).name);
                f1.setOwner("LoginHash");
                f1.setOwnerObfuscatedName(cn.name);
                f1.setDescriptor("[J");
                f1.setStatic(true);
                jc.getFields().add(f1);
                JField f2 = new JField();
                f2.setName("packedCallStack2");
                f2.setObfuscatedName(laFields.get(1).name);
                f2.setOwner("LoginHash");
                f2.setOwnerObfuscatedName(cn.name);
                f2.setDescriptor("[J");
                f2.setStatic(true);
                jc.getFields().add(f2);
                out.add(jc);
                return;
            }
        }
        System.err.println("[MappingGenerator] ERROR: LoginHash not found");
    }

    private static void identifyJagexAccounts(Map<String, ClassNode> classes, List<JClass> out) {
        for (ClassNode cn : classes.values()) {
            long staticStringFields = cn.fields.stream()
                    .filter(f -> f.desc.equals("Ljava/lang/String;") && (f.access & 0x0008) != 0).count();
            if (staticStringFields >= 4) {
                JClass jc = build("JagexAccounts", cn);
                System.out.println("[MappingGenerator] JagexAccounts -> " + cn.name);
                addStaticStringField(jc, cn, "JX_DISPLAY_NAME", 0);
                addStaticStringField(jc, cn, "JX_CHARACTER_ID", 1);
                addStaticStringField(jc, cn, "JX_SESSION_ID", 2);
                addStaticStringField(jc, cn, "JX_REFRESH_TOKEN", 3);
                addStaticStringField(jc, cn, "JX_ACCESS_TOKEN", 4);
                out.add(jc);
                return;
            }
        }
        System.err.println("[MappingGenerator] ERROR: JagexAccounts not found");
    }

    private static void identifyRandomDat(Map<String, ClassNode> classes, List<JClass> out) {
        for (ClassNode cn : classes.values()) {
            boolean hasFileField = cn.fields.stream()
                    .anyMatch(f -> f.desc.equals("Ljava/io/File;") && (f.access & 0x0008) != 0);
            if (!hasFileField)
                continue;
            JClass jc = build("RandomDat", cn);
            System.out.println("[MappingGenerator] RandomDat -> " + cn.name);
            cn.fields.stream().filter(f -> f.desc.equals("Ljava/io/File;") && (f.access & 0x0008) != 0).findFirst()
                    .ifPresent(fn -> {
                        JField f = new JField();
                        f.setName("randomDat");
                        f.setObfuscatedName(fn.name);
                        f.setOwner("RandomDat");
                        f.setOwnerObfuscatedName(cn.name);
                        f.setDescriptor(fn.desc);
                        f.setStatic(true);
                        jc.getFields().add(f);
                    });
            out.add(jc);
            return;
        }
        System.err.println("[MappingGenerator] ERROR: RandomDat not found");
    }
}

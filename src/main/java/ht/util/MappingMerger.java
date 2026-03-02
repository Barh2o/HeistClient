package ht.heist.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import ht.heist.util.dto.JClass;
import ht.heist.util.dto.JField;
import ht.heist.util.dto.JMethod;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MappingMerger {
    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: MappingMerger <path_to_vita_mappings.json>");
            return;
        }

        Path vitaPath = Paths.get(args[0]);
        if (!Files.exists(vitaPath)) {
            System.err.println("VitaLite mappings file not found at: " + vitaPath);
            return;
        }

        Path localPath = Paths.get("src/main/resources/injector/mappings.json");
        if (!Files.exists(localPath)) {
            System.err.println("Local mappings not found at: " + localPath);
            return;
        }

        Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .setPrettyPrinting()
                .create();

        // Load local mappings
        List<JClass> localMappings;
        try (Reader reader = Files.newBufferedReader(localPath)) {
            localMappings = gson.fromJson(reader, new TypeToken<List<JClass>>() {
            }.getType());
        }
        if (localMappings == null)
            localMappings = new ArrayList<>();

        // Load vita mappings
        List<JClass> vitaMappings;
        try (Reader reader = Files.newBufferedReader(vitaPath)) {
            vitaMappings = gson.fromJson(reader, new TypeToken<List<JClass>>() {
            }.getType());
        }
        if (vitaMappings == null)
            vitaMappings = new ArrayList<>();

        System.out.println("[MappingMerger] Local classes: " + localMappings.size());
        System.out.println("[MappingMerger] Vita classes:  " + vitaMappings.size());

        Map<String, JClass> localMap = new LinkedHashMap<>();
        for (JClass jc : localMappings) {
            if (jc.getName() != null) {
                localMap.put(jc.getName(), jc);
            }
        }

        int addedClasses = 0;
        int addedFields = 0;
        int addedMethods = 0;

        for (JClass vitaClass : vitaMappings) {
            if (vitaClass.getName() == null)
                continue;

            JClass localClass = localMap.get(vitaClass.getName());
            if (localClass == null) {
                localMappings.add(vitaClass);
                localMap.put(vitaClass.getName(), vitaClass);
                addedClasses++;
                continue;
            }

            // Merge fields
            Map<String, JField> localFields = new HashMap<>();
            for (JField localF : localClass.getFields()) {
                if (localF.getName() != null) {
                    localFields.put(localF.getName(), localF);
                }
            }

            for (JField vitaF : vitaClass.getFields()) {
                if (vitaF.getName() == null)
                    continue;
                if (!localFields.containsKey(vitaF.getName())) {
                    vitaF.setOwnerObfuscatedName(localClass.getObfuscatedName());
                    localClass.getFields().add(vitaF);
                    addedFields++;
                }
            }

            // Merge methods
            Map<String, JMethod> localMethods = new HashMap<>();
            for (JMethod localM : localClass.getMethods()) {
                if (localM.getName() != null) {
                    localMethods.put(localM.getName(), localM);
                }
            }

            for (JMethod vitaM : vitaClass.getMethods()) {
                if (vitaM.getName() == null)
                    continue;
                if (!localMethods.containsKey(vitaM.getName())) {
                    vitaM.setOwnerObfuscatedName(localClass.getObfuscatedName());
                    localClass.getMethods().add(vitaM);
                    addedMethods++;
                }
            }
        }

        System.out.println("[MappingMerger] Merge complete.");
        System.out.println("  Added classes: " + addedClasses);
        System.out.println("  Added fields:  " + addedFields);
        System.out.println("  Added methods: " + addedMethods);

        try (Writer writer = Files.newBufferedWriter(localPath)) {
            gson.toJson(localMappings, writer);
        }
        System.out.println("[MappingMerger] Saved to " + localPath);

        Path secondaryLocalPath = Paths.get("src/main/resources/ht/heist/injector/mappings.json");
        if (Files.exists(secondaryLocalPath)) {
            try (Writer writer = Files.newBufferedWriter(secondaryLocalPath)) {
                gson.toJson(localMappings, writer);
            }
            System.out.println("[MappingMerger] Saved to " + secondaryLocalPath);
        }
    }
}

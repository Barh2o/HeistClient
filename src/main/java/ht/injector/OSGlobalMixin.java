package ht.heist.injector;

import ht.heist.Static;
import ht.heist.util.asm.BytecodeBuilder;
import ht.heist.injector.util.LdcRewriter;
import ht.heist.util.MappingProvider;
import ht.heist.injector.util.expreditor.impls.*;
import ht.heist.model.ConditionType;
import ht.heist.util.dto.JClass;
import ht.heist.util.dto.JField;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.List;

public class OSGlobalMixin {
    private static final PathsGetReplacer pathsGetReplacer = new PathsGetReplacer();
    private static final ModifyResourceLoading modifyResourceLoading = new ModifyResourceLoading();
    private static final ReplaceMethodByString replaceMethodByString = new ReplaceMethodByString(
            "Attempted to load patches of already loading midiplayer!");
    private static final RuntimeMaxMemoryReplacer memoryReplacer = new RuntimeMaxMemoryReplacer(805_306_368L);
    private static final SystemPropertyReplacer propertyReplacer = new SystemPropertyReplacer();
    private static final IntegerLiteralReplacer integerReplacer = new IntegerLiteralReplacer(-1094877034);

    public static void patch(ClassNode classNode) {
        pathsGetReplacer.instrument(classNode);
        memoryReplacer.instrument(classNode);
        propertyReplacer.instrument(classNode);
        integerReplacer.instrument(classNode);

        if (Static.getCliArgs().isNoMusic() || Static.getCliArgs().isMin()) {
            replaceMethodByString.instrument(classNode);
            modifyResourceLoading.instrument(classNode);
        }

        for (MethodNode method : classNode.methods) {
            randomDat(classNode, method);
            mouseFlag(method);
            isHidden(classNode, method);

            if (!Static.getCliArgs().isIncognito()) {
                LdcRewriter.rewriteString(
                        method,
                        "Welcome to RuneScape",
                        "<col=FFFFFF>Welcome to </col><col=00FFFF>HeistClient</col>");
            }
        }
    }

    /**
     * Hack to child-proof side effects of headless mode
     */
    public static void isHidden(ClassNode cn, MethodNode method) {
        if (!method.name.equals("isHidden") || !method.desc.equals("()Z"))
            return;

        InsnList code = BytecodeBuilder.create()
                .ifBlock(
                        ConditionType.FALSE, // If not equal to 0 (i.e., if true)
                        b -> b.invokeStatic("ht/heist/Static", "isHeadless", "()Z"),
                        b -> b.pushThis()
                                .invokeVirtual(cn.name, "isSelfHidden", "()Z")
                                .returnValue(Opcodes.IRETURN))
                .build();

        method.instructions.insert(code);
    }

    public static void mouseFlag(MethodNode method) {
        JClass client = MappingProvider.getClass("Client");
        JField mouseFlag = MappingProvider.getField(client, "mouseFlag");

        if (mouseFlag == null)
            return; // mapping not yet available

        List<FieldInsnNode> toReplace = new ArrayList<>();

        for (AbstractInsnNode insn : method.instructions) {
            if (insn.getOpcode() != Opcodes.GETSTATIC)
                continue;

            FieldInsnNode fin = (FieldInsnNode) insn;
            if (!fin.owner.equals(mouseFlag.getOwnerObfuscatedName())
                    || !fin.name.equals(mouseFlag.getObfuscatedName()))
                continue;

            toReplace.add(fin);
        }

        if (toReplace.isEmpty())
            return;

        for (FieldInsnNode insn : toReplace) {
            InsnNode iconst0 = new InsnNode(Opcodes.ICONST_0);
            method.instructions.set(insn, iconst0);
        }
    }

    public static void randomDat(ClassNode clazz, MethodNode method) {
        JClass client = MappingProvider.getClass("Client");
        JField randomDat = MappingProvider.getField(client, "randomDat");
        if (randomDat == null)
            return; // mapping not yet available
        AbstractInsnNode target = null;
        for (AbstractInsnNode insn : method.instructions) {
            if (!(insn instanceof FieldInsnNode))
                continue;
            FieldInsnNode fin = (FieldInsnNode) insn;
            if (!fin.owner.equals(randomDat.getOwnerObfuscatedName())
                    || !fin.name.equals(randomDat.getObfuscatedName()))
                continue;

            if (insn.getNext().getOpcode() != Opcodes.IFNULL) {
                if (insn.getPrevious().getOpcode() != Opcodes.ACONST_NULL || !(insn.getNext() instanceof JumpInsnNode))
                    continue;
                if (insn.getNext().getOpcode() == Opcodes.GOTO)
                    continue;
                target = insn;
                break;
            }
            target = insn;
            break;
        }

        if (target == null) {
            return;
        }

        InsnList code = BytecodeBuilder.create()
                .pushString(clazz.name + "." + method.name + method.desc)
                .invokeStatic("client", "setRandomDat", "(Ljava/lang/String;)V")
                .build();

        method.instructions.insertBefore(target, code);
    }
}

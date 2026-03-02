package ht.heist.injector.pipeline;

import ht.heist.injector.annotations.Disable;
import ht.heist.injector.util.AnnotationUtil;
import ht.heist.injector.util.*;
import ht.heist.model.ConditionType;
import ht.heist.util.asm.BytecodeBuilder;
import ht.heist.util.asm.InsnUtil;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

/**
 * Injects conditional disable checks into methods based on boolean return
 * values.
 */
public class DisableTransformer {
    /**
     * Injects conditional disable logic into target method based on boolean return
     * value.
     * 
     * @param mixin  mixin class containing disable method
     * @param method method annotated with Disable that returns boolean
     */
    public static void patch(ClassNode mixin, MethodNode method) {
        if (!method.desc.endsWith(")Z")) {
            throw new RuntimeException(
                    "Method " + method.name + " in mixin " + mixin.name + " must be a boolean method.");
        }

        String name = AnnotationUtil.getAnnotation(method, Disable.class, "value");

        ClassNode gamepack = TransformerUtil.getMethodClass(mixin, name);
        if (gamepack == null) {
            System.out.println("[DisableTransformer] WARN: No method mapping for " + name + " — skipping.");
            return;
        }
        InjectTransformer.patch(gamepack, mixin, method);

        MethodNode toHook = TransformerUtil.getTargetMethod(mixin, name);

        if (toHook == null) {
            System.err.println("Could not find method to hook: " + name);
            return;
        }

        InsnList call = MethodUtil.generateContextAwareInvoke(gamepack, toHook, method, false);

        InsnList instructions = BytecodeBuilder.create()
                .ifBlock(
                        ConditionType.NOT_EQUALS,
                        b -> b.appendInsnList(call).pushInt(0),
                        b -> {
                            InsnList returnInsnList = InsnUtil.generateDefaultReturn(toHook);
                            b.appendInsnList(returnInsnList);
                        })
                .build();

        AbstractInsnNode injectionPoint = null;

        if (toHook.name.equals("<init>")) {
            for (AbstractInsnNode insn : toHook.instructions) {
                if (insn.getOpcode() == Opcodes.INVOKESPECIAL) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;
                    if (methodInsn.name.equals("<init>")) {
                        injectionPoint = insn;
                        break;
                    }
                }
            }
        }

        boolean isHookedStatic = (toHook.access & Opcodes.ACC_STATIC) != 0;
        if (injectionPoint != null) {
            toHook.instructions.insert(injectionPoint, instructions);
        } else {
            if (isHookedStatic)
                toHook.instructions.insert(instructions);
            else
                toHook.instructions.insert(toHook.instructions.getFirst(), instructions);
        }
    }
}

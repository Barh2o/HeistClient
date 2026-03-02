package ht.heist.rlmixins;

import ht.heist.injector.annotations.At;
import ht.heist.injector.annotations.AtTarget;
import ht.heist.injector.annotations.Insert;
import ht.heist.injector.annotations.Mixin;
import ht.heist.util.asm.BytecodeBuilder;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

@Mixin("net/runelite/client/RuneLite")
public class RuneLiteMixin {
    @Insert(
            method = "main",
            at = @At(
                    value = AtTarget.CHECKCAST,
                    owner = "net/runelite/client/RuneLite"
            ),
            raw = true
    )
    public static void main(ClassNode classNode, MethodNode method, AbstractInsnNode insertionPoint) {
        FieldNode rlInstanceField = new FieldNode(
                Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
                "rlInstance",
                "Lnet/runelite/client/RuneLite;",
                null,
                null
        );
        classNode.fields.add(rlInstanceField);

        InsnList toInject = BytecodeBuilder.create()
                .dup()
                .putStaticField("net/runelite/client/RuneLite", "rlInstance", "Lnet/runelite/client/RuneLite;")
                .build();

        method.instructions.insert(insertionPoint, toInject);
    }
}

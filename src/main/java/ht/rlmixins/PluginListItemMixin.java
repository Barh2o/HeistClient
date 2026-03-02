package ht.heist.rlmixins;

import ht.heist.injector.annotations.At;
import ht.heist.injector.annotations.AtTarget;
import ht.heist.injector.annotations.Insert;
import ht.heist.injector.annotations.Mixin;
import ht.heist.util.asm.BytecodeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;

import java.util.ArrayList;
import java.util.List;

@Mixin("net/runelite/client/plugins/config/PluginListItem")
public class PluginListItemMixin
{
    @Insert(
            method = "<init>",
            at = @At(value = AtTarget.RETURN),
            raw = true
    )
    public static void constructorHook(MethodNode method, AbstractInsnNode insertionPoint) {
        BytecodeBuilder builder = BytecodeBuilder.create();

        builder.tryCatch(
                "java/lang/Exception",
                tryBlock -> {
                    tryBlock
                            .pushThis()
                            .pushThis()
                            .getField(
                                    "net/runelite/client/plugins/config/PluginListItem",
                                    "pluginConfig",
                                    "Lnet/runelite/client/plugins/config/PluginConfigurationDescriptor;"
                            )
                            .invokeVirtual(
                                    "net/runelite/client/plugins/config/PluginConfigurationDescriptor",
                                    "getPlugin",
                                    "()Lnet/runelite/client/plugins/Plugin;"
                            )
                            .invokeStatic(
                                    "ht/heist/services/hotswapper/PluginReloader",
                                    "addRedButtonAfterPin",
                                    "(Ljavax/swing/JPanel;Lnet/runelite/client/plugins/Plugin;)V"
                            );
                },
                catchBlock -> {
                    catchBlock
                            .dup()
                            .invokeVirtual("java/lang/Exception", "printStackTrace", "()V")
                            .pop();
                }
        );

        InsnList code = builder.build();
        method.instructions.insertBefore(insertionPoint, code);

        List<TryCatchBlockNode> tryCatchBlocks = builder.getTryCatchBlocks();
        if (method.tryCatchBlocks == null) {
            method.tryCatchBlocks = new ArrayList<>();
        }
        method.tryCatchBlocks.addAll(tryCatchBlocks);
    }
}

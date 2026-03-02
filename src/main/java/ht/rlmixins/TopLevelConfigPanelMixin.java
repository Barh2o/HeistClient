package ht.heist.rlmixins;

import ht.heist.injector.annotations.At;
import ht.heist.injector.annotations.AtTarget;
import ht.heist.injector.annotations.Insert;
import ht.heist.injector.annotations.Mixin;
import ht.heist.util.asm.BytecodeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import static org.objectweb.asm.Opcodes.*;

@Mixin("net/runelite/client/plugins/config/TopLevelConfigPanel")
public class TopLevelConfigPanelMixin
{
    @Insert(
            method = "<init>",
            at = @At(
                    value = AtTarget.INVOKE,
                    owner = "net/runelite/client/plugins/config/TopLevelConfigPanel",
                    target = "addTab"
            ),
            ordinal = -1,
            raw = true
    )
    public static void constructorHook(MethodNode method, AbstractInsnNode insertionPoint)
    {
        InsnList code = BytecodeBuilder.create()
                .loadLocal(0, ALOAD)
                .invokeStatic(
                        "ht/heist/ui/sdn/VitaExternalsPanel",
                        "get",
                        "()Lht/heist/ui/sdn/VitaExternalsPanel;"
                )
                .pushString("/ht/heist/HeistClient/icon-small.png")
                .pushString("Vita Hub")
                .invokeVirtual(
                        "net/runelite/client/plugins/config/TopLevelConfigPanel",
                        "addTab",
                        "(Lnet/runelite/client/ui/PluginPanel;Ljava/lang/String;Ljava/lang/String;)Lnet/runelite/client/ui/components/materialtabs/MaterialTab;"
                ).pop().build();
        method.instructions.insert(insertionPoint, code);
    }
}

package ht.heist.rlmixins;

import ht.heist.injector.annotations.At;
import ht.heist.injector.annotations.AtTarget;
import ht.heist.injector.annotations.Insert;
import ht.heist.injector.annotations.Mixin;
import ht.heist.util.asm.BytecodeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.InsnList;

@Mixin("net/runelite/client/plugins/config/PluginToggleButton")
public class PluginToggleButtonMixin
{
    private static final String LOAD_DESCRIPTOR = "(Ljava/lang/Class;Ljava/lang/String;)Ljava/awt/image/BufferedImage;";
    private static final String SWITCHER_PATH = "switcher_on.png";
    private static final String RECOLOR_DESCRIPTOR = "(Ljava/awt/Image;Ljava/awt/Color;)Ljava/awt/image/BufferedImage;";

    @Insert(
            method = "<clinit>",
            at = @At(value = AtTarget.RETURN),
            raw = true
    )
    public static void recolorToggleButton(MethodNode method, AbstractInsnNode insertionPoint)
    {
        for (AbstractInsnNode node : method.instructions.toArray())
        {
            if (!(node instanceof MethodInsnNode))
            {
                continue;
            }

            MethodInsnNode methodInsn = (MethodInsnNode) node;

            if (!"net/runelite/client/util/ImageUtil".equals(methodInsn.owner)
                    || !"loadImageResource".equals(methodInsn.name)
                    || !LOAD_DESCRIPTOR.equals(methodInsn.desc))
            {
                continue;
            }

            AbstractInsnNode previous = methodInsn.getPrevious();

            if (!(previous instanceof LdcInsnNode))
            {
                continue;
            }

            LdcInsnNode ldc = (LdcInsnNode) previous;
            if (!SWITCHER_PATH.equals(ldc.cst))
            {
                continue;
            }

            InsnList injected = BytecodeBuilder.create()
                    .getStaticField(
                        "net/runelite/client/ui/ColorScheme",
                        "BRAND_ORANGE",
                        "Ljava/awt/Color;"
                    )
                    .invokeStatic(
                        "net/runelite/client/util/ImageUtil",
                        "recolorImage",
                        RECOLOR_DESCRIPTOR
                    )
                    .build();

            method.instructions.insert(methodInsn, injected);
            break;
        }
    }
}

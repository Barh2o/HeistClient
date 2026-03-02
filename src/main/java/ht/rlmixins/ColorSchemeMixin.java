package ht.heist.rlmixins;

import ht.heist.injector.annotations.At;
import ht.heist.injector.annotations.AtTarget;
import ht.heist.injector.annotations.Insert;
import ht.heist.injector.annotations.Mixin;
import ht.heist.util.asm.BytecodeBuilder;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Mixin("net/runelite/client/ui/ColorScheme")
public class ColorSchemeMixin
{
    @Insert(
            method = "<clinit>",
            at = @At(value = AtTarget.RETURN),
            raw = true
    )
    public static void brandColor(MethodNode method, AbstractInsnNode insertionPoint)
    {
        BytecodeBuilder builder = BytecodeBuilder.create();
        builder
                .newInstance("java/awt/Color")
                .dup()
                .pushInt(50)
                .pushInt(160)
                .pushInt(250)
                .pushInt(255)
                .invokeSpecial("java/awt/Color", "<init>", "(IIII)V")
                .putStaticField("net/runelite/client/ui/ColorScheme", "BRAND_ORANGE", "Ljava/awt/Color;");

        method.instructions.insertBefore(insertionPoint, builder.build());
    }
}

package ht.rlmixins;

import ht.heist.Static;
import ht.heist.injector.annotations.*;
import ht.heist.util.asm.BytecodeBuilder;
import ht.heist.injector.util.LdcRewriter;
import ht.heist.model.ConditionType;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

@Mixin("net/runelite/client/ui/SplashScreen")
public class SplashScreenMixin {
        @Insert(method = "<init>", at = @At(value = AtTarget.RETURN), raw = true)
        public static void constructorHook(MethodNode method, AbstractInsnNode insertionPoint) {
                BytecodeBuilder bb = BytecodeBuilder.create();
                var label = bb.createLabel("skipPatch");
                InsnList code = bb
                                .invokeStatic(
                                                "ht/heist/Static",
                                                "getCliArgs",
                                                "()Lht/heist/HeistClientOptions;")
                                .invokeVirtual(
                                                "ht/heist/HeistClientOptions",
                                                "isIncognito",
                                                "()Z")
                                .jumpIf(ConditionType.TRUE, label)
                                .pushThis()
                                .invokeStatic(
                                                "ht/heist/runelite/ClientUIUpdater",
                                                "patchSplashScreen",
                                                "(Ljavax/swing/JFrame;)V")
                                .placeLabel(label)
                                .build();

                method.instructions.insertBefore(
                                insertionPoint,
                                code);

                if (Static.getCliArgs().isIncognito())
                        return;

                // LdcRewriter.rewriteString(method, "runelite_splash.png", "icon_splash.png");
                // LdcRewriter.rewriteClassRef(method, "net/runelite/client/ui/SplashScreen",
                // "ht/heist/heistclient/Main");
        }
}

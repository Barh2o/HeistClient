package ht.heist.mixins;

import ht.heist.injector.annotations.At;
import ht.heist.injector.annotations.AtTarget;
import ht.heist.injector.annotations.Insert;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.util.LoopSleepInjector;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodNode;

@Mixin("MouseRecorder")
public class TMouseRecorderMixin {
    @Insert(method = "run", at = @At(value = AtTarget.RETURN), raw = true)
    public static void constructorHook(MethodNode method, AbstractInsnNode insertionPoint) {
        LoopSleepInjector.injectConditionalSleepSafe(method, "ht/heist/services/ClickManager", "shouldBlockManualMovement", "()Z");
    }
}

package ht.heist.rlmixins;

import ht.heist.injector.annotations.At;
import ht.heist.injector.annotations.AtTarget;
import ht.heist.injector.annotations.Insert;
import ht.heist.injector.annotations.Mixin;
import ht.heist.runelite.ClientUIUpdater;

@Mixin("net/runelite/client/ui/ClientUI")
public class ClientUIMixin {
    @Insert(
            method = "lambda$init$6",
            at = @At(value = AtTarget.INVOKE, owner = "net/runelite/client/ui/ClientUI", target = "updateFrameConfig"),
            ordinal = -1
    )
    public static void initVita() {
        ClientUIUpdater.inject();
    }
}

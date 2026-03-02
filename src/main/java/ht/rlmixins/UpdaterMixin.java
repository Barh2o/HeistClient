package ht.heist.rlmixins;

import ht.heist.injector.annotations.MethodOverride;
import ht.heist.injector.annotations.Mixin;

@Mixin("net/runelite/client/Updater")
public class UpdaterMixin {
    @MethodOverride("tryUpdate")
    public void tryUpdate() {
        return;
    }
}

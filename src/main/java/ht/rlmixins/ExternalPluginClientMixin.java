package ht.heist.rlmixins;

import ht.heist.injector.annotations.MethodOverride;
import ht.heist.injector.annotations.Mixin;

@Mixin("net/runelite/client/externalplugins/ExternalPluginClient")
public class ExternalPluginClientMixin {

    @MethodOverride("submitPlugins")
    public void submitPlugins() {
        return;
    }
}

package ht.heist.rlmixins;

import ht.heist.injector.annotations.MethodOverride;
import ht.heist.injector.annotations.Mixin;

@Mixin("net/runelite/client/plugins/grandexchange/GrandExchangeClient")
public class GrandExchangeClientMixin {
    @MethodOverride("submit")
    public void submit()
    {

    }
}

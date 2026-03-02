package ht.heist.rlmixins;

import ht.heist.injector.annotations.MethodOverride;
import ht.heist.injector.annotations.Mixin;

@Mixin("net/runelite/client/ClientSessionManager")
public class ClientSessionManagerMixin
{
    @MethodOverride("start")
    public void start()
    {
    }

    @MethodOverride("onClientShutdown")
    public void onClientShutdown()
    {
    }

    @MethodOverride("ping")
    public void ping()
    {
    }

    @MethodOverride("isWorldHostValid")
    public boolean isWorldHostValid()
    {
        return true;
    }
}

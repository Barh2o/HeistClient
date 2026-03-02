package ht.heist.rlmixins;

import ht.heist.injector.annotations.MethodOverride;
import ht.heist.injector.annotations.Mixin;

@Mixin("net/runelite/client/discord/DiscordService")
public class DiscordServiceMixin
{
    @MethodOverride("<init>")
    public DiscordServiceMixin()
    {
        // Disable Discord integration
    }

    @MethodOverride("init")
    public void init()
    {
        // Disable Discord integration
    }

    @MethodOverride("updatePresence")
    public void updatePresence()
    {
        // Disable Discord integration
    }
}

package ht.heist.mixins;

import ht.heist.Static;
import ht.heist.injector.annotations.Disable;
import ht.heist.injector.annotations.Mixin;

@Mixin("Client")
public class TMouseHookMixin
{
    @Disable("mouseHookLoader")
    public static boolean mouseHookLoader()
    {
        return !Static.getCliArgs().isDisableMouseHook();
    }
}

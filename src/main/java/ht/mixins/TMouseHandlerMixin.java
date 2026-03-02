package ht.heist.mixins;

import ht.heist.api.TMouseHandler;
import ht.heist.injector.annotations.Inject;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;

@Mixin("MouseHandler")
public class TMouseHandlerMixin implements TMouseHandler
{
    @Shadow("MouseHandler_lastPressedTimeMillis")
    private static long mouseLastPressedMillis;

    @Shadow("MouseHandler_x")
    public static int mouseX;

    @Shadow("MouseHandler_y")
    public static int mouseY;

    @Inject
    public long getMouseLastPressedMillis() {
        return mouseLastPressedMillis;
    }

    @Inject
    public void setMouseLastPressedMillis(long millis) {
        mouseLastPressedMillis = millis;
    }

    @Override
    @Inject
    public int getMouseX() {
        return mouseX;
    }

    @Override
    @Inject
    public int getMouseY() {
        return mouseY;
    }
}

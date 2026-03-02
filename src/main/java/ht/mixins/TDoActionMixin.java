package ht.heist.mixins;

import ht.heist.api.TClient;
import ht.heist.injector.annotations.Inject;
import ht.heist.injector.annotations.MethodHook;
import ht.heist.injector.annotations.Mixin;
import ht.heist.injector.annotations.Shadow;
import ht.heist.model.ui.HeistClientOptionsPanel;

@Mixin("Client")
public abstract class TDoActionMixin implements TClient {
    @MethodHook("doAction")
    public static void invokeMenuActionHook(int param0, int param1, int opcode, int id, int itemId, int worldViewId, String option, String target, int canvasX, int canvasY) {
        HeistClientOptionsPanel.getInstance().onMenuAction(option, target, id, opcode, param0, param1, itemId);
    }

    @Shadow("doAction")
    public abstract void RSDoAction(int param0, int param1, int opcode, int id, int itemId, int worldViewId, String option, String target, int canvasX, int canvasY);

    @Override
    @Inject
    public void invokeMenuAction(String option, String target, int identifier, int opcode, int param0, int param1, int itemId, int x, int y) {
        if (!isClientThread())
            return;

        RSDoAction(param0, param1, opcode, identifier, itemId, -1, option, target, x, y);
    }

    @Override
    @Inject
    public void invokeMenuAction(String option, String target, int identifier, int opcode, int param0, int param1, int itemId, int worldView, int x, int y) {
        if (!isClientThread())
            return;

        RSDoAction(param0, param1, opcode, identifier, itemId, worldView, option, target, x, y);
    }
}

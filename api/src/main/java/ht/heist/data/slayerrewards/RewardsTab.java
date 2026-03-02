package ht.heist.data.slayerrewards;

import ht.heist.Static;
import ht.heist.api.game.GameAPI;
import ht.heist.api.widgets.SlayerRewardsAPI;
import ht.heist.api.widgets.WidgetAPI;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.runelite.api.MenuAction;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

@RequiredArgsConstructor
@Getter
public enum RewardsTab {
    UNLOCK(0),
    EXTEND(2),
    BUY(4),
    TASKS(6)
    ;

    private final int index;

    public void open()
    {
        GameAPI.invokeMenuAction(
                1,
                MenuAction.CC_OP.getId(),
                index,
                InterfaceID.SlayerRewards.TABS,
                -1
        );
    }
}

package ht.heist.api.widgets;

import ht.heist.data.slayerrewards.BuyTab;
import ht.heist.data.slayerrewards.ExtendTab;
import ht.heist.data.slayerrewards.TasksTab;
import ht.heist.data.slayerrewards.UnlockTab;
import lombok.Getter;
import net.runelite.api.gameval.InterfaceID;
import net.runelite.api.widgets.Widget;

public class SlayerRewardsAPI
{
    @Getter
    private static final UnlockTab unlockTab = new UnlockTab();
    @Getter
    private static final ExtendTab extendTab = new ExtendTab();
    @Getter
    private static final BuyTab buyTab = new BuyTab();
    @Getter
    private static final TasksTab tasksTab = new TasksTab();

    public static boolean isOpen()
    {
        Widget slayerWidget = WidgetAPI.get(InterfaceID.SlayerRewards.UNIVERSE);
        return slayerWidget != null && WidgetAPI.isVisible(slayerWidget);
    }

    public static int getPoints()
    {
        Widget widget = WidgetAPI.get(InterfaceID.SlayerRewards.TABS).getChild(8);
        String text = WidgetAPI.getText(widget);
        return Integer.parseInt(text.replaceAll("[^0-9]", ""));
    }
}

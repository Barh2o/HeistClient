package ht.heist.data.locatables.sailing;

import ht.heist.api.widgets.DialogueAPI;
import ht.heist.api.widgets.WidgetAPI;
import net.runelite.api.gameval.InterfaceID;

public class BoatBoardingAPI
{
    public static boolean isOpen()
    {
        return WidgetAPI.isVisible(InterfaceID.SailingBoatSelection.UNIVERSE);
    }

    public static boolean boardRecent()
    {
        if(!isOpen())
        {
            return false;
        }
        DialogueAPI.resumePause(InterfaceID.SailingBoatSelection.BOATS_CLICK_LAYER, 2);
        return true;
    }
}

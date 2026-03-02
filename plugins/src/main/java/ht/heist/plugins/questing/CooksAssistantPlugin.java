package ht.heist.plugins.questing;

import ht.heist.Logger;
import ht.heist.api.widgets.InventoryAPI;
import ht.heist.api.game.MovementAPI;
import ht.heist.api.game.SceneAPI;
import ht.heist.api.widgets.WidgetAPI;
import ht.heist.plugins.questing.QuestEngine.State;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import javax.inject.Inject;
import java.util.function.BooleanSupplier;

@PluginDescriptor(name = "Cook's Assistant (Heist)", description = "Automates Cook's Assistant gathering and completion", tags = {
        "quest", "cook", "heist" })
public class CooksAssistantPlugin extends Plugin {

    @Inject
    private QuestEngine engine;

    private static final WorldPoint COOK_LOCATION = new WorldPoint(3207, 3213, 0);
    private static final WorldPoint DAIRY_COW_LOCATION = new WorldPoint(3254, 3270, 0);
    private static final WorldPoint CHICKEN_COOP_LOCATION = new WorldPoint(3230, 3297, 0);
    private static final WorldPoint FLOUR_MILL_LOCATION = new WorldPoint(3166, 3302, 0);

    @Override
    protected void startUp() throws Exception {
        Logger.info("Cook's Assistant automation started!");
        // Simple logic loop (in a real plugin, this would be in a recurring task)
    }

    private void runLoop() {
        // IDs: 1933=Pot of Flour, 1927=Bucket of milk, 1944=Egg
        if (!InventoryAPI.contains(1933)) {
            engine.walkConditional(FLOUR_MILL_LOCATION, (BooleanSupplier) () -> InventoryAPI.contains(1933));
            // Interaction logic...
        } else if (!InventoryAPI.contains(1927)) {
            engine.walkConditional(DAIRY_COW_LOCATION, (BooleanSupplier) () -> InventoryAPI.contains(1927));
        } else if (!InventoryAPI.contains(1944)) {
            engine.walkConditional(CHICKEN_COOP_LOCATION, (BooleanSupplier) () -> InventoryAPI.contains(1944));
        } else {
            engine.walkConditional(COOK_LOCATION, () -> false);
            // Click cook, talk, etc.
        }
    }
}

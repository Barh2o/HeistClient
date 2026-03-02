package ht.heist.plugins.multiclientutils.dispatchers;

import ht.heist.Logger;
import ht.heist.Static;
import ht.heist.api.entities.NpcAPI;
import ht.heist.api.entities.PlayerAPI;
import ht.heist.api.entities.TileObjectAPI;
import ht.heist.api.game.MovementAPI;
import ht.heist.data.wrappers.NpcEx;
import ht.heist.data.wrappers.PlayerEx;
import ht.heist.data.wrappers.TileObjectEx;
import ht.heist.plugins.multiclientutils.MultiClientUtilPlugin;
import ht.heist.queries.NpcQuery;
import ht.heist.queries.PlayerQuery;
import ht.heist.queries.TileObjectQuery;
import ht.heist.services.pathfinder.Walker;
import ht.heist.util.TextUtil;
import ht.heist.util.ThreadPool;
import ht.heist.util.WorldPointUtil;
import net.runelite.api.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.events.MenuEntryAdded;

import java.util.function.Consumer;

public class ExtendedMenuDispatcher
{
    public static void process(MultiClientUtilPlugin plugin, MenuEntryAdded entry)
    {
        Client client = Static.getClient();
        MenuAction action = MenuAction.of(entry.getType());
        String name = "<col=00ff00>" + TextUtil.sanitize(entry.getTarget()).split(" \\(")[0].trim() + " ";
        int id = entry.getIdentifier();
        int packedWP = getWorldPoint();
        switch (action)
        {
            case WALK:
                addMenuOption(
                        entry,
                        "(Nearby) Walk Here",
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("WALK", packedWP);
                            MovementAPI.walkToWorldPoint(WorldPointUtil.fromCompressed(packedWP));
                        })
                );
                addMenuOption(
                        entry,
                        "(All) Pathfind Here",
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PATHFIND", packedWP, client.getWorld());
                            Walker.walkTo(WorldPointUtil.fromCompressed(packedWP));
                        })
                );
                break;
            case GAME_OBJECT_FIRST_OPTION:
                addMenuOption(
                        entry,
                        "(Nearby) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("OBJECT", id, 0, packedWP);
                            TileObjectEx object = getObject(id, packedWP);
                            if(object == null)
                                return;
                            TileObjectAPI.interact(object, 0);
                        })
                );
                break;
            case GAME_OBJECT_SECOND_OPTION:
                addMenuOption(
                        entry,
                        "(Nearby) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("OBJECT", id, 1, packedWP);
                            TileObjectEx object = getObject(id, packedWP);
                            if(object == null)
                                return;
                            TileObjectAPI.interact(object, 1);
                        })
                );
                break;
            case GAME_OBJECT_THIRD_OPTION:
                addMenuOption(
                        entry,
                        "(Nearby) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("OBJECT", id, 2, packedWP);
                            TileObjectEx object = getObject(id, packedWP);
                            if(object == null)
                                return;
                            TileObjectAPI.interact(object, 2);
                        })
                );
                break;
            case GAME_OBJECT_FOURTH_OPTION:
                addMenuOption(
                        entry,
                        "(Nearby) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("OBJECT", id, 3, packedWP);
                            TileObjectEx object = getObject(id, packedWP);
                            if(object == null)
                                return;
                            TileObjectAPI.interact(object, 3);
                        })
                );
                break;
            case GAME_OBJECT_FIFTH_OPTION:
                addMenuOption(
                        entry,
                        "(Nearby) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("OBJECT", id, 4, packedWP);
                            TileObjectEx object = getObject(id, packedWP);
                            if(object == null)
                                return;
                            TileObjectAPI.interact(object, 4);
                        })
                );
                break;
            case NPC_FIRST_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("NPC", id, 0);
                            NpcEx npc = getNpc(id);
                            if(npc == null)
                                return;
                            NpcAPI.interact(npc, 0);
                        })
                );
                break;
            case NPC_SECOND_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("NPC", id, 1);
                            NpcEx npc = getNpc(id);
                            if(npc == null)
                                return;
                            NpcAPI.interact(npc, 1);
                        })
                );
                break;
            case NPC_THIRD_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("NPC", id, 2);
                            NpcEx npc = getNpc(id);
                            if(npc == null)
                                return;
                            NpcAPI.interact(npc, 2);
                        })
                );
                break;
            case NPC_FOURTH_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("NPC", id, 3);
                            NpcEx npc = getNpc(id);
                            if(npc == null)
                                return;
                            NpcAPI.interact(npc, 3);
                        })
                );
                break;
            case NPC_FIFTH_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("NPC", id, 4);
                            NpcEx npc = getNpc(id);
                            if(npc == null)
                                return;
                            NpcAPI.interact(npc, 4);
                        })
                );
                break;
            case PLAYER_FIRST_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 0);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 0);
                        })
                );
                break;
            case PLAYER_SECOND_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 1);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 1);
                        })
                );
                break;
            case PLAYER_THIRD_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 2);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 2);
                        })
                );
                break;
            case PLAYER_FOURTH_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 3);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 3);
                        })
                );
                break;
            case PLAYER_FIFTH_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 4);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 4);
                        })
                );
                break;
            case PLAYER_SIXTH_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 5);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 5);
                        })
                );
                break;
            case PLAYER_SEVENTH_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 6);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 6);
                        })
                );
                break;
            case PLAYER_EIGHTH_OPTION:
                addMenuOption(
                        entry,
                        "(All) " + entry.getOption(),
                        name,
                        e -> ThreadPool.submit(() -> {
                            plugin.sendMessage("PLAYER", id, 7);
                            PlayerEx player = getPlayer(id);
                            if(player == null)
                                return;
                            PlayerAPI.interact(player, 7);
                        })
                );
                break;
        }
    }

    private static int getWorldPoint()
    {
        Client client = Static.getClient();
        WorldView wv = client.getTopLevelWorldView();
        Tile tile = wv.getSelectedSceneTile();
        if(tile == null)
            return -1;
        WorldPoint worldPoint = WorldPointUtil.get(tile.getWorldLocation());
        return WorldPointUtil.compress(worldPoint);
    }

    private static void addMenuOption(MenuEntryAdded entry, String option, String target, Consumer<MenuEntry> onClick)
    {
        Client client = Static.getClient();
        client.getMenu().createMenuEntry(getIndexOf(entry))
                .setOption(option)
                .setTarget("<col=00ff00>" + target + " ")
                .setType(MenuAction.RUNELITE)
                .onClick(onClick);
    }

    private static int getIndexOf(MenuEntryAdded entry)
    {
        Client client = Static.getClient();
        MenuEntry[] entries = client.getMenu().getMenuEntries();
        for (int i = 0; i < entries.length; i++)
        {
            if(entries[i] == entry.getMenuEntry())
            {
                return i;
            }
        }
        Logger.error("[ExtendedMenus] Failed to get menu index");
        return 1;
    }

    private static NpcEx getNpc(int index)
    {
        return new NpcQuery()
                .withIndex(index)
                .first();
    }

    private static PlayerEx getPlayer(int index)
    {
        return new PlayerQuery()
                .keepIf(p -> p.getIndex() == index)
                .first();
    }

    private static TileObjectEx getObject(int id, int worldPoint)
    {
        WorldPoint wp = WorldPointUtil.fromCompressed(worldPoint);
        return new TileObjectQuery()
                .withId(id)
                .within(wp, 1)
                .first();
    }
}

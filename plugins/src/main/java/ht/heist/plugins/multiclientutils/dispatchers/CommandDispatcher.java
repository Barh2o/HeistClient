package ht.heist.plugins.multiclientutils.dispatchers;

import ht.heist.Logger;
import ht.heist.api.entities.NpcAPI;
import ht.heist.api.entities.PlayerAPI;
import ht.heist.api.entities.TileObjectAPI;
import ht.heist.api.game.MovementAPI;
import ht.heist.api.threaded.Delays;
import ht.heist.api.game.WorldsAPI;
import ht.heist.data.wrappers.NpcEx;
import ht.heist.data.wrappers.PlayerEx;
import ht.heist.data.wrappers.TileObjectEx;
import ht.heist.plugins.multiclientutils.model.MultiMessage;
import ht.heist.queries.NpcQuery;
import ht.heist.queries.PlayerQuery;
import ht.heist.queries.TileObjectQuery;
import ht.heist.services.CatFacts;
import ht.heist.services.pathfinder.Walker;
import ht.heist.util.MessageUtil;
import ht.heist.util.ThreadPool;
import ht.heist.util.WorldPointUtil;
import lombok.Getter;
import net.runelite.api.coords.WorldPoint;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class CommandDispatcher
{
    @Getter
    private static final Map<String,Instant> players = new HashMap<>();

    public static void processSelf(MultiMessage message)
    {
        int id;
        int action;
        WorldPoint worldPoint;
        switch (message.getCommand())
        {
            case "PATHFIND":
                WorldPoint dest = WorldPointUtil.fromCompressed(message.getInt(0));
                int world = message.getInt(1);
                ThreadPool.submit(() -> {
                    WorldsAPI.hop(world).execute();
                    Delays.tick();
                    Walker.walkTo(dest);
                });
                break;
            case "WALK":
                worldPoint = WorldPointUtil.fromCompressed(message.getInt(0));
                MovementAPI.walkToWorldPoint(worldPoint);
                break;
            case "NPC":
                id = message.getInt(0);
                action = message.getInt(1);
                NpcEx npc = new NpcQuery()
                        .withIndex(id)
                        .first();
                if(npc == null)
                    break;
                NpcAPI.interact(npc, action);
                break;
            case "PLAYER":
                id = message.getInt(0);
                action = message.getInt(1);
                PlayerEx player = new PlayerQuery()
                        .keepIf(p -> p.getIndex() == id)
                        .first();
                if(player == null)
                    break;
                PlayerAPI.interact(player, action);
                break;
            case "OBJECT":
                id = message.getInt(0);
                action = message.getInt(1);
                worldPoint = WorldPointUtil.fromCompressed(message.getInt(2));
                TileObjectEx object = new TileObjectQuery()
                        .withId(id)
                        .within(worldPoint, 1)
                        .first();
                if(object == null)
                    break;
                TileObjectAPI.interact(object, action);
                break;
        }
    }

    public static void process(MultiMessage message)
    {
        PlayerEx sender = PlayerAPI.search()
                .withName(message.getSender())
                .first();
        switch (message.getCommand())
        {
            case "DESPAWN":
                players.remove(message.getSender());
                return;
            case "PING":
                players.put(message.getSender(), Instant.now());
                return;
            case "PATHFIND":
                WorldPoint dest = WorldPointUtil.fromCompressed(message.getInt(0));
                int world = message.getInt(1);
                ThreadPool.submit(() -> {
                    WorldsAPI.hop(world).execute();
                    Delays.tick();
                    Walker.walkTo(dest);
                });
                return;
            case "CATFACTS":
                String fact = CatFacts.get(60);
                MessageUtil.sendPublicChatMessage(fact);
                return;
        }

        if(sender == null)
        {
            Logger.warn("Player '" + message.getSender() + "' is unavailable.");
            return;
        }

        int id;
        int action;
        WorldPoint worldPoint;
        switch (message.getCommand())
        {
            case "FOLLOW":
                PlayerAPI.interact(sender, 2);
                return;
            case "DD":
                MovementAPI.walkToWorldPoint(sender.getWorldPoint());
                return;
            case "SCATTER":
                int x = sender.getWorldPoint().getX() - 6;
                int y = sender.getWorldPoint().getY() - 6;
                x += ThreadLocalRandom.current().nextInt(0, 13);
                y += ThreadLocalRandom.current().nextInt(0, 13);
                MovementAPI.walkToWorldPoint(x, y);
                return;
            case "WALK":
                worldPoint = WorldPointUtil.fromCompressed(message.getInt(0));
                MovementAPI.walkToWorldPoint(worldPoint);
                return;
            case "NPC":
                id = message.getInt(0);
                action = message.getInt(1);
                NpcEx npc = new NpcQuery()
                        .withIndex(id)
                        .first();
                if(npc == null)
                    break;
                NpcAPI.interact(npc, action);
                break;
            case "PLAYER":
                id = message.getInt(0);
                action = message.getInt(1);
                PlayerEx player = new PlayerQuery()
                        .keepIf(p -> p.getIndex() == id)
                        .first();
                if(player == null)
                    break;
                PlayerAPI.interact(player, action);
                break;
            case "OBJECT":
                id = message.getInt(0);
                action = message.getInt(1);
                worldPoint = WorldPointUtil.fromCompressed(message.getInt(2));
                TileObjectEx object = new TileObjectQuery()
                        .withId(id)
                        .within(worldPoint, 1)
                        .first();
                if(object == null)
                    break;
                TileObjectAPI.interact(object, action);
                break;
            default:
                Logger.error("[ExtendedMenu] Unrecognized command '" + message.getCommand() + "'");
        }
    }
}

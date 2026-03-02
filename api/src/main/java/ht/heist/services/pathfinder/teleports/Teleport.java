package ht.heist.services.pathfinder.teleports;

import ht.heist.util.handler.StepHandler;
import ht.heist.util.handler.HandlerBuilder;
import ht.heist.util.WorldPointUtil;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

import java.util.ArrayList;
import java.util.List;

@Value
public class Teleport
{
    WorldPoint destination;
    int radius;
    StepHandler handlers;

    public Teleport(WorldPoint destination, int radius, List<Runnable> handlers){
        this.destination = destination;
        this.radius = radius;
        HandlerBuilder builder = HandlerBuilder.get();
        int i = 0;
        for(Runnable handler : handlers){
            builder.add(i++, handler);
        }
        builder.addDelay(i, 3);
        this.handlers = builder.build();
    }

    public Teleport(WorldPoint destination, int radius, Runnable handler){
        this.destination = destination;
        this.radius = radius;
        HandlerBuilder builder = HandlerBuilder.get()
                .add(0, handler)
                .addDelay(1, 3);
        this.handlers = builder.build();
    }

    public Teleport(WorldPoint destination, int radius, StepHandler handler){
        this.destination = destination;
        this.radius = radius;
        this.handlers = handler;
    }

    public static List<Teleport> buildTeleportLinks()
    {
        return new ArrayList<>(TeleportLoader.buildTeleports());
    }

    public Teleport copy()
    {
        return new Teleport(
                WorldPointUtil.fromCompressed(WorldPointUtil.compress(destination)),
                radius,
                handlers
        );
    }
}

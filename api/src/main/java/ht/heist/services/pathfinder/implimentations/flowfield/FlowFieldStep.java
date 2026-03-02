package ht.heist.services.pathfinder.implimentations.flowfield;

import ht.heist.services.pathfinder.abstractions.IStep;
import ht.heist.services.pathfinder.transports.Transport;
import ht.heist.util.WorldPointUtil;
import net.runelite.api.coords.WorldPoint;

import java.util.List;

/**
 * Represents a single step in a flow field path.
 * Stores position as compressed int for memory efficiency.
 */
public class FlowFieldStep implements IStep
{
    private final int position;
    private final Transport transport;

    public FlowFieldStep(int position, Transport transport) {
        this.position = position;
        this.transport = transport;
    }

    @Override
    public WorldPoint getPosition()
    {
        List<WorldPoint> point = WorldPointUtil.toInstance(WorldPointUtil.fromCompressed(position));
        if(!point.isEmpty())
        {
            return point.get(0);
        }
        return WorldPointUtil.fromCompressed(position);
    }

    @Override
    public Transport getTransport() {
        return transport;
    }

    @Override
    public int getPackedPosition() {
        return position;
    }

    @Override
    public boolean hasTransport()
    {
        return transport != null;
    }
}

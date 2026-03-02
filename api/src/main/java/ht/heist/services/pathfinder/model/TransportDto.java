package ht.heist.services.pathfinder.model;

import ht.heist.services.pathfinder.transports.Transport;
import ht.heist.services.pathfinder.transports.TransportLoader;
import ht.heist.services.pathfinder.requirements.Requirements;
import lombok.Value;
import net.runelite.api.coords.WorldPoint;

@Value
public class TransportDto
{
    WorldPoint source;
    WorldPoint destination;
    String action;
    Integer objectId;
    Requirements requirements;

    public Transport toTransport()
    {
        return TransportLoader.objectTransport(source, destination, objectId, action, requirements);
    }
}

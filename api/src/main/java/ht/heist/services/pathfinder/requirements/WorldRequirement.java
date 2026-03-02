package ht.heist.services.pathfinder.requirements;

import ht.heist.api.game.WorldsAPI;
import lombok.Value;

@Value
public class WorldRequirement implements Requirement
{
    boolean memberWorld;

    @Override
    public Boolean get()
    {
        return !memberWorld || WorldsAPI.inMembersWorld();
    }
}

package ht.heist.services.pathfinder.requirements;

import ht.heist.api.game.SkillAPI;
import ht.heist.api.game.WorldsAPI;
import lombok.Value;
import net.runelite.api.Skill;

import java.util.Set;

@Value
public class SkillRequirement implements Requirement
{
    Skill skill;
    int level;

    @Override
    public Boolean get()
    {
        if(SkillAPI.MEMBER_SKILLS.contains(skill) && !WorldsAPI.inMembersWorld())
        {
            return false;
        }

        return SkillAPI.getLevel(skill) >= level;
    }
}


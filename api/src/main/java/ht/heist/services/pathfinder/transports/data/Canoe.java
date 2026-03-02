package ht.heist.services.pathfinder.transports.data;

import ht.heist.data.ItemConstants;
import ht.heist.services.pathfinder.requirements.ItemRequirement;
import ht.heist.services.pathfinder.requirements.Requirements;
import ht.heist.services.pathfinder.requirements.RequirementsBuilder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum Canoe
{
    LOG(1, 12, 27262996),
    DUGOUT(2, 27, 27262994),
    STABLE_DUGOUT(3, 42, 27262988),
    //WAKA(Integer.MAX_VALUE, 57, 27262990),
    ;

    private final int distance;
    private final int level;
    private final int widgetId;
    private final Requirements requirements = RequirementsBuilder.get()
            .addRequirement(new ItemRequirement(null, 1, ItemConstants.AXES))
            .build();
}

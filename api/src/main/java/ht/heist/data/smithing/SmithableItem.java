package ht.heist.data.smithing;

import ht.heist.services.pathfinder.requirements.Requirements;

/**
 * Interface for smithable items across different metals.
 */
public interface SmithableItem {
    String getDisplayName();

    int getBarCount();

    int getOutputId();

    int getOutputQuantity();

    int getInterfaceID();

    Requirements getRequirements();

    boolean canAccess();
}

package ht.heist.corejava.api;

import java.util.List;

/**
 * Path generation and traversal.
 */
public interface PathService {
    /**
     * Generates a path from the start point to the end point.
     * 
     * @param start The starting point.
     * @param end   The target destination.
     * @return A list of HeistPoints representing the path, or empty if no path
     *         found.
     */
    List<HeistPoint> generatePath(HeistPoint start, HeistPoint end);

    /**
     * Sets the current active path for the service to manage.
     * 
     * @param path the path list of points
     */
    void setActivePath(List<HeistPoint> path);

    /**
     * Retrieves the next point in the active path, or null if complete.
     */
    HeistPoint getNextWaypoint();

    /**
     * Increments the path to the next waypoint.
     */
    void advanceWaypoint();

    /**
     * @return true if there are no more waypoints in the active path
     */
    boolean isPathComplete();

    /**
     * @return the current active path, if any
     */
    List<HeistPoint> getActivePath();
}

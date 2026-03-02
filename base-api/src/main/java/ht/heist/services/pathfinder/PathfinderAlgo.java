package ht.heist.services.pathfinder;

import ht.heist.Static;

public enum PathfinderAlgo
{
    HYBRID_BFS("ht.heist.services.pathfinder.implimentations.hybridbfs.HybridBFSAlgo"),
    BI_DIR_BFS("ht.heist.services.pathfinder.implimentations.bidirbfs.BiDirBFSAlgo"),
    FLOW_FIELD("ht.heist.services.pathfinder.implimentations.flowfield.FlowFieldAlgo"),
    ASTAR("ht.heist.services.pathfinder.implimentations.astar.AStarAlgo"),
    JPS("ht.heist.services.pathfinder.implimentations.jps.JPSAlgo")
    ;

    private final String fqdn;

    PathfinderAlgo(String fqdn)
    {
        this.fqdn = fqdn;
    }

    public Class<?> getPathfinder()
    {
        try
        {
            return Static.getClassLoader().loadClass(fqdn);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException(e);
        }
    }

    public <T> T newInstance()
    {
        try
        {
            return (T) getPathfinder().getDeclaredConstructor().newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}

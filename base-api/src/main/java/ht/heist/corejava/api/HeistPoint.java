package ht.heist.corejava.api;

/**
 * Pure-Java representation of a 3D coordinate in the game world.
 */
public class HeistPoint {
    public final int x;
    public final int y;
    public final int plane;

    public HeistPoint(int x, int y, int plane) {
        this.x = x;
        this.y = y;
        this.plane = plane;
    }

    public int distanceTo(HeistPoint other) {
        if (this.plane != other.plane)
            return Integer.MAX_VALUE;
        return Math.max(Math.abs(this.x - other.x), Math.abs(this.y - other.y));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        HeistPoint that = (HeistPoint) o;
        return x == that.x && y == that.y && plane == that.plane;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(x, y, plane);
    }
}

package Model.QuadTree.Data;
import Model.DynamicHashFile.Data.IRecord;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;

public abstract class QuadTreeLog implements IData {
    protected long id;
    protected Coordinate[] coordinates;

    public QuadTreeLog(long id, Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.id = id;
        Coordinate[] coordinates = CoordinateComputer.normalizeCoordinates(minCoordinate, maxCoordinate);
        this.coordinates = new Coordinate[]{ coordinates[0], coordinates[1]};
    }

    public long getId() {
        return id;
    }

    @Override
    public boolean equals(IData other) {
        return this.compare(other) == 1;
    }

    @Override
    public Coordinate[] getCoordinates() {
        return coordinates;
    }

    @Override
    public void setCoordinates(Coordinate[] coordinates) { if (coordinates.length == 2) this.coordinates = coordinates; }

    public boolean equals(IRecord other) {
        if (other instanceof IData) {
            return this.equals((IData) other);
        }
        return false;
    }

    @Override
    public void edit(IData other) {

    }

}

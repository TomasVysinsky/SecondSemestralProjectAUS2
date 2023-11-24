package Model.QuadTree.Data;

import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;

public abstract class Log implements IData {
    protected long id;
    protected String description;
    protected Coordinate[] coordinates;

    public Log(long id, String description, Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.id = id;
        this.description = description;
        Coordinate[] coordinates = CoordinateComputer.normalizeCoordinates(minCoordinate, maxCoordinate);
        this.coordinates = new Coordinate[]{ coordinates[0], coordinates[1]};
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        return description;
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

    /**
     * pomocna metoda pre overridnute metody setUp
     * @param other
     * @return true ak
     */
    protected boolean editLog(Log other) {
        if (other != null) {
            if (this.equals(other)) {
                this.description = other.description;
                return true;
            }
        }
        return false;
    }


    /**
     *
     * @param property
     * @return ci sa nehnutelnost vlozila alebo nie
     */
    public abstract boolean addProperty(Log property);
    public abstract void removeProperty(Log property);

    /**
     *
     * @return
     */
    public abstract String getFullDescription();
}

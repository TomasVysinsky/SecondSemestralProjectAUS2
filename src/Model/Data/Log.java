package Model.Data;

import Model.DynamicHashFile.Data.IRecord;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;
import Model.QuadTree.Data.IData;

import java.nio.ByteBuffer;
import java.util.BitSet;

public abstract class Log implements IData {
    protected long id;
    protected char[] description;
    protected int numberOfValidChars;
    protected Coordinate[] coordinates;
    //protected static final int coordinateArraySize = 2;

    public Log(long id, String newDescription, int descriptionSize, Coordinate minCoordinate, Coordinate maxCoordinate) {
        this.id = id;
        this.description = new char[descriptionSize];
        if (newDescription.length() > descriptionSize) {
            this.description = newDescription.substring(0, descriptionSize).toCharArray();
            this.numberOfValidChars = descriptionSize;
        } else {
            for (int i = 0; i < newDescription.length(); i++) {
                this.description[i] = newDescription.charAt(i);
            }
            this.numberOfValidChars = newDescription.length();
        }
        Coordinate[] coordinates = CoordinateComputer.normalizeCoordinates(minCoordinate, maxCoordinate);
        this.coordinates = new Coordinate[]{ coordinates[0], coordinates[1]};
    }

    public long getId() {
        return id;
    }

    public String getDescription() {
        String value = "";
        for (int i = 0; i < this.description.length; i++) {
            if (this.description[i] == '\u0000')
                break;
            value += this.description[i];
        }
        return value;
//        return String.valueOf(this.description);
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
    public BitSet getHash() {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(0, this.id);
        return BitSet.valueOf(buffer.array());
    }

    /**
     * pomocna metoda pre overridnute metody setUp
     * @param other
     * @return true ak
     */
    protected boolean editLog(Log other) {
        if (other != null) {
            if (this.equals(other)) {
                // TODO check ci to takto mozem urobit
                this.description = other.description;
                this.numberOfValidChars = other.numberOfValidChars;
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

package Model.Data;

import Model.DynamicHashFile.Data.IRecord;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Data.IData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Parcel extends Log {
    private long[] buildings;
    private int validBuildings;

    public Parcel() {
        super(0, "", 11, new Coordinate(), new Coordinate());
        this.buildings = new long[5];
        this.validBuildings = 0;
    }

    public Parcel(long id, String description, Coordinate coordinate1, Coordinate coordinate2) {
        super(id, description, 11, coordinate1, coordinate2);
        this.buildings = new long[5];
    }

    /**
     *
     * @param other
     * @return 1 ak je to parcel a id sa rovnaju, 2 ak je to Model.Data.Log a id sa rovnaju. 0 ak sa nedaju porovnat.
     */
    @Override
    public int compare(IData other) {
        int[] cmpMin = super.coordinates[0].position(other.getCoordinates()[0]);
        int[] cmpMax = super.coordinates[1].position(other.getCoordinates()[1]);
        if (cmpMin[0] == 0 && cmpMin[1] == 0 && cmpMax[0] == 0 && cmpMax[1] == 0) {
            if (other instanceof Log) {
                if (super.id == ((Log) other).id) {
                    if (other instanceof Parcel) {
                        return 1;
                    }
                    return 2;
                }
            }
        }
        return 0;
    }

    @Override
    public void edit(IData other) {
        if (other != null) {
            if (other instanceof Parcel) {
                super.editLog((Log) other);
            }
        }
    }

    @Override
    public boolean addProperty(Log property) {
        if (property instanceof Building) {
            if (this.validBuildings < this.buildings.length) {
                this.buildings[this.validBuildings] = property.id;
                this.validBuildings++;
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeProperty(Log property) {
        if (property instanceof Building) {
            Building parcel = (Building) property;
            for (int i = 0; i < this.validBuildings; i++) {
                if (this.buildings[i] == parcel.id) {
                    this.validBuildings--;
                    this.buildings[i] = this.buildings[this.validBuildings];
                    break;
                }
            }
        }
    }

    @Override
    public String getFullDescription() {
        return "Parcel: " + this.id + " Description: " + this.getDescription();
    }

    @Override
    public boolean isInstanceOfSame(IRecord other) {
        return other instanceof Parcel;
    }

    @Override
    public int getSize() {
        return Long.BYTES + Character.BYTES * this.description.length + Integer.BYTES +
                Coordinate.getSize() * this.coordinates.length + Long.BYTES * this.buildings.length +
                Integer.BYTES;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getSize());
        byteBuffer.putLong(this.id);
        for (char c : this.description) {
            byteBuffer.putChar(c);
        }
        byteBuffer.putInt(this.numberOfValidChars);
        for (Coordinate coordnate : this.coordinates) {
            byteBuffer.put(coordnate.toByteArray());
        }
        for (long parcel : this.buildings) {
            byteBuffer.putLong(parcel);
        }
        byteBuffer.putInt(this.validBuildings);

        return byteBuffer.array();
    }

    @Override
    public void fromByteArray(byte[] bytes) {
        if (bytes.length == this.getSize()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            this.id = byteBuffer.getLong();
            for (int i = 0; i < this.description.length; i++) {
                this.description[i] = byteBuffer.getChar();
            }
            this.numberOfValidChars = byteBuffer.getInt();
            for (int i = 0; i < this.coordinates.length; i++) {
                byte[] newArray = new byte[Coordinate.getSize()];
                for (int j = 0; j < newArray.length; j++) {
                    newArray[j] = byteBuffer.get();
                }
                this.coordinates[i].fromByteArray(newArray);
            }
            for (int i = 0; i < this.buildings.length; i++) {
                this.buildings[i] = byteBuffer.getLong();
            }
            this.validBuildings = byteBuffer.getInt();
            return;
        }
        throw new ArrayIndexOutOfBoundsException("Pole ma inu dlzku");
    }
}

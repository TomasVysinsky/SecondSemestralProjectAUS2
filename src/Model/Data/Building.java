package Model.Data;

import Model.DynamicHashFile.Data.IRecord;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;
import Model.QuadTree.Data.IData;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

public class Building extends Log {
    private long[] parcels;
    private int supisneCislo, validParcels;
    //private static final int descriptionSize = 15, parcelArraySize = 6;

    public Building() {
        super(0, "", 15, new Coordinate(), new Coordinate());
        this.parcels = new long[6];
        this.supisneCislo = 0;
        this.validParcels = 0;
    }

    public Building(long id, int supisneCislo, String description, Coordinate coordinate1, Coordinate coordinate2) {
        super(id, description, 15, coordinate1, coordinate2);
        this.parcels = new long[6];
        this.supisneCislo = supisneCislo;
        this.validParcels = 0;
    }

    public long[] getParcels() {
        return parcels;
    }

    public int getValidParcels() {
        return validParcels;
    }

    /**
     *
     * @param other
     * @return 1 ak je to budova a id sa rovnaju, 2 ak je to Model.Data.Log a id sa rovnaju. 0 ak sa nedaju porovnat.
     */
    @Override
    public int compare(IData other) {
        int[] cmpMin = super.coordinates[0].position(other.getCoordinates()[0]);
        int[] cmpMax = super.coordinates[1].position(other.getCoordinates()[1]);
        if (cmpMin[0] == 0 && cmpMin[1] == 0 && cmpMax[0] == 0 && cmpMax[1] == 0) {
            if (other instanceof Log) {
                if (super.id == ((Log) other).id) {
                    if (other instanceof Building) {
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
            if (other instanceof Building) {
                if (super.editLog((Log) other))
                    this.supisneCislo = ((Building) other).supisneCislo;
            }
        }
    }

    @Override
    public boolean addProperty(Log property) {
        if (property instanceof Parcel) {
            if (this.validParcels < this.parcels.length) {
                this.parcels[this.validParcels] = property.id;
                this.validParcels++;
                return true;
            }
        }
        return false;
    }

    @Override
    public void removeProperty(Log property) {
        if (property instanceof Parcel) {
            Parcel parcel = (Parcel) property;
            for (int i = 0; i < this.validParcels; i++) {
                if (this.parcels[i] == parcel.id) {
                    this.validParcels--;
                    this.parcels[i] = this.parcels[this.validParcels];
                    break;
                }
            }
        }
    }

    @Override
    public String getFullDescription() {
        return "Building: " + this.id + " Supisne cislo: " + this.supisneCislo + " Description: " + this.getDescription();
    }

    @Override
    public boolean isInstanceOfSame(IRecord other) {
        return other instanceof Building;
    }

    public int getSupisneCislo() {
        return supisneCislo;
    }

    @Override
    public int getSize() {
        return Long.BYTES + Character.BYTES * this.description.length + Integer.BYTES +
                Coordinate.getSize() * this.coordinates.length + Long.BYTES * this.parcels.length +
                Integer.BYTES * 2;
    }

    @Override
    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getSize());
        byteBuffer.putLong(this.id);
        for (int i = 0; i < this.description.length; i++) {
            byteBuffer.putChar(this.description[i]);
        }
        byteBuffer.putInt(this.numberOfValidChars);
        for (Coordinate coordnate : this.coordinates) {
            byteBuffer.put(coordnate.toByteArray());
        }
        for (long parcel : this.parcels) {
            byteBuffer.putLong(parcel);
        }
        byteBuffer.putInt(this.supisneCislo);
        byteBuffer.putInt(this.validParcels);

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
            for (int i = 0; i < this.parcels.length; i++) {
                this.parcels[i] = byteBuffer.getLong();
            }
            this.supisneCislo = byteBuffer.getInt();
            this.validParcels = byteBuffer.getInt();
            return;
        }
        throw new ArrayIndexOutOfBoundsException("Pole ma inu dlzku");
    }
}

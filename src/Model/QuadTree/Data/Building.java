package Model.QuadTree.Data;

import Model.QuadTree.Coordinates.Coordinate;

import java.util.ArrayList;

public class Building extends Log {
    public ArrayList<Parcel> parcels;
    int supisneCislo;

    public Building(long id, int supisneCislo, String description, Coordinate coordinate1, Coordinate coordinate2) {
        super(id, description, coordinate1, coordinate2);
        this.parcels = new ArrayList<Parcel>();
        this.supisneCislo = supisneCislo;
    }

    /**
     *
     * @param other
     * @return 1 ak je to budova a id sa rovnaju, 2 ak je to Model.QuadTree.Data.Log a id sa rovnaju. 0 ak sa nedaju porovnat.
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
            this.parcels.add((Parcel) property);
            return true;
        }
        return false;
    }

    @Override
    public void removeProperty(Log property) {
        if (property instanceof Parcel) {
            this.parcels.remove(property);
        }
    }

    @Override
    public String getFullDescription() {
        return "Building: " + this.supisneCislo + " Description: " + this.description;
    }

    public int getSupisneCislo() {
        return supisneCislo;
    }
}

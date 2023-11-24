package Model.QuadTree.Data;

import Model.QuadTree.Coordinates.Coordinate;

import java.util.ArrayList;

public class Parcel extends Log {
    public ArrayList<Building> buildings;
    int cisloParcely;

    public Parcel(long id, int cisloParcely, String description, Coordinate coordinate1, Coordinate coordinate2) {
        super(id, description, coordinate1, coordinate2);
        this.buildings = new ArrayList<Building>();
        this.cisloParcely = cisloParcely;
    }

    /**
     *
     * @param other
     * @return 1 ak je to parcel a id sa rovnaju, 2 ak je to Model.QuadTree.Data.Log a id sa rovnaju. 0 ak sa nedaju porovnat.
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
                if (super.editLog((Log) other))
                    this.cisloParcely = ((Parcel) other).cisloParcely;
            }
        }
    }

    @Override
    public boolean addProperty(Log property) {
        if (property instanceof Building) {
            this.buildings.add((Building) property);
            return true;
        }
        return false;
    }

    @Override
    public void removeProperty(Log property) {
        if (property instanceof Building) {
            this.buildings.remove(property);
        }
    }

    @Override
    public String getFullDescription() {
        return "Parcel: " + this.cisloParcely + " Description: " + this.description;
    }

    public int getCisloParcely() {
        return cisloParcely;
    }
}

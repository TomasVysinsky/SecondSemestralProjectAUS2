package Model.QuadTree.Data;

import Model.DynamicHashFile.Data.IRecord;
import Model.QuadTree.Coordinates.Coordinate;

public interface IData {
    public int compare(IData other);

    public boolean equals(IData other);
    public Coordinate[] getCoordinates();
    public void setCoordinates(Coordinate[] coordinates);
    public void edit(IData other);
}

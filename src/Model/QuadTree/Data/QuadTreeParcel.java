package Model.QuadTree.Data;
import Model.QuadTree.Coordinates.Coordinate;

public class QuadTreeParcel extends QuadTreeLog{
    public QuadTreeParcel(long id, Coordinate minCoordinate, Coordinate maxCoordinate) {
        super(id, minCoordinate, maxCoordinate);
    }

    @Override
    public int compare(IData other) {
        int[] cmpMin = super.coordinates[0].position(other.getCoordinates()[0]);
        int[] cmpMax = super.coordinates[1].position(other.getCoordinates()[1]);
        if (cmpMin[0] == 0 && cmpMin[1] == 0 && cmpMax[0] == 0 && cmpMax[1] == 0) {
            if (other instanceof QuadTreeLog) {
                if (super.id == ((QuadTreeLog) other).id) {
                    if (other instanceof QuadTreeParcel) {
                        return 1;
                    }
                    return 2;
                }
            }
        }
        return 0;
    }
}

package Generator;

import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Data.Parcel;

import java.util.ArrayList;

public class ParcelGenerator extends dataGenerator<Parcel> {

    public ParcelGenerator() {}

    /*public ArrayList<Parcel> generateData(Coordinate minCoordinate, Coordinate maxCoordinate, int numberOfInstances, long firstId) {
        return super.generateData(minCoordinate, maxCoordinate, numberOfInstances, firstId);
    }*/

    @Override
    public Parcel createInstance(Coordinate minCoordinate, Coordinate maxCoordinate, long id) {
        int cisloParcelu = Math.abs(this.random.nextInt());
        return new Parcel(id, cisloParcelu, "Parcel number " + cisloParcelu,
                this.generateBetween(minCoordinate, maxCoordinate), this.generateBetween(minCoordinate, maxCoordinate));
    }

}

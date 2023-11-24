package Generator;

import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;

import java.util.ArrayList;
import java.util.Random;

public abstract class dataGenerator<T> {

    protected Random random;

    public dataGenerator() {
        this.random = new Random();
    }

    /**
     * V
     * @param minCoordinate
     * @param maxCoordinate
     * @return
     */
    public ArrayList<T> generateData(Coordinate minCoordinate, Coordinate maxCoordinate, int numberOfInstances, long firstId) {
        ArrayList<T> generatedInstances = new ArrayList<>();
        for (int i = 0; i < numberOfInstances; i++) {
            generatedInstances.add(this.createInstance(minCoordinate, maxCoordinate, firstId));
            firstId++;
        }
        return generatedInstances;
    }

    public abstract T createInstance(Coordinate minCoordinate, Coordinate maxCoordinate, long id);


    /**
     * generuje suradnicu zo zadaneho priestoru
     * @param minCoordinate
     * @param maxCoordinate
     * @return
     */
    protected Coordinate generateBetween(Coordinate minCoordinate, Coordinate maxCoordinate) {
        Width minWidth = minCoordinate.getWidth(), maxWidth = maxCoordinate.getWidth();
        double cMinWidthPosition = minCoordinate.getWidthPosition(), cMaxWidthPosition = maxCoordinate.getWidthPosition();
        Length minLength = minCoordinate.getLength(), maxLength = maxCoordinate.getLength();
        double cMinLengthPosition = minCoordinate.getLengthPosition(), cMaxLengthPosition = maxCoordinate.getLengthPosition();

        if (minWidth != maxWidth) {
            // maxWidth = minWidth;
            cMaxWidthPosition = cMaxWidthPosition * -1;
        }
        if  (minLength != maxLength) {
            // maxLength = minLength;
            cMaxLengthPosition = cMaxLengthPosition * -1;
        }
        double minWidthPosition = Math.min(cMinWidthPosition, cMaxWidthPosition),
                maxWidthPosition = Math.max(cMinWidthPosition, cMaxWidthPosition),
                minLengthPosition = Math.min(cMinLengthPosition, cMaxLengthPosition),
                maxLengthPosition = Math.max(cMinLengthPosition, cMaxLengthPosition);

        double widthPosition = minWidthPosition + random.nextDouble() * (maxWidthPosition - minWidthPosition);
        double lengthPosition = minLengthPosition + random.nextDouble() * (maxLengthPosition - minLengthPosition);
        return new Coordinate(minWidth, widthPosition, minLength, lengthPosition);
    }
}

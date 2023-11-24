package Model.QuadTree.Coordinates;

public class CoordinateComputer {
    /**
     *
     * @param minCoordinate
     * @param maxCoordinate
     * @return pole suradnic upravene tak, aby na indexe 0 bol lavy horny roh a na indexe 1 pravy dolny
     */
    public static Coordinate[] normalizeCoordinates(Coordinate minCoordinate, Coordinate maxCoordinate) {
        int[] cmpCoordinates = minCoordinate.position(maxCoordinate);

        if (cmpCoordinates[0] == 1) {
            if (cmpCoordinates[1] == -1) {
                Coordinate tmp = maxCoordinate;
                maxCoordinate = minCoordinate;
                minCoordinate = tmp;
            } else {
                Coordinate tmp = new Coordinate(maxCoordinate.getWidth(), maxCoordinate.getWidthPosition(),
                        minCoordinate.getLength(), minCoordinate.getLengthPosition());
                maxCoordinate = new Coordinate(minCoordinate.getWidth(), minCoordinate.getWidthPosition(),
                        maxCoordinate.getLength(), maxCoordinate.getLengthPosition());
                minCoordinate = tmp;
            }
        } else if (cmpCoordinates[1] == -1) {
            Coordinate tmp = new Coordinate(minCoordinate.getWidth(), minCoordinate.getWidthPosition(),
                    maxCoordinate.getLength(), maxCoordinate.getLengthPosition());
            maxCoordinate = new Coordinate(maxCoordinate.getWidth(), maxCoordinate.getWidthPosition(),
                    minCoordinate.getLength(), minCoordinate.getLengthPosition());
            minCoordinate = tmp;
        }

        return new Coordinate[]{ minCoordinate, maxCoordinate };
    }

    public static Coordinate[] invertCoordinates(Coordinate minCoordinate, Coordinate maxCoordinate) {
        int[] cmpCoordinates = minCoordinate.position(maxCoordinate);

        if (cmpCoordinates[0] == 1) {
            if (cmpCoordinates[1] == 1) {
                Coordinate tmp = maxCoordinate;
                maxCoordinate = minCoordinate;
                minCoordinate = tmp;
            } else {
                Coordinate tmp = new Coordinate(maxCoordinate.getWidth(), maxCoordinate.getWidthPosition(),
                        minCoordinate.getLength(), minCoordinate.getLengthPosition());
                maxCoordinate = new Coordinate(minCoordinate.getWidth(), minCoordinate.getWidthPosition(),
                        maxCoordinate.getLength(), maxCoordinate.getLengthPosition());
                minCoordinate = tmp;
            }
        } else if (cmpCoordinates[1] == 1) {
            Coordinate tmp = new Coordinate(minCoordinate.getWidth(), minCoordinate.getWidthPosition(),
                    maxCoordinate.getLength(), maxCoordinate.getLengthPosition());
            maxCoordinate = new Coordinate(maxCoordinate.getWidth(), maxCoordinate.getWidthPosition(),
                    minCoordinate.getLength(), minCoordinate.getLengthPosition());
            minCoordinate = tmp;
        }

        return new Coordinate[]{ minCoordinate, maxCoordinate };
    }

    /**
     * Vrati ci sa premenna cmpCoordinate nachadza v obdlzniku urcenom suradnicami minCoordinate a maxCoordinate.
     * @param minCoordinate
     * @param maxCoordinate
     * @param cmpCoordinate
     * @return ci sa premenna cmpCoordinate nachadza v obdlzniku urcenom suradnicami minCoordinate a maxCoordinate
     */
    public static boolean containsCoordinate(Coordinate minCoordinate, Coordinate maxCoordinate, Coordinate cmpCoordinate) {
        Coordinate[] coordinates = CoordinateComputer.normalizeCoordinates(minCoordinate, maxCoordinate);
        int[] cmpMin = coordinates[0].position(cmpCoordinate);
        int[] cmpMax = coordinates[1].position(cmpCoordinate);
        return cmpMin[0] == -1 && cmpMin[1] == 1 && cmpMax[0] == 1 && cmpMax[1] == -1;
    }

    /**
     * Vrati ci sa premenna cmpCoordinate nachadza v obdlzniku urcenom suradnicami minCoordinate a maxCoordinate vratane hranic.
     * @param minCoordinate
     * @param maxCoordinate
     * @param cmpCoordinate
     * @return ci sa premenna cmpCoordinate nachadza v obdlzniku urcenom suradnicami minCoordinate a maxCoordinate
     */
    public static boolean containsCoordinateInclBorderPoints(Coordinate minCoordinate, Coordinate maxCoordinate, Coordinate cmpCoordinate) {
        Coordinate[] coordinates = CoordinateComputer.normalizeCoordinates(minCoordinate, maxCoordinate);
        int[] cmpMin = coordinates[0].position(cmpCoordinate);
        int[] cmpMax = coordinates[1].position(cmpCoordinate);
        return (cmpMin[0] == -1 || cmpMin[0] == 0) && (cmpMin[1] == 1 || cmpMin[1] == 0) &&
                (cmpMax[0] == 1 || cmpMax[0] == 0) && (cmpMax[1] == -1 || cmpMax[1] == 0);
    }
}

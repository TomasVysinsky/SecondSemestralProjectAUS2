package Generator;

import Model.QuadTree.Coordinates.Coordinate;
import Model.Data.Building;

public class BuildingGenerator extends dataGenerator<Building> {

    public BuildingGenerator() {}

    @Override
    public Building createInstance(Coordinate minCoordinate, Coordinate maxCoordinate, long id) {
        int supisneCislo = Math.abs(this.random.nextInt());
        return new Building(id, supisneCislo, "Supisne cislo " + supisneCislo,
                this.generateBetween(minCoordinate, maxCoordinate), this.generateBetween(minCoordinate, maxCoordinate));
    }
}

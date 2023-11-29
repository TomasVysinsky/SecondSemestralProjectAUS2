package Model;

import Generator.BuildingGenerator;
import Generator.ParcelGenerator;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;
import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;
import Model.Data.Building;
import Model.QuadTree.Data.IData;
import Model.Data.Log;
import Model.Data.Parcel;
import Model.QuadTree.QuadTree;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Model {
    private QuadTree buildings, parcels, allProperties;
    private long currentBuildingID, currentParcelID;
    private BuildingGenerator bGenerator;
    private ParcelGenerator pGenerator;

    public Model(int maxDepth,
                 Width minWidth, double minWidthPosition, Length minLength, double minLengthPosition,
                 Width maxWidth, double maxWidthPosition, Length maxLength, double maxLengthPosition) {
        this.buildings = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.parcels = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.allProperties = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.currentBuildingID = Long.MIN_VALUE;
        this.currentParcelID = Long.MIN_VALUE;
        this.bGenerator = new BuildingGenerator();
        this.pGenerator = new ParcelGenerator();
    }

    public void initializeNewQuadTrees(int maxDepth,
                                       Width minWidth, double minWidthPosition, Length minLength, double minLengthPosition,
                                       Width maxWidth, double maxWidthPosition, Length maxLength, double maxLengthPosition) {
        this.buildings = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.parcels = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.allProperties = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.currentBuildingID = Long.MIN_VALUE;
        this.currentParcelID = Long.MIN_VALUE;
    }

    public boolean insertBuilding(int supisneCislo, String description, Coordinate minCoordinate, Coordinate maxCoordinate) {
        Building newBuilding = new Building(this.currentBuildingID, supisneCislo, description, minCoordinate, maxCoordinate);
        return this.insertBuilding(newBuilding);
    }

    public boolean insertBuilding(Building newBuilding) {
        if (!this.buildings.insert(newBuilding))
            return false;
        if (!this.allProperties.insert(newBuilding)) {
            this.buildings.delete(newBuilding);
            return false;
        }

        ArrayList<IData> parcely = this.parcels.find(newBuilding.getCoordinates()[0], newBuilding.getCoordinates()[1]);
        for (IData data : parcely) {
            ((Parcel) data).addProperty(newBuilding);
            newBuilding.addProperty((Parcel) data);
        }

        this.currentBuildingID++;
        return true;
    }

    public boolean insertParcel(int cisloParcely, String description, Coordinate minCoordinate, Coordinate maxCoordinate) {
        Parcel newParcel = new Parcel(this.currentBuildingID, cisloParcely, description, minCoordinate, maxCoordinate);
        return this.insertParcel(newParcel);
    }

    public boolean insertParcel(Parcel newParcel) {
        if (!this.parcels.insert(newParcel))
            return false;
        if (!this.allProperties.insert(newParcel)) {
            this.parcels.delete(newParcel);
            return false;
        }

        ArrayList<IData> budovy = this.buildings.find(newParcel.getCoordinates()[0], newParcel.getCoordinates()[1]);
        for (IData data : budovy) {
            ((Building) data).addProperty(newParcel);
            newParcel.addProperty((Building) data);
        }

        this.currentBuildingID++;
        return true;
    }

    public ArrayList<Log> findBuildingsAtCoordinate(Coordinate coordinate) {
        ArrayList<IData> dataList = this.buildings.find(coordinate);
        if (dataList == null)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < dataList.size(); i++)
            finalDataList.add((Log) dataList.get(i));
        return finalDataList;
    }

    public ArrayList<Log> findBuildingsAtField(Coordinate minCoordinate, Coordinate maxCoordinate) {
        ArrayList<IData> dataList = this.buildings.find(minCoordinate, maxCoordinate);
        if (dataList == null)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < dataList.size(); i++)
            finalDataList.add((Log) dataList.get(i));
        return finalDataList;
    }

    public ArrayList<Log> findParcelsAtCoordinate(Coordinate coordinate) {
        ArrayList<IData> dataList = this.parcels.find(coordinate);
        if (dataList == null)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < dataList.size(); i++)
            finalDataList.add((Log) dataList.get(i));
        return finalDataList;
    }

    public ArrayList<Log> findParcelsAtField(Coordinate minCoordinate, Coordinate maxCoordinate) {
        ArrayList<IData> dataList = this.parcels.find(minCoordinate, maxCoordinate);
        if (dataList == null)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < dataList.size(); i++)
            finalDataList.add((Log) dataList.get(i));
        return finalDataList;
    }

    public ArrayList<Log> findPropertiesAtCoordinate(Coordinate coordinate) {
        ArrayList<IData> dataList = this.allProperties.find(coordinate);
        if (dataList == null)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>(dataList.size());
        for (int i = 0; i < dataList.size(); i++)
            finalDataList.add((Log) dataList.get(i));
        return finalDataList;
    }

    public ArrayList<Log> findPropertiesAtField(Coordinate minCoordinate, Coordinate maxCoordinate) {
        ArrayList<IData> dataList = this.allProperties.find(minCoordinate, maxCoordinate);
        if (dataList == null)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>(dataList.size());
        for (int i = 0; i < dataList.size(); i++)
            finalDataList.add((Log) dataList.get(i));
        return finalDataList;
    }

    public ArrayList<Log> findAllProperties() {
        ArrayList<IData> dataList = this.allProperties.getAllData();
        if (dataList == null)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>(dataList.size());
        for (int i = 0; i < dataList.size(); i++)
            finalDataList.add((Log) dataList.get(i));
        return finalDataList;
    }

    public void remove(Log data) {
        if (data == null)
            return;

        this.allProperties.delete(data);
        if (data instanceof Building) {
            ArrayList<Log> foundParcels = this.findParcelsAtField(data.getCoordinates()[0], data.getCoordinates()[1]);
            for (Log current : foundParcels) {
                current.removeProperty(current);
            }
            this.buildings.delete(data);

        } else if (data instanceof Parcel) {
            ArrayList<Log> foundBuildings = this.findBuildingsAtField(data.getCoordinates()[0], data.getCoordinates()[1]);
            for (Log current : foundBuildings) {
                current.removeProperty(current);
            }
            this.parcels.delete(data);
        }
    }

    public boolean generateBuildings(int numberOfBuildings) {
        ArrayList<Building> newBuildings = this.bGenerator.generateData(this.buildings.getCoordinates()[0], this.buildings.getCoordinates()[1], numberOfBuildings, this.currentBuildingID);
        for (Building current : newBuildings)
            if (!this.insertBuilding(current))
                return false;
        return true;
    }

    public boolean generateParcels(int numberOfParcels) {
        ArrayList<Parcel> newParcels = this.pGenerator.generateData(this.parcels.getCoordinates()[0], this.parcels.getCoordinates()[1], numberOfParcels, this.currentParcelID);
        for (Parcel current : newParcels)
            if (!this.insertParcel(current))
                return false;
        return true;
    }

    public boolean saveInFile(String fileName) {
        try {
            FileWriter fileWriter = new FileWriter(fileName);
            ArrayList<IData> dataList = this.allProperties.getAllData();
            fileWriter.write("Tree," + this.allProperties.getMaxDepth() + ',' +
                    this.widthToChar(this.allProperties.getCoordinates()[0].getWidth()) + ',' + this.allProperties.getCoordinates()[0].getWidthPosition() + ',' +
                    this.lengthToChar(this.allProperties.getCoordinates()[0].getLength()) + ',' + this.allProperties.getCoordinates()[0].getLengthPosition() + ',' +
                    this.widthToChar(this.allProperties.getCoordinates()[1].getWidth()) + ',' + this.allProperties.getCoordinates()[1].getWidthPosition() + ',' +
                    this.lengthToChar(this.allProperties.getCoordinates()[1].getLength()) + ',' + this.allProperties.getCoordinates()[1].getLengthPosition() + "\n");

            for (IData data : dataList) {
                if (data instanceof Building) {
                    Building building = (Building) data;
                    fileWriter.write("Building," + building.getId() + ',' + building.getSupisneCislo() + ',' + building.getDescription() + ',' +
                            this.widthToChar(building.getCoordinates()[0].getWidth()) + ',' + building.getCoordinates()[0].getWidthPosition() + ',' +
                            this.lengthToChar(building.getCoordinates()[0].getLength()) + ',' + building.getCoordinates()[0].getLengthPosition() + ',' +
                            this.widthToChar(building.getCoordinates()[1].getWidth()) + ',' + building.getCoordinates()[1].getWidthPosition() + ',' +
                            this.lengthToChar(building.getCoordinates()[1].getLength()) + ',' + building.getCoordinates()[1].getLengthPosition() + "\n");
                } else if (data instanceof Parcel) {
                    Parcel parcel = (Parcel) data;
                    fileWriter.write("Parcel," + parcel.getId() + ',' + parcel.getCisloParcely() + ',' + parcel.getDescription() + ',' +
                            this.widthToChar(parcel.getCoordinates()[0].getWidth()) + ',' + parcel.getCoordinates()[0].getWidthPosition() + ',' +
                            this.lengthToChar(parcel.getCoordinates()[0].getLength()) + ',' + parcel.getCoordinates()[0].getLengthPosition() + ',' +
                            this.widthToChar(parcel.getCoordinates()[1].getWidth()) + ',' + parcel.getCoordinates()[1].getWidthPosition() + ',' +
                            this.lengthToChar(parcel.getCoordinates()[1].getLength()) + ',' + parcel.getCoordinates()[1].getLengthPosition() + "\n");
                }
            }

            fileWriter.close();
        } catch (Exception e) {
            System.out.println("Error occured while writing into the file.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean loadFromFile(String fileName) {
        try {
            FileReader fileReader = new FileReader(fileName);
            BufferedReader buffer = new BufferedReader(fileReader);
            String line = buffer.readLine();
            if (line != null) {
                ArrayList<String> lineWords = new ArrayList<>(Arrays.asList(line.split(",")));
                Coordinate minCoordinate = new Coordinate(this.charToWidth(lineWords.get(2).charAt(0)), Double.parseDouble(lineWords.get(3)),
                        this.charToLength(lineWords.get(4).charAt(0)), Double.parseDouble(lineWords.get(5)));
                Coordinate maxCoordinate = new Coordinate(this.charToWidth(lineWords.get(6).charAt(0)), Double.parseDouble(lineWords.get(7)),
                        this.charToLength(lineWords.get(8).charAt(0)), Double.parseDouble(lineWords.get(9)));
                if (CoordinateComputer.containsCoordinateInclBorderPoints(this.allProperties.getCoordinates()[0], this.allProperties.getCoordinates()[1], minCoordinate) &&
                        CoordinateComputer.containsCoordinateInclBorderPoints(this.allProperties.getCoordinates()[0], this.allProperties.getCoordinates()[1], maxCoordinate)) {
                    while ((line = buffer.readLine()) != null) {
                        lineWords.clear();
                        lineWords.addAll(Arrays.asList(line.split(",")));
                        minCoordinate = new Coordinate(this.charToWidth(lineWords.get(4).charAt(0)), Double.parseDouble(lineWords.get(5)),
                                this.charToLength(lineWords.get(6).charAt(0)), Double.parseDouble(lineWords.get(7)));
                        maxCoordinate = new Coordinate(this.charToWidth(lineWords.get(8).charAt(0)), Double.parseDouble(lineWords.get(9)),
                                this.charToLength(lineWords.get(10).charAt(0)), Double.parseDouble(lineWords.get(11)));
                        if (lineWords.get(0).equals("Building")) {
                            this.insertBuilding(new Building(Long.parseLong(lineWords.get(1)),
                                    Integer.parseInt(lineWords.get(2)), lineWords.get(3), minCoordinate, maxCoordinate));
                        } else if (lineWords.get(0).equals("Parcel")){
                            this.insertParcel(new Parcel(Long.parseLong(lineWords.get(1)),
                                    Integer.parseInt(lineWords.get(2)), lineWords.get(3), minCoordinate, maxCoordinate));
                        }
                    }
                } else {
                    System.out.println("Ulozeny strom sa nenachadza v aktualnom strome.");
                    return false;
                }

            }
        } catch (Exception e) {
            System.out.println("Error occured while reading the file.");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //TODO odlahcit to o tieto metody

    private char widthToChar(Width width) {
        if (width == Width.N)
            return 'N';
        return 'S';
    }

    private char lengthToChar(Length length) {
        if (length == Length.E)
            return 'E';
        return 'W';
    }

    private Width charToWidth(char width) {
        if (width == 'N') {
            return Width.N;
        } else {
            return Width.S;
        }
    }

    private Length charToLength(char length) {
        if (length == 'E') {
            return Length.E;
        } else {
            return Length.W;
        }
    }
}

package Model;

import Generator.BuildingGenerator;
import Generator.ParcelGenerator;
import Model.DynamicHashFile.Data.Block;
import Model.DynamicHashFile.Data.IRecord;
import Model.DynamicHashFile.DynamicHashFile;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;
import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;
import Model.Data.Building;
import Model.QuadTree.Data.IData;
import Model.Data.Log;
import Model.Data.Parcel;
import Model.QuadTree.Data.QuadTreeBuilding;
import Model.QuadTree.Data.QuadTreeParcel;
import Model.QuadTree.QuadTree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;

public class Model {
    private QuadTree buildingsQuadTree, parcelsQuadTree, allPropertiesQuadTree;
    private DynamicHashFile<Parcel> parcelFile;
    private DynamicHashFile<Building> buildingFile;
    private long currentBuildingID, currentParcelID;
    private BuildingGenerator bGenerator;
    private ParcelGenerator pGenerator;
    private String fileName;

    public Model(int maxDepthQuadTree, int maxHashSize, int blockFactor, int overflowBlockFactor, String filename,
                 Width minWidth, double minWidthPosition, Length minLength, double minLengthPosition,
                 Width maxWidth, double maxWidthPosition, Length maxLength, double maxLengthPosition) {
        this.buildingsQuadTree = new QuadTree(maxDepthQuadTree, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.parcelsQuadTree = new QuadTree(maxDepthQuadTree, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.allPropertiesQuadTree = new QuadTree(maxDepthQuadTree, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.currentBuildingID = 0;
        this.currentParcelID = 0;
        this.parcelFile = new DynamicHashFile<Parcel>(blockFactor, overflowBlockFactor, maxHashSize, filename + "Parcels", Parcel.class);
        this.buildingFile = new DynamicHashFile<Building>(blockFactor, overflowBlockFactor, maxHashSize, filename + "Buildings", Building.class);

        this.bGenerator = new BuildingGenerator();
        this.pGenerator = new ParcelGenerator();
        this.fileName = filename;

        this.loadFromFile();
    }

    public void initializeNewQuadTrees(int maxDepth,
                                       Width minWidth, double minWidthPosition, Length minLength, double minLengthPosition,
                                       Width maxWidth, double maxWidthPosition, Length maxLength, double maxLengthPosition) {
        this.buildingsQuadTree = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.parcelsQuadTree = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.allPropertiesQuadTree = new QuadTree(maxDepth, minWidth, minWidthPosition, minLength, minLengthPosition,
                maxWidth, maxWidthPosition, maxLength, maxLengthPosition);
        this.currentBuildingID = 0;
        this.currentParcelID = 0;
    }

    /**
     * Z parametrov vytvori novu budovu a vlozi ju do modelu
     * @param supisneCislo
     * @param description
     * @param minCoordinate
     * @param maxCoordinate
     * @return
     */
    public boolean insertBuilding(int supisneCislo, String description, Coordinate minCoordinate, Coordinate maxCoordinate) {
        Building newBuilding = new Building(this.currentBuildingID, supisneCislo, description, minCoordinate, maxCoordinate);
        this.currentBuildingID++;
        return this.insertBuilding(newBuilding);
    }

    /**
     * Vlozi existujucu budovu do modelu pokial sa v nej nenachadza.
     * @param newBuilding
     * @return
     */
    public boolean insertBuilding(Building newBuilding) {
        // Najprv sa vlozia nevyhnutne informacie do QuadTree
        QuadTreeBuilding qtBuilding = this.convertToQTBuilding(newBuilding);
        if (!this.buildingsQuadTree.insert(qtBuilding))
            return false;
        if (!this.allPropertiesQuadTree.insert(qtBuilding)) {
            this.buildingsQuadTree.delete(qtBuilding);
            return false;
        }

        // Nasledne sa pridaju vsetky prislusne parcely
        ArrayList<IData> parcely = this.parcelsQuadTree.find(newBuilding.getCoordinates()[0], newBuilding.getCoordinates()[1]);
        ArrayList<IData> neplatne = new ArrayList<IData>();
        for (IData data : parcely) {
            Parcel foundParcel = this.convertQTParcel((QuadTreeParcel) data);
            foundParcel = this.parcelFile.find(foundParcel);
            if (foundParcel.getBuildings().length == foundParcel.getValidBuildings()) {
                neplatne.add(data);
            }
        }
        for (IData data : neplatne) {
            parcely.remove(data);
        }
        if (parcely.size() > newBuilding.getParcels().length) {
            int len = newBuilding.getParcels().length;
            for (int i = 0; i <= parcely.size() - len; i++) {
                parcely.remove(0);
            }
        }

        for (IData data : parcely) {
            if (!newBuilding.addProperty(this.convertQTParcel((QuadTreeParcel) data)))
                break;

        }

        // Zaznam sa ulozi do suboru
        if (!this.buildingFile.insert(newBuilding)) {
            System.out.println("Nieco sa pokazilo pri vkladani budovy v modeli");
            return false;
        }

        // Budova sa ulozi do prislusnych parcelov, pokial je to mozne
        for (IData data : parcely) {
            Parcel foundParcel = this.convertQTParcel((QuadTreeParcel) data);
            foundParcel = this.parcelFile.find(foundParcel);
            if (foundParcel.addProperty(newBuilding))
                this.parcelFile.edit(foundParcel);
        }

        return true;
    }

    public boolean insertParcel(String description, Coordinate minCoordinate, Coordinate maxCoordinate) {
        Parcel newParcel = new Parcel(this.currentParcelID, description, minCoordinate, maxCoordinate);
        this.currentParcelID++;
        return this.insertParcel(newParcel);
    }

    public boolean insertParcel(Parcel newParcel) {
        // Najprv sa vlozia nevyhnutne informacie do QuadTree
        QuadTreeParcel qtParcel = this.convertToQTParcel(newParcel);
        if (!this.parcelsQuadTree.insert(qtParcel))
            return false;
        if (!this.allPropertiesQuadTree.insert(qtParcel)) {
            this.parcelsQuadTree.delete(qtParcel);
            return false;
        }

        ArrayList<IData> budovy = this.buildingsQuadTree.find(newParcel.getCoordinates()[0], newParcel.getCoordinates()[1]);
        ArrayList<IData> neplatne = new ArrayList<IData>();
        for (IData data : budovy) {
            Building foundBuilding = this.convertQTBuilding((QuadTreeBuilding) data);
            foundBuilding = this.buildingFile.find(foundBuilding);
            if (foundBuilding.getParcels().length == foundBuilding.getValidParcels()) {
                neplatne.add(data);
            }
        }
        for (IData data : neplatne) {
            budovy.remove(data);
        }
        if (budovy.size() > newParcel.getBuildings().length) {
            int len = newParcel.getBuildings().length;
            for (int i = 0; i <= budovy.size() - len; i++) {
                budovy.remove(0);
            }
        }

        for (IData data : budovy) {
            if (!newParcel.addProperty(this.convertQTBuilding((QuadTreeBuilding) data)))
                break;
        }

        if (!this.parcelFile.insert(newParcel)) {
            System.out.println("Nieco sa pokazilo pri vkladani parcely v modeli");
            return false;
        }

        for (IData data : budovy) {
            Building foundBuilding = this.convertQTBuilding((QuadTreeBuilding) data);
            foundBuilding = this.buildingFile.find(foundBuilding);
            if (foundBuilding.addProperty(newParcel))
                this.buildingFile.edit(foundBuilding);
        }

        return true;
    }

    public Building findBuilding(long id) {
        return this.buildingFile.find(new Building(id, 0, "", new Coordinate(), new Coordinate()));
    }

    public ArrayList<Log> findBuildingsWithIDs(long[] ids, int validBuildings) {
        if (ids.length < validBuildings)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < validBuildings; i++)
            finalDataList.add(this.buildingFile.find(new Building(ids[i], 0, "", new Coordinate(), new Coordinate())));

        if (finalDataList.isEmpty())
            return null;
        return finalDataList;
    }

    /**
     * Najde vsetky budovy na danych suradniciach
     * @param minCoordinate
     * @param maxCoordinate
     * @return
     */
    public ArrayList<Log> findBuildingsAtField(Coordinate minCoordinate, Coordinate maxCoordinate) {
        // Najprv najde vsetky budovy v QuadTree
        ArrayList<IData> dataList = this.buildingsQuadTree.find(minCoordinate, maxCoordinate);
        if (dataList == null)
            return null;

        // Potom vlozi prislusne prvky zo suboru do ArrayListu, ktory nasledne vrati
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < dataList.size(); i++) {
            Building building = this.buildingFile.find(this.convertQTBuilding((QuadTreeBuilding) dataList.get(i)));
            if (building != null)
                finalDataList.add(building);
        }
        return finalDataList;
    }

    public Parcel findParcel(long id) {
        return this.parcelFile.find(new Parcel(id, "", new Coordinate(), new Coordinate()));
    }

    public ArrayList<Log> findParcelsWithIDs(long[] ids, int validParcels) {
        if (ids.length < validParcels)
            return null;
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < validParcels; i++)
            finalDataList.add(this.parcelFile.find(new Parcel(ids[i], "", new Coordinate(), new Coordinate())));

        if (finalDataList.isEmpty())
            return null;
        return finalDataList;
    }

    public ArrayList<Log> findParcelsAtField(Coordinate minCoordinate, Coordinate maxCoordinate) {
        // Najprv najde vsetky parcely v QuadTree
        ArrayList<IData> dataList = this.parcelsQuadTree.find(minCoordinate, maxCoordinate);
        if (dataList == null)
            return null;

        // Potom vlozi prislusne prvky zo suboru do ArrayListu, ktory nasledne vrati
        ArrayList<Log> finalDataList = new ArrayList<Log>();
        for (int i = 0; i < dataList.size(); i++) {
            Parcel parcel = this.parcelFile.find(this.convertQTParcel((QuadTreeParcel) dataList.get(i)));
            if (parcel != null)
                finalDataList.add(parcel);
        }
        return finalDataList;
    }

    public void remove(Log data) {
        if (data == null)
            return;

        if (data instanceof Building) {
            ArrayList<Log> foundParcels = this.findParcelsAtField(data.getCoordinates()[0], data.getCoordinates()[1]);
            for (Log current : foundParcels) {
                current.removeProperty(data);
                this.parcelFile.edit((Parcel) current);
            }

            this.buildingFile.delete(data);
            QuadTreeBuilding qtBuilding = this.convertToQTBuilding((Building) data);
            this.buildingsQuadTree.delete(qtBuilding);
            this.allPropertiesQuadTree.delete(qtBuilding);

        } else if (data instanceof Parcel) {
            ArrayList<Log> foundBuildings = this.findBuildingsAtField(data.getCoordinates()[0], data.getCoordinates()[1]);
            for (Log current : foundBuildings) {
                current.removeProperty(data);
                this.buildingFile.edit((Building) current);
            }
            this.parcelFile.delete(data);
            QuadTreeParcel qtParcel = this.convertToQTParcel((Parcel) data);
            this.parcelsQuadTree.delete(qtParcel);
            this.allPropertiesQuadTree.delete(qtParcel);
        }
    }

    /**
     * Vrati novu budovu ak prebehne edit v poriadku a staru ak nie
     * @param oldBuilding
     * @param newBuilding
     * @return
     */
    public Log editBuilding(Building oldBuilding, Building newBuilding) {
        if (!oldBuilding.equals((IData) newBuilding)) {
            QuadTreeBuilding newqtBuilding = this.convertToQTBuilding(newBuilding);
            if (this.allPropertiesQuadTree.insert(newqtBuilding)) {
                QuadTreeBuilding oldqtBuilding = this.convertToQTBuilding(oldBuilding);
                this.allPropertiesQuadTree.delete(oldqtBuilding);
                this.buildingsQuadTree.insert(newqtBuilding);
                this.buildingsQuadTree.delete(oldqtBuilding);

                long[] originalParcelIndexes = oldBuilding.getParcels();
                for (int i = 0; i < oldBuilding.getValidParcels(); i++) {
                    Parcel contacted = this.parcelFile.find(new Parcel(originalParcelIndexes[i], "", new Coordinate(), new Coordinate()));
                    contacted.removeProperty(oldBuilding);
                    this.parcelFile.edit(contacted);
                }

                ArrayList<IData> parcely = this.parcelsQuadTree.find(newBuilding.getCoordinates()[0], newBuilding.getCoordinates()[1]);
                for (IData data : parcely) {
                    if (!newBuilding.addProperty(this.convertQTParcel((QuadTreeParcel) data)))
                        break;
                }

                for (IData data : parcely) {
                    Parcel foundParcel = this.convertQTParcel((QuadTreeParcel) data);
                    foundParcel = this.parcelFile.find(foundParcel);
                    if (foundParcel.addProperty(newBuilding))
                        this.parcelFile.edit(foundParcel);
                }
            } else {
                return oldBuilding;
            }
        } else {
            long[] originalParcelIndexes = oldBuilding.getParcels();
            for (int i = 0; i < oldBuilding.getValidParcels(); i++) {
                newBuilding.addProperty(new Parcel(originalParcelIndexes[i], "", new Coordinate(), new Coordinate()));
            }
        }
        this.buildingFile.edit(newBuilding);
        return newBuilding;
    }

    /**
     * Vrati novy parcel ak prebehne edit v poriadku a stary ak nie
     * @param oldParcel
     * @param newParcel
     * @return
     */
    public Log editParcel(Parcel oldParcel, Parcel newParcel) {
        if (!oldParcel.equals((IData) newParcel)) {
            QuadTreeParcel newqtParcel = this.convertToQTParcel(newParcel);
            if (this.allPropertiesQuadTree.insert(newqtParcel)) {
                QuadTreeParcel oldqtParcel = this.convertToQTParcel(oldParcel);
                this.allPropertiesQuadTree.delete(oldqtParcel);
                this.parcelsQuadTree.insert(newqtParcel);
                this.parcelsQuadTree.delete(oldqtParcel);

                long[] originalBuildingIndexes = oldParcel.getBuildings();
                for (int i = 0; i < oldParcel.getValidBuildings(); i++) {
                    Parcel contacted = this.parcelFile.find(new Parcel(originalBuildingIndexes[i], "", new Coordinate(), new Coordinate()));
                    contacted.removeProperty(oldParcel);
                    this.parcelFile.edit(contacted);
                }

                ArrayList<IData> budovy = this.buildingsQuadTree.find(newParcel.getCoordinates()[0], newParcel.getCoordinates()[1]);
                for (IData data : budovy) {
                    if (!newParcel.addProperty(this.convertQTBuilding((QuadTreeBuilding) data)))
                        break;
                }

                for (IData data : budovy) {
                    Building foundBuilding = this.convertQTBuilding((QuadTreeBuilding) data);
                    foundBuilding = this.buildingFile.find(foundBuilding);
                    if (foundBuilding.addProperty(newParcel))
                        this.buildingFile.edit(foundBuilding);
                }
            } else {
                return oldParcel;
            }
        } else {
            long[] originalBuildingIndexes = oldParcel.getBuildings();
            for (int i = 0; i < oldParcel.getValidBuildings(); i++) {
                newParcel.addProperty(new Parcel(originalBuildingIndexes[i], "", new Coordinate(), new Coordinate()));
            }
        }
        this.parcelFile.edit(newParcel);
        return newParcel;
    }

    public boolean generateBuildings(int numberOfBuildings) {
        ArrayList<Building> newBuildings = this.bGenerator.generateData(this.buildingsQuadTree.getCoordinates()[0], this.buildingsQuadTree.getCoordinates()[1], numberOfBuildings, this.currentBuildingID);
        for (Building current : newBuildings) {
            if (!this.insertBuilding(current))
                return false;
            this.currentBuildingID++;
        }
        return true;
    }

    public boolean generateParcels(int numberOfParcels) {
        ArrayList<Parcel> newParcels = this.pGenerator.generateData(this.parcelsQuadTree.getCoordinates()[0], this.parcelsQuadTree.getCoordinates()[1], numberOfParcels, this.currentParcelID);
        for (Parcel current : newParcels) {
            if (!this.insertParcel(current))
                return false;
            this.currentParcelID++;
        }
        return true;
    }

    public boolean saveInFile() {
        // Najprv sa ulozia nastavenia suborov
        this.parcelFile.saveCurrentSetup();
        this.buildingFile.saveCurrentSetup();
        try {
            // Ukladanie obsahu Quadstromov do suboru
            FileWriter fileWriter = new FileWriter(fileName + "QuadTree.txt");
            ArrayList<IData> dataList = this.allPropertiesQuadTree.getAllData();
            // Najprv parametre QuadStromu
            fileWriter.write(String.valueOf(this.currentBuildingID) + ',' + String.valueOf(this.currentParcelID) + "\n");
            fileWriter.write("Tree," + this.allPropertiesQuadTree.getMaxDepth() + ',' +
                    this.allPropertiesQuadTree.getCoordinates()[0].getWidthAsChar() + ',' + this.allPropertiesQuadTree.getCoordinates()[0].getWidthPosition() + ',' +
                    this.allPropertiesQuadTree.getCoordinates()[0].getLengthAsChar() + ',' + this.allPropertiesQuadTree.getCoordinates()[0].getLengthPosition() + ',' +
                    this.allPropertiesQuadTree.getCoordinates()[1].getWidthAsChar() + ',' + this.allPropertiesQuadTree.getCoordinates()[1].getWidthPosition() + ',' +
                    this.allPropertiesQuadTree.getCoordinates()[1].getLengthAsChar() + ',' + this.allPropertiesQuadTree.getCoordinates()[1].getLengthPosition() + "\n");

            for (IData data : dataList) {
                if (data instanceof QuadTreeBuilding) {
                    QuadTreeBuilding building = (QuadTreeBuilding) data;
                    fileWriter.write("Building," + building.getId() + ',' +
                            building.getCoordinates()[0].getWidthAsChar() + ',' + building.getCoordinates()[0].getWidthPosition() + ',' +
                            building.getCoordinates()[0].getLengthAsChar() + ',' + building.getCoordinates()[0].getLengthPosition() + ',' +
                            building.getCoordinates()[1].getWidthAsChar() + ',' + building.getCoordinates()[1].getWidthPosition() + ',' +
                            building.getCoordinates()[1].getLengthAsChar() + ',' + building.getCoordinates()[1].getLengthPosition() + "\n");
                } else if (data instanceof QuadTreeParcel) {
                    QuadTreeParcel parcel = (QuadTreeParcel) data;
                    fileWriter.write("Parcel," + parcel.getId() + ',' +
                            parcel.getCoordinates()[0].getWidthAsChar() + ',' + parcel.getCoordinates()[0].getWidthPosition() + ',' +
                            parcel.getCoordinates()[0].getLengthAsChar() + ',' + parcel.getCoordinates()[0].getLengthPosition() + ',' +
                            parcel.getCoordinates()[1].getWidthAsChar() + ',' + parcel.getCoordinates()[1].getWidthPosition() + ',' +
                            parcel.getCoordinates()[1].getLengthAsChar() + ',' + parcel.getCoordinates()[1].getLengthPosition() + "\n");
                }
            }

            fileWriter.close();
        } catch (Exception e) {
            System.out.println("Error occured while writing into the file: " + e);
            return false;
        }
        return true;
    }

    public boolean loadFromFile() {
        File file = new File(this.fileName + "QuadTree.txt");
        if (!file.exists())
            return false;
        try {
            FileReader fileReader = new FileReader(this.fileName + "QuadTree.txt");
            BufferedReader buffer = new BufferedReader(fileReader);
            String line = buffer.readLine();
            if (line != null) {
                ArrayList<String> lineWords = new ArrayList<>(Arrays.asList(line.split(",")));
                this.currentBuildingID = Integer.parseInt(lineWords.get(0));
                this.currentParcelID = Integer.parseInt(lineWords.get(1));

                line = buffer.readLine();
                lineWords.clear();
                lineWords.addAll(Arrays.asList(line.split(",")));

                Coordinate minCoordinate = new Coordinate(this.charToWidth(lineWords.get(2).charAt(0)), Double.parseDouble(lineWords.get(3)),
                        this.charToLength(lineWords.get(4).charAt(0)), Double.parseDouble(lineWords.get(5)));
                Coordinate maxCoordinate = new Coordinate(this.charToWidth(lineWords.get(6).charAt(0)), Double.parseDouble(lineWords.get(7)),
                        this.charToLength(lineWords.get(8).charAt(0)), Double.parseDouble(lineWords.get(9)));

                if (CoordinateComputer.containsCoordinateInclBorderPoints(this.allPropertiesQuadTree.getCoordinates()[0], this.allPropertiesQuadTree.getCoordinates()[1], minCoordinate) &&
                        CoordinateComputer.containsCoordinateInclBorderPoints(this.allPropertiesQuadTree.getCoordinates()[0], this.allPropertiesQuadTree.getCoordinates()[1], maxCoordinate)) {

                    while ((line = buffer.readLine()) != null) {
                        lineWords.clear();
                        lineWords.addAll(Arrays.asList(line.split(",")));
                        minCoordinate = new Coordinate(this.charToWidth(lineWords.get(2).charAt(0)), Double.parseDouble(lineWords.get(3)),
                                this.charToLength(lineWords.get(4).charAt(0)), Double.parseDouble(lineWords.get(5)));
                        maxCoordinate = new Coordinate(this.charToWidth(lineWords.get(6).charAt(0)), Double.parseDouble(lineWords.get(7)),
                                this.charToLength(lineWords.get(8).charAt(0)), Double.parseDouble(lineWords.get(9)));
                        if (lineWords.get(0).equals("Building")) {
                            QuadTreeBuilding newQTBuilding = new QuadTreeBuilding(Long.parseLong(lineWords.get(1)), minCoordinate, maxCoordinate);
                            this.buildingsQuadTree.insert(newQTBuilding);
                            this.allPropertiesQuadTree.insert(newQTBuilding);

                        } else if (lineWords.get(0).equals("Parcel")){
                            QuadTreeParcel newQTParcel = new QuadTreeParcel(Long.parseLong(lineWords.get(1)), minCoordinate, maxCoordinate);
                            this.parcelsQuadTree.insert(newQTParcel);
                            this.allPropertiesQuadTree.insert(newQTParcel);
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

    public String getBuildingFileAsString() {
        String finalText = "";
        ArrayList<Block<Building>> blocks = this.buildingFile.getAllRegularBlocks();
        finalText += "Regular file\n";
        finalText = this.appendBuildingBlocksToString(blocks, finalText);

        blocks = this.buildingFile.getAllOverflowBlocks();
        finalText += "\nOverflow file\n";
        finalText = this.appendBuildingBlocksToString(blocks, finalText);
        return finalText;
    }

    public String getParcelFileAsString() {
        String finalText = "";
        ArrayList<Block<Parcel>> blocks = this.parcelFile.getAllRegularBlocks();
        finalText += "Regular file\n";
        finalText = this.appendParcelBlocksToString(blocks, finalText);

        blocks = this.parcelFile.getAllOverflowBlocks();
        finalText += "\nOverflow file\n";
        finalText = this.appendParcelBlocksToString(blocks, finalText);
        return finalText;
    }

    private String appendBuildingBlocksToString(ArrayList<Block<Building>> blocks, String text) {
        for (int i = 0; i < blocks.size(); i++) {
            text += "Block " + i + " Valid Count: " + blocks.get(i).getValidCount() + " Next Block: " + blocks.get(i).getNextBlock() + " Active: " + blocks.get(i).isActive() + "\n";
            IRecord[] records = blocks.get(i).getRecords();
            for (int j = 0; j < blocks.get(i).getValidCount(); j++) {
                text += ((Log)records[j]).getFullDescription() + "\n";
            }
        }
        return text;
    }

    private String appendParcelBlocksToString(ArrayList<Block<Parcel>> blocks, String text) {
        for (int i = 0; i < blocks.size(); i++) {
            text += "Block " + i + " Valid Count: " + blocks.get(i).getValidCount() + " Next Block: " + blocks.get(i).getNextBlock() + " Active: " + blocks.get(i).isActive() + "\n";
            IRecord[] records = blocks.get(i).getRecords();
            for (int j = 0; j < blocks.get(i).getValidCount(); j++) {
                text += ((Log)records[j]).getFullDescription() + "\n";
            }
        }
        return text;
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

    /**
     * "Konvertuje" QuadTree budovu na budovu s rovnakym id, suradnicami a prazdnymi zvysnymi atributmi
     * @param qtBuilding
     * @return
     */
    private Building convertQTBuilding(QuadTreeBuilding qtBuilding) {
        return new Building(qtBuilding.getId(), 0, "", qtBuilding.getCoordinates()[0], qtBuilding.getCoordinates()[1]);
    }

    /**
     * "Konvertuje" budovu na QuadTree budovu s rovnakymi klucmi
     * @param building
     * @return
     */
    private QuadTreeBuilding convertToQTBuilding(Building building) {
        return new QuadTreeBuilding(building.getId(), building.getCoordinates()[0], building.getCoordinates()[1]);
    }

    /**
     * "Konvertuje" QuadTree parcel na parcel s rovnakym id, suradnicami a prazdnymi zvysnymi atributmi
     * @param qtParcel
     * @return
     */
    private Parcel convertQTParcel(QuadTreeParcel qtParcel) {
        return new Parcel(qtParcel.getId(), "", qtParcel.getCoordinates()[0], qtParcel.getCoordinates()[1]);
    }

    /**
     * "Konvertuje" parcel na QuadTree parcel s rovnakymi klucmi
     * @param parcel
     * @return
     */
    private QuadTreeParcel convertToQTParcel(Parcel parcel) {
        return new QuadTreeParcel(parcel.getId(), parcel.getCoordinates()[0], parcel.getCoordinates()[1]);
    }
}

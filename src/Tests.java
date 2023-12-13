import Generator.BuildingGenerator;
import Generator.ParcelGenerator;
import Model.Data.Building;
import Model.Data.Log;
import Model.DynamicHashFile.Data.Block;
import Model.DynamicHashFile.Data.IRecord;
import Model.DynamicHashFile.DynamicHashFile;
import Model.DynamicHashFile.DynamicHashFileNode;
import Model.DynamicHashFile.DynamicHashFileNodeExternal;
import Model.DynamicHashFile.DynamicHashFileNodeInternal;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;
import Model.QuadTree.Data.IData;
import Model.Data.Parcel;
import Model.QuadTree.QuadTree;
import Model.QuadTree.Coordinates.Width;
import Model.QuadTree.Coordinates.Length;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;


public class Tests {
    private ParcelGenerator pGenerator;
    private BuildingGenerator bGenerator;
    private QuadTree testTree;
    private Width[] width = new Width[2];
    private double[] widthPositions = new double[2], lengthPositions = new double[2];
    private Length[] lengths = new Length[2];
    private Coordinate[] coordinates, searchedArea;
    private ArrayList<Parcel> parcelList;
    private ArrayList<Building> buildingList;
    private ArrayList<IData> dataToFind = new ArrayList<>();
    private int id, numberOfInstances, maxDepth;
    public Tests() {
        this.pGenerator = new ParcelGenerator();
        this.bGenerator = new BuildingGenerator();
        this.width[0] = Width.N;
        this.widthPositions[0] = 90;
        this.lengths[0] = Length.E;
        this.lengthPositions[0] = 90;
        this.width[1] = Width.S;
        this.widthPositions[1] = 90;
        this.lengths[1] = Length.W;
        this.lengthPositions[1] = 90;
        this.id = 1;
        this.numberOfInstances = 10000;
        this.maxDepth = 50;

        this.coordinates = new Coordinate[]{ new Coordinate(this.width[0], this.widthPositions[0], this.lengths[0], this.lengthPositions[0]),
                new Coordinate(this.width[1], this.widthPositions[1], this.lengths[1], this.lengthPositions[1])};

        this.testTree = new QuadTree(this.maxDepth, this.width[0], this.widthPositions[0], this.lengths[0], this.lengthPositions[0],
                this.width[1], this.widthPositions[1], this.lengths[1], this.lengthPositions[1]);

        this.parcelList = this.pGenerator.generateData(this.coordinates[0], this.coordinates[1], this.numberOfInstances, this.id);

        this.searchedArea = new Coordinate[] {new Coordinate(Width.N, 40, Length.W, 40),
                new Coordinate(Width.S, 40, Length.E, 40)};
    }

    public boolean testIRecordByteConversions() {
        Parcel parcel = new Parcel(1, 20, "Abcde", this.searchedArea[0], this.searchedArea[1]);
        Building building = new Building(2, 30, "Fghij", this.searchedArea[0], this.searchedArea[1]);
        parcel.addProperty(building);
        building.addProperty(parcel);

        byte[] byteBuilding = building.toByteArray();
        byte[] byteParcel = parcel.toByteArray();
        Parcel newParcel = new Parcel();
        Building newBuilding = new Building();
        System.out.println("Parcel: " + newParcel.getSize() + " & " + byteParcel.length);
        System.out.println("Building: " + newBuilding.getSize() + " & " + byteBuilding.length);
        newParcel.fromByteArray(byteParcel);
        newBuilding.fromByteArray(byteBuilding);

        if (!parcel.equals(newParcel))
            System.out.println("Parcel equals nevysiel.");
        if (!parcel.getDescription().equals(newParcel.getDescription()))
            System.out.println("Parcel description nevysiel.");
        if (!building.equals(newBuilding))
            System.out.println("Building equals nevysiel.");
        if (!building.getDescription().equals(newBuilding.getDescription()))
            System.out.println("Building description nevysiel.");

        return true;
    }

    public boolean testInsert(){
        //false test
        Parcel parcel = new Parcel(12, 1, "test1",
                new Coordinate(this.width[0], this.widthPositions[0] + 1, this.lengths[0], this.lengthPositions[0]+1),
                new Coordinate(this.width[1], this.widthPositions[1] + 1, this.lengths[1], this.lengthPositions[1]+1));
        if (this.testTree.insert(parcel)){
            return false;
        }
        System.out.println("Test insert mimo suradnic vysiel dobre");

        parcel = new Parcel(12, 1, "test1",
                new Coordinate(this.width[0], this.widthPositions[0] - 1, this.lengths[0], this.lengthPositions[0]-1),
                new Coordinate(this.width[1], this.widthPositions[1] - 1, this.lengths[1], this.lengthPositions[1]-1));
        if (!this.testTree.insert(parcel)){
            return false;
        }
        System.out.println("Test insert vramci suradnic vysiel dobre");

        if (this.testTree.insert(parcel)){
            return false;
        }
        System.out.println("Test insert vramci primarnych klucov vysiel dobre");

        parcel = new Parcel(12, 2, "test1",
                new Coordinate(this.width[0], this.widthPositions[0] - 2, this.lengths[0], this.lengthPositions[0] - 2),
                new Coordinate(this.width[0], this.widthPositions[0] - 3, this.lengths[0], this.lengthPositions[0] - 3));
        if (!this.testTree.insert(parcel)){
            return false;
        }
        System.out.println("Test insert vramci suradnic 2 vysiel dobre");

        return true;
    }

    public boolean bruteForceTestInsert() {
        // Insert bruteForce Test
        for (Parcel data : this.parcelList) {
            ArrayList<Coordinate> dataCoordinates = new ArrayList<>();
            dataCoordinates.addAll(Arrays.asList(data.getCoordinates()));
            dataCoordinates.addAll(Arrays.asList(CoordinateComputer.invertCoordinates(data.getCoordinates()[0], data.getCoordinates()[1])));
            boolean included = false;

            if (!this.testTree.insert(data)) {
                System.out.println("Pokazene data: " + data.getId());
                return false;
            }
            for (Coordinate coordinate : dataCoordinates) {
                if (CoordinateComputer.containsCoordinateInclBorderPoints(this.searchedArea[0], this.searchedArea[1], coordinate)) {
                    this.dataToFind.add(data);
                    included = true;
                    break;
                }
            }
            if (!included) {
                if (CoordinateComputer.containsCoordinateInclBorderPoints(dataCoordinates.get(0), dataCoordinates.get(1), this.searchedArea[0]) ||
                        CoordinateComputer.containsCoordinateInclBorderPoints(dataCoordinates.get(0), dataCoordinates.get(1), this.searchedArea[1]))
                    this.dataToFind.add(data);
            }
        }

        return true;
    }

    public boolean bruteForceTestFind() {
        ArrayList<IData> foundData = this.testTree.find(this.searchedArea[0], this.searchedArea[1]);

        for (IData data : foundData) {
            if (!this.dataToFind.contains(data)) {
                System.out.println("1. Data ID: " + ((Parcel) data).getId() + " sa nenachadza v predpokladanych datach");
                return false;
            }
        }

        for (IData data : this.dataToFind) {
            if (!foundData.contains(data)) {
                System.out.println("2. Data ID: " + ((Parcel) data).getId() + " sa nenachadza v najdenych datach");
                return false;
            }
        }

        return true;
    }

    public boolean bruteForceTestDelete() {
        for (Parcel data : this.parcelList)
            this.testTree.delete(data);

        return this.testTree.isEmpty();
    }

    public void changeDepth(int newDepth) {
        this.testTree.changeDepth(newDepth);
        this.maxDepth = newDepth;
    }

    public double getHealth() { return this.testTree.getHealth(); }

    public void optimise() {
        this.testTree.optimise();
    }

    public void monteCarlo(int numberOfExperiments) {
        double averageInsertImprovement = 0, bestInsertImprovement = 0, worstInsertImprovement = 0,
                averageFindImprovement = 0, bestFindImprovement = 0, worstFindImprovement = 0,
                averageDeleteImprovement = 0, bestDeleteImprovement = 0, worstDeleteImprovement = 0;

        QuadTree regularTree, optimisedTree;
        for (int i = 0; i < numberOfExperiments; i++) {
            this.parcelList = this.pGenerator.generateData(this.coordinates[0], this.coordinates[1], this.numberOfInstances, this.id);
            regularTree = new QuadTree(this.maxDepth, this.width[0], this.widthPositions[0], this.lengths[0], this.lengthPositions[0],
                    this.width[1], this.widthPositions[1], this.lengths[1], this.lengthPositions[1]);
            optimisedTree = new QuadTree(this.maxDepth, this.width[0], this.widthPositions[0], this.lengths[0], this.lengthPositions[0],
                    this.width[1], this.widthPositions[1], this.lengths[1], this.lengthPositions[1]);
            for (Parcel data : this.parcelList) {
                if (!regularTree.insert(data) || !optimisedTree.insert(data)) {
                    System.out.println("Probem pri inicializacnom vkladani");
                    return;
                }
            }
            optimisedTree.optimise2();
            //ArrayList<Parcel> newDataListForOptimised = this.pGenerator.generateData(optimisedTree.getCoordinates()[0], optimisedTree.getCoordinates()[1], this.numberOfInstances, this.id);
            //ArrayList<Parcel> newDataListForRegular = this.pGenerator.generateData(this.coordinates[0], this.coordinates[1], this.numberOfInstances, this.id);

            for (Parcel data : this.parcelList) {
                regularTree.delete(data);
                optimisedTree.delete(data);
            }

            if (!regularTree.isEmpty() || !optimisedTree.isEmpty()){
                System.out.println("Stromy sa nepremazali.");
                return;
            }

            //Testy insertov
            long timeOptimised = 0, timeRegular = 0;
            double improvement = 0;
            for (Parcel data : this.parcelList) {
                long start = System.nanoTime();
                if (!optimisedTree.insert(data)) {
                    System.out.println("Problem pri optimalizovanom teste insert");
                    return;
                }
                timeOptimised += System.nanoTime() - start;

                start = System.nanoTime();
                if (!regularTree.insert(data)) {
                    System.out.println("Problem pri regularnom teste insert");
                    return;
                }
                timeRegular += System.nanoTime() - start;

            }
            improvement = 100 - ((double) timeOptimised / timeRegular) * 100;
            if (i == 0) {
                averageInsertImprovement = improvement;
                bestInsertImprovement = improvement;
                worstInsertImprovement = improvement;
            } else {
                averageInsertImprovement = (averageInsertImprovement + improvement) / 2;
                if (improvement > bestInsertImprovement)
                    bestInsertImprovement = improvement;
                if (improvement < worstInsertImprovement)
                    worstInsertImprovement = improvement;
            }

            //Testy Find
            timeOptimised = 0;
            timeRegular = 0;
            for (Parcel data : this.parcelList) {
                long start = System.nanoTime();
                optimisedTree.find(this.searchedArea[0], this.searchedArea[1]);
                timeOptimised += System.nanoTime() - start;

                start = System.nanoTime();
                optimisedTree.find(this.searchedArea[0], this.searchedArea[1]);
                timeRegular += System.nanoTime() - start;

            }
            improvement = 100 - ((double) timeOptimised / timeRegular) * 100;
            if (i == 0) {
                averageFindImprovement = improvement;
                bestFindImprovement = improvement;
                worstFindImprovement = improvement;
            } else {
                averageFindImprovement = (averageFindImprovement + improvement) / 2;
                if (improvement > bestFindImprovement)
                    bestFindImprovement = improvement;
                if (improvement < worstFindImprovement)
                    worstFindImprovement = improvement;
            }


            //Testy delete
            timeOptimised = 0;
            timeRegular = 0;
            for (Parcel data : this.parcelList) {
                long start = System.nanoTime();
                if (optimisedTree.delete(data) != data) {
                    System.out.println("Problem pri optimalnom delete.");
                    return;
                }
                timeOptimised += System.nanoTime() - start;
                //timeOptimised += (System.nanoTime() - start) / 1000000;

                start = System.nanoTime();
                if (regularTree.delete(data) != data) {
                    System.out.println("Problem pri regularnom delete.");
                    return;
                }
                timeRegular += System.nanoTime() - start;

            }

            if (!regularTree.isEmpty() || !optimisedTree.isEmpty()){
                System.out.println("Stromy sa pri testoch nepremazali.");
                return;
            }

            improvement = 100 - ((double) timeOptimised / timeRegular) * 100;
            if (i == 0) {
                averageDeleteImprovement = improvement;
                bestDeleteImprovement = improvement;
                worstDeleteImprovement = improvement;
            } else {
                averageDeleteImprovement = (averageDeleteImprovement + improvement) / 2;
                if (improvement > bestDeleteImprovement)
                    bestDeleteImprovement = improvement;
                if (improvement < worstDeleteImprovement)
                    worstDeleteImprovement = improvement;
            }
        }

        System.out.println("Priemerne zlepsenie insert bolo: " + averageInsertImprovement + "%\n Najlepsie zrychlenie bolo: " +
                bestInsertImprovement + "%\n Najhorsie zlepsenie bolo: " + worstInsertImprovement + "%\n");
        System.out.println("Priemerne zlepsenie find bolo: " + averageFindImprovement + "%\n Najlepsie zrychlenie bolo: " +
                bestFindImprovement + "%\n Najhorsie zlepsenie bolo: " + worstFindImprovement + "%\n");
        System.out.println("Priemerne zlepsenie delete bolo: " + averageDeleteImprovement + "%\n Najlepsie zrychlenie bolo: " +
                bestDeleteImprovement + "%\n Najhorsie zlepsenie bolo: " + worstDeleteImprovement + "%\n");
    }

    public void testHashFile(){
        DynamicHashFile<Parcel> parcelHashFile = new DynamicHashFile<Parcel>(3, 4, 10, "parcely", Parcel.class);
//        DynamicHashFile<Building> buildingHashFile = new DynamicHashFile<Building>(3, 4, 3, "budovy", Building.class);
        this.parcelList = pGenerator.generateData(this.coordinates[0], this.coordinates[1], 10, 0);
        /*this.buildingList = bGenerator.generateData(this.coordinates[0], this.coordinates[1], 9, 0);
        for (Building building : buildingList) {
            System.out.println("\n" + building.getFullDescription());
            buildingHashFile.insert(building);
            this.printBuildingHashFile(buildingHashFile);
        }*/

        for (Parcel parcel : parcelList) {
            System.out.println("\nInsert: " + parcel.getFullDescription());
            parcelHashFile.insert(parcel);
            this.printParcelHashFile(parcelHashFile);
        }

        if (parcelHashFile.insert(parcelList.get(0)))
            System.out.println("Test unikatnosti nefunguje spravne");
        if (parcelHashFile.insert(parcelList.get(8)))
            System.out.println("Test unikatnosti nefunguje spravne");

        System.out.println("");
//        for (int i = 0; i < 3; i++) {
        for (int i = 0; i < this.parcelList.size(); i++) {
            if (!this.parcelList.get(i).equals(parcelHashFile.find(this.parcelList.get(i))))
                System.out.println("Find Parcel test N.o.: " + i + " failure");
            else
                System.out.println("Find Parcel test N.o.: " + i + " correct");
            /*if (!this.buildingList.get(i).equals(buildingHashFile.find(this.buildingList.get(i))))
                System.out.println("Find Building test N.o.: " + i + " failure");
            else
                System.out.println("Find Building test N.o.: " + i + " correct");*/
        }

        Parcel toEdit = this.parcelList.get(3);
        Parcel edited = new Parcel(toEdit.getId(), 0, "ABCDE", toEdit.getCoordinates()[0], toEdit.getCoordinates()[1]);
        if (!parcelHashFile.edit(edited))
            System.out.println("Pri edite doslo k chybe");

//        for (Parcel parcel : parcelList) {
        for (int i = 0; i < this.parcelList.size() - 1; i++) {
            System.out.println("\nDelete: " + parcelList.get(i).getFullDescription());
            parcelHashFile.delete(parcelList.get(i));
            this.printParcelHashFile(parcelHashFile);
        }

        for (int i = 0; i < this.parcelList.size() - 1; i++) {
            System.out.println("\nInsert: " + parcelList.get(i).getFullDescription());
            parcelHashFile.insert(parcelList.get(i));
            this.printParcelHashFile(parcelHashFile);
        }

        System.out.println(" ");
        for (int i = 0; i < this.parcelList.size(); i++) {
            if (!this.parcelList.get(i).equals(parcelHashFile.find(this.parcelList.get(i))))
                System.out.println("Find Parcel test N.o.: " + i + " failure");
            else
                System.out.println("Find Parcel test N.o.: " + i + " correct");
            /*if (!this.buildingList.get(i).equals(buildingHashFile.find(this.buildingList.get(i))))
                System.out.println("Find Building test N.o.: " + i + " failure");
            else
                System.out.println("Find Building test N.o.: " + i + " correct");*/
        }

        for (Parcel parcel : parcelList) {
//        for (int i = 0; i < this.parcelList.size() - 1; i++) {
            System.out.println("\nDelete: " + parcel.getFullDescription());
            parcelHashFile.delete(parcel);
            this.printParcelHashFile(parcelHashFile);
        }

//        this.printParcelHashFile(parcelHashFile);
    }

    public void printParcelHashFile(DynamicHashFile<Parcel> file) {
        System.out.println(file.getTrieAsString());

        ArrayList<Block<Parcel>> blocks = file.getAllRegularBlocks();
        System.out.println("Regular file");
        printBlocks(blocks);

        blocks = file.getAllOverflowBlocks();
        System.out.println("Overflow file");
        printBlocks(blocks);
    }

    private void printBlocks(ArrayList<Block<Parcel>> blocks) {
        for (int i = 0; i < blocks.size(); i++) {
            System.out.println("Block " + i + " Valid Count: " + blocks.get(i).getValidCount() + " Next Block: " + blocks.get(i).getNextBlock() + " Active: " + blocks.get(i).isActive());
            IRecord[] records = blocks.get(i).getRecords();
            for (int j = 0; j < blocks.get(i).getValidCount(); j++) {
                System.out.println(((Log)records[j]).getFullDescription());
            }
        }
    }

    public void printBuildingHashFile(DynamicHashFile<Building> file) {
        System.out.println(file.getTrieAsString());

        ArrayList<Block<Building>> blocks = file.getAllRegularBlocks();
        System.out.println("Regular file");
        for (int i = 0; i < blocks.size(); i++) {
            System.out.println("Block " + i + " Valid Count: " + blocks.get(i).getValidCount());
            IRecord[] records = blocks.get(i).getRecords();
            for (int j = 0; j < blocks.get(i).getValidCount(); j++) {
                System.out.println(((Log)records[j]).getFullDescription());
            }
        }

        blocks = file.getAllOverflowBlocks();
        System.out.println("Overflow file");
        for (int i = 0; i < blocks.size(); i++) {
            System.out.println("Block " + i + " Valid Count: " + blocks.get(i).getValidCount());
            IRecord[] records = blocks.get(i).getRecords();
            for (int j = 0; j < blocks.get(i).getValidCount(); j++) {
                System.out.println(((Log)records[j]).getFullDescription());
            }
        }
    }
}

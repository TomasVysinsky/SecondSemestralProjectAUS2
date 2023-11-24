package Model.QuadTree;

import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;
import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;
import Model.QuadTree.Data.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Queue;
import java.util.LinkedList;

public class QuadTree {
    private QuadTreeNode root;
    private int maxDepth;

    private int currentDepth;

    //private int nodesWithCurrentDepth;
    private ArrayList<Integer> nodesOnEachDepth;
    private Coordinate[] coordinates;

    boolean autoOptimise;

    public QuadTree(int maxDepth,
                    Width minWidth, double minWidthPosition, Length minLength, double minLengthPosition,
                    Width maxWidth, double maxWidthPosition, Length maxLength, double maxLengthPosition)
    {
        this.root = new QuadTreeNode(new Coordinate(minWidth, minWidthPosition, minLength, minLengthPosition),
                new Coordinate(maxWidth, maxWidthPosition, maxLength, maxLengthPosition), null, 1);
        this.coordinates = this.root.getCoordinates();
        this.maxDepth = maxDepth;
        this.currentDepth = 1;
        //this.nodesWithCurrentDepth = 1;
        this.nodesOnEachDepth = new ArrayList<>();
        this.nodesOnEachDepth.add(1);
        this.autoOptimise = false;
    }

    /**
     *
     * @param coordinate
     * @return vsetky data, ktore sa prekryvaju s danym bodom
     */
    public ArrayList<IData> find(Coordinate coordinate) {
        return this.findFromNode(coordinate, this.root);
    }

    /**
     *
     * @param coordinate
     * @param startNode
     * @return vsetky data, ktore sa prekryvaju s danym bodom pricom hladanie zacina od zadaneho nodu
     */
    private ArrayList<IData> findFromNode(Coordinate coordinate, QuadTreeNode startNode) {
        if (!startNode.containsCoordinate(coordinate))
            return null;

        boolean bottomFound = false;
        QuadTreeNode sector = startNode;
        ArrayList<IData> dataFound = new ArrayList<IData>();
        while (!bottomFound){
            ArrayList<IData> sectorData = sector.getDataList();
            for (IData data : sectorData) {
                if (CoordinateComputer.containsCoordinateInclBorderPoints(data.getCoordinates()[0], data.getCoordinates()[1], coordinate))
                    dataFound.add(data);
            }
            QuadTreeNode nextSector = sector.findNextNode(new Coordinate[]{coordinate});
            if (nextSector == sector){
                bottomFound = true;
            } else {
                sector = nextSector;
            }
        }

        return dataFound;
    }

    /**
     *
     * @param minCoordinate
     * @param maxCoordinate
     * @return vsetky data, ktore sa prekryvaju s polygonom definovanym dvomi danymi bodmi
     */
    public ArrayList<IData> find(Coordinate minCoordinate, Coordinate maxCoordinate) {
        if (!CoordinateComputer.containsCoordinateInclBorderPoints(this.root.getCoordinates()[0], this.root.getCoordinates()[1], minCoordinate)
                || !CoordinateComputer.containsCoordinateInclBorderPoints(this.root.getCoordinates()[0], this.root.getCoordinates()[1], maxCoordinate))
            return null;

        Queue<QuadTreeNode> sectors = new LinkedList<QuadTreeNode>();
        sectors.add(this.root);
        ArrayList<IData> dataFound = new ArrayList<IData>();

        while (!sectors.isEmpty()) {
            QuadTreeNode actualSector = sectors.remove();
            ArrayList<IData> sectorData = actualSector.getDataList();

            for (IData data : sectorData) {
                boolean found = false;

                // Overi ci sa niektora z primarnych suradnic urcujuce data nenachadzaju v hladanej ploche pripadne ci nekopiruje hladanu plochu
                for (Coordinate coordinate : data.getCoordinates()) {
                    if (CoordinateComputer.containsCoordinateInclBorderPoints(minCoordinate, maxCoordinate, coordinate)) {
                        dataFound.add(data);
                        found = true;
                        break;
                    }
                }

                // Overi ci sa niektora z inverznych suradnic nenachadza v hladanej ploche
                if (!found) {
                    Coordinate[] invertedDataCoordinates = CoordinateComputer.invertCoordinates(data.getCoordinates()[0], data.getCoordinates()[1]);
                    for (Coordinate coordinate : invertedDataCoordinates) {
                        if (CoordinateComputer.containsCoordinateInclBorderPoints(minCoordinate, maxCoordinate, coordinate)) {
                            dataFound.add(data);
                            found = true;
                            break;
                        }
                    }
                }

                // Overi ci sa hladana plocha nenachadza v ploche ulozeneho data
                if (!found) {
                    if (CoordinateComputer.containsCoordinateInclBorderPoints(data.getCoordinates()[0], data.getCoordinates()[1], minCoordinate)) {
                        dataFound.add(data);
                    } else if (CoordinateComputer.containsCoordinateInclBorderPoints(data.getCoordinates()[0], data.getCoordinates()[1], maxCoordinate)) {
                        dataFound.add(data);
                    }
                }
            }

            if (actualSector.gotChildren()) {
                ArrayList<QuadTreeNode> childrenContained = new ArrayList<QuadTreeNode>();

                for (QuadTreeNode child : actualSector.getChildren()) {
                    Coordinate[] invertedCoordinates = CoordinateComputer.invertCoordinates(child.getCoordinates()[0], child.getCoordinates()[1]);
                    Coordinate[] allCoordinates = new Coordinate[]{child.getCoordinates()[0], child.getCoordinates()[1],
                            invertedCoordinates[0], invertedCoordinates[1]};

                    boolean found = false;
                    for (Coordinate actualCoordinate : allCoordinates) {
                        if (CoordinateComputer.containsCoordinateInclBorderPoints(minCoordinate, maxCoordinate, actualCoordinate)) {
                            childrenContained.add(child);
                            found = true;
                            break;
                        }
                    }

                    if (!found)
                        if (CoordinateComputer.containsCoordinateInclBorderPoints(allCoordinates[0], allCoordinates[1], minCoordinate)) {
                            childrenContained.add(child);
                        } else if (CoordinateComputer.containsCoordinateInclBorderPoints(allCoordinates[0], allCoordinates[1], maxCoordinate)) {
                            childrenContained.add(child);
                        }
                }

                sectors.addAll(childrenContained);
            }
        }

        return dataFound;
    }

    /**
     *
     * @param data
     * @return true ak sa data podari vlozit, false ak nie
     */
    public boolean insert(IData data) {
        boolean inserted = false;
        QuadTreeNode sector = this.root;

        while(!inserted){
            sector = findSector(data.getCoordinates(), sector);
            if (sector == null)
                return false;

            try {
                inserted = sector.tryInsert(data, sector.getDepth() < this.maxDepth);
            } catch (Exception e) {
                return false;
            }
            if (!inserted) {
                if (sector.getChildren()[0].getDepth() > this.currentDepth) {
                    this.currentDepth++;
                    //this.nodesWithCurrentDepth = sector.getChildren().length;
                    this.nodesOnEachDepth.add(sector.getChildren().length);
                } else { //if (sector.getChildren()[0].getDepth() == this.currentDepth) {
                    //this.nodesWithCurrentDepth += sector.getChildren().length;
                    this.nodesOnEachDepth.set(sector.getDepth(), this.nodesOnEachDepth.get(sector.getDepth()) + sector.getChildren().length);
                }
            }
        }

        if (autoOptimise)
            if (this.optimalisationNeeded())
                this.optimise();

        return true;
    }

    public IData delete(IData data) {
        if (data == null)
            return null;
        if (!this.root.containsCoordinate(data.getCoordinates()[0]) || !this.root.containsCoordinate(data.getCoordinates()[1]))
            return null;

        Coordinate[] dataCoordinates = CoordinateComputer.normalizeCoordinates(data.getCoordinates()[0], data.getCoordinates()[1]);
        QuadTreeNode sector = this.findSector(dataCoordinates, this.root);
        if (sector == null)
            return null;

        IData removedData = sector.remove(data);
        sector = sector.getParent();

        boolean optimalisationNeed = true;

        do {
            if (sector == null) {
                optimalisationNeed = false;
            } else {
                if (sector.optimiseNode()) {
                    this.nodesOnEachDepth.set(sector.getDepth(), this.nodesOnEachDepth.get(sector.getDepth()) - 4);
                    if (sector.getDepth() == this.currentDepth - 1 && this.nodesOnEachDepth.get(sector.getDepth()) == 0) {
                        this.currentDepth--;
                        this.nodesOnEachDepth.remove(sector.getDepth());
                    }
                    sector = sector.getParent();
                } else {
                    optimalisationNeed = false;
                }
            }
        } while (optimalisationNeed);


        if (autoOptimise)
            if (this.optimalisationNeeded())
                this.optimise();

        return removedData;
    }

    /**
     * Najde data ktore maju rovnake primerne aj sekundarne kluce a aktualizuje ich podla toho
     * @param data
     */
    public void edit(IData data) {
        if (data != null) {
            for (IData current : this.findSector(data.getCoordinates(), this.root).getDataList()) {
                if (current.equals(data))
                    current.edit(data);
            }
        }
    }

    /**
     * Zmeni maximalnu hlbku stromu na zadanu a akrualizuje strom tak, aby sa do nej vmestil
     * @param newDepth
     */
    public void changeDepth(int newDepth) {
        if (newDepth == this.maxDepth)
            return;

        if (newDepth > this.maxDepth && this.maxDepth == this.currentDepth) {
            // Moznost ked sa hlbka zvacsi a potrebujeme rozsirit aj hlbku nejakeho nodu

            Queue<QuadTreeNode> sectors = new LinkedList<>();
            sectors.add(this.root);
            int newCurrentDepth = this.currentDepth;

            while (!sectors.isEmpty()) {
                QuadTreeNode currentNode = sectors.remove();

                if (currentNode.getDepth() >= this.currentDepth) {
                    if (currentNode.tryToIncreaseMyDepth()) {
                        if (currentNode.getDepth() == newCurrentDepth) {
                            newCurrentDepth++;
                            //this.nodesWithCurrentDepth = currentNode.getChildren().length;
                            this.nodesOnEachDepth.add(currentNode.getChildren().length);
                        } else { //if (currentNode.getDepth() == newCurrentDepth - 1) {
                            //this.nodesWithCurrentDepth += currentNode.getChildren().length;
                            this.nodesOnEachDepth.set(currentNode.getDepth(), this.nodesOnEachDepth.get(currentNode.getDepth()) + currentNode.getChildren().length);
                        }

                        if (currentNode.getChildren()[0].getDepth() < newDepth)
                            sectors.addAll(Arrays.asList(currentNode.getChildren()));
                    }

                } else if (currentNode.gotChildren()) {
                    sectors.addAll(Arrays.asList(currentNode.getChildren()));
                }
            }

            this.currentDepth = newCurrentDepth;
        } else if (newDepth < this.currentDepth) {
            // zmensovanie stromu ak je vacsi ako nova velkost
            Queue<QuadTreeNode> sectorsUnexplored = new LinkedList<>();
            sectorsUnexplored.add(this.root);
            Queue<QuadTreeNode> sectorsToReduceDepth = new LinkedList<>();

            while (!sectorsUnexplored.isEmpty()) {
                QuadTreeNode currentNode = sectorsUnexplored.remove();

                if (!currentNode.gotChildren() && currentNode.getDepth() > newDepth) {
                    if (!sectorsToReduceDepth.contains(currentNode.getParent()))
                        sectorsToReduceDepth.add(currentNode.getParent());

                } else if (currentNode.gotChildren()){
                    sectorsUnexplored.addAll(Arrays.asList(currentNode.getChildren()));
                }
            }

            while (!sectorsToReduceDepth.isEmpty()) {
                QuadTreeNode currentNode = sectorsUnexplored.remove();
                currentNode.tryToReduceDepth();
                this.nodesOnEachDepth.set(currentNode.getDepth(), this.nodesOnEachDepth.get(currentNode.getDepth()) - 4);
                if (currentNode.getDepth() == this.currentDepth - 1 && this.nodesOnEachDepth.get(currentNode.getDepth()) == 0) {
                    this.currentDepth--;
                    this.nodesOnEachDepth.remove(currentNode.getDepth());
                }
                if (currentNode.getDepth() > newDepth) {
                    if (!sectorsToReduceDepth.contains(currentNode.getParent()))
                        sectorsToReduceDepth.add(currentNode.getParent());

                }
            }
            //this.currentDepth = newDepth;
            //this.actualiseNumberOfDeepest();
        }

        this.maxDepth = newDepth;
    }

    /**
     * Zoberie stredy 20% dat s najmesim prekryvanim a priemer ich stredov urcime ako novy stred stromu.
     * Nasledne okolo neho vytvorime root tak, aby obsiahol vsetky data.
     * Potom zvacsujeme hlbku stromu dovtedy, dokym nebude aktualna hlbka stromu mensia ako maximalna.
     */
    public void optimise() {
        Coordinate[] newCoordinates = this.calculateOptimalCoordinatesOfTree();

        // vytvori novy root podla novych suradnic a naplni ho datami
        ArrayList<IData> allData = this.getAllData();
        this.root = new QuadTreeNode(newCoordinates[0], newCoordinates[1], null, 1);
        this.coordinates = this.root.getCoordinates();
        this.currentDepth = 1;
        this.nodesOnEachDepth = new ArrayList<>();
        this.nodesOnEachDepth.add(1);
        for (IData data : allData) {
            if (!this.insert(data))
                throw new NullPointerException("the new root have been recalculated wrongly");
        }

        // v pripade potreby zvacsuje maximalnu hlbku kym vsetky data nie su rozdelene do nodov optimalne
        this.increaseDepthToMaxPossible();
    }

    public void optimise2() {
        ArrayList<IData> allDataList = this.getAllData();
        ArrayList<Coordinate> middles = new ArrayList<>();
        double northestPosition = Double.MIN_VALUE, southestPosition = Double.MAX_VALUE;
        double westestPosition = Double.MAX_VALUE, eastiestPosition = Double.MIN_VALUE;
        for (IData data : allDataList) {
            middles.add(data.getCoordinates()[0].getMiddle(data.getCoordinates()[1]));
            northestPosition = Math.max(northestPosition, data.getCoordinates()[0].getNorthWidthPosition());
            southestPosition = Math.min(southestPosition, data.getCoordinates()[1].getNorthWidthPosition());
            westestPosition = Math.min(westestPosition, data.getCoordinates()[0].getEastLengthPosition());
            eastiestPosition = Math.max(eastiestPosition, data.getCoordinates()[1].getEastLengthPosition());
        }
        int[] furthestFound = new int[2];
        double biggestDistance = -1;
        for (int i = 0; i < middles.size(); i++) {
            double[] middleVector1 = middles.get(i).getVectorPosition();
            for (int j = 0; j < middles.size(); j++) {
                double[] middleVector2 = middles.get(j).getVectorPosition();
                double currentDistance = Math.sqrt(Math.pow(middleVector1[0] - middleVector2[0], 2) + Math.pow(middleVector1[1] - middleVector2[1], 2));
                if (currentDistance > biggestDistance) {
                    biggestDistance = currentDistance;
                    furthestFound[0] = i;
                    furthestFound[1] = j;
                }
            }
        }
        Coordinate newMiddle = middles.get(furthestFound[0]).getMiddle(middles.get(furthestFound[1]));
        double biggestWidthDifference = Math.max(Math.abs(northestPosition - newMiddle.getNorthWidthPosition()), Math.abs(newMiddle.getNorthWidthPosition() - southestPosition)) + 1;
        double biggestLengthDifference = Math.max(Math.abs(westestPosition - newMiddle.getLengthPosition()), Math.abs(newMiddle.getLengthPosition() - eastiestPosition)) + 1;
        Coordinate[] newCoordinates = new Coordinate[]{new Coordinate(Width.N, newMiddle.getNorthWidthPosition() + biggestWidthDifference, Length.E, newMiddle.getEastLengthPosition() - biggestLengthDifference),
                new Coordinate(Width.N, newMiddle.getNorthWidthPosition() - biggestWidthDifference, Length.E, newMiddle.getEastLengthPosition() + biggestLengthDifference)};
        this.root = new QuadTreeNode(newCoordinates[0], newCoordinates[1], null, this.maxDepth);
        this.coordinates = this.root.getCoordinates();
        this.currentDepth = 1;
        this.nodesOnEachDepth = new ArrayList<>();
        this.nodesOnEachDepth.add(1);
        for (IData data : allDataList) {
            if (!this.insert(data))
                throw new NullPointerException("the new root have been recalculated wrongly");
        }
    }

    /**
     * Ak je strom hlboky ako maximalna hlbka vyskusa strom prehlbovat dokym nebude maximalna hlbka vacsia ako aktualna.
     * 50% aktualneho zdravia je potom pomer aktualnej maximalnej k najmensej maximalnej hlbke, po ktorej zvacseni by
     * sa strom uz neprehlboval.
     * Zvysnych 50% sa pocita tak, ze sa vypocita doplnok do jedna k podielu aktualnej vzdialenosti sstredu od optimalneho
     * stredu ku teoreticky najhorsej vzdialenosti stredu od optimalneho a teda keby sa nachadzal v jednom z vrchlov
     * urcujucich koren stromu. Vysledok sa vynasobi 50.
     * @return zdravie stormu v percentach
     */
    public int getHealth() {
        double health = 0;

        int currentMaxDepth = this.maxDepth;
        this.increaseDepthToMaxPossible();
        health = 50 * (currentMaxDepth / (double)(this.maxDepth - 1));
        this.changeDepth(currentMaxDepth);

        Coordinate[] optimalCoordinates = this.calculateOptimalCoordinatesOfTree();
        Coordinate optimalMiddle = optimalCoordinates[0].getMiddle(optimalCoordinates[1]);
        Coordinate currentMiddle = this.root.getCoordinates()[0].getMiddle(this.root.getCoordinates()[1]);
        double[] optimalMiddleVector = optimalMiddle.getVectorPosition();
        double[] currentMiddleVector = currentMiddle.getVectorPosition();
        double[] furthestOptimalVector = this.root.getCoordinates()[0].getVectorPosition();

        double currentDistance = Math.sqrt(Math.pow(optimalMiddleVector[0] - currentMiddleVector[0], 2) + Math.pow(optimalMiddleVector[1] - currentMiddleVector[1], 2));
        double worstDistance = Math.sqrt(Math.pow(furthestOptimalVector[0] - currentMiddleVector[0], 2) + Math.pow(furthestOptimalVector[1] - currentMiddleVector[1], 2));
        health += 50 * (1 - (currentDistance / worstDistance));

        return (int) health;
    }

    /**
     *
     * @return true ak je zdravie stromu mensie ako 60%
     */
    public boolean optimalisationNeeded() {
        if (this.getHealth() < 60)
            return true;
        return false;
    }

    public void turnOnAutoOptimalisation() { this.autoOptimise = true; }
    public void turnOffAutoOptimalisation() { this.autoOptimise = false; }

    public boolean isEmpty() {
        if (this.root.getDataList().size() == 0) {
            if (this.root.gotChildren()) {
                return false;
            }
            return true;
        }
        return false;
    }

    public Coordinate[] getCoordinates() {
        return coordinates;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    /**
     *
     * @param coordinates
     * @return Najmensi existujuci sektor stromu, do ktoreho sa suradnice zmestia. Vrati null ak sa suradnice nenachadzaju v strome.
     */
    private QuadTreeNode findSector(Coordinate[] coordinates, QuadTreeNode startSector) {
        for (Coordinate coordinate : coordinates) {
            if (!this.root.containsCoordinate(coordinate))
                return null;
        }

        boolean sectorFound = false;
        QuadTreeNode nodeFound = startSector;
        while (!sectorFound){
            QuadTreeNode tmpNode = nodeFound.findNextNode(coordinates);
            if (tmpNode == nodeFound) {
                sectorFound = true;
            } else {
                nodeFound = tmpNode;
            }
        }
        return nodeFound;
    }

    /**
     * Zvysuje hlbku stromu dokym nie je maximalna hlbka stromu o jedno vacsia ako aktualna
     */
    private void increaseDepthToMaxPossible() {
        if (this.currentDepth == this.maxDepth) {
            boolean increaseDepth = true;
            while (increaseDepth) {
                this.changeDepth(this.maxDepth + 1);
                if (this.currentDepth < maxDepth)
                    increaseDepth = false;
            }
        }
    }

    /**
     *
     * @return ArrayList vsetkych dat co sa v strome nachadzaju
     */
    public ArrayList<IData> getAllData() {
        ArrayList<IData> allData = new ArrayList<IData>();
        Queue<QuadTreeNode> sectors = new LinkedList<QuadTreeNode>();
        sectors.add(this.root);

        while (!sectors.isEmpty()) {
            QuadTreeNode sector = sectors.remove();
            allData.addAll(sector.getDataList());
            if (sector.gotChildren())
                sectors.addAll(Arrays.asList(sector.getChildren()));
        }

        return allData;
    }

    /**
     * Zoberie stredy 20% dat s najmesim prekryvanim a priemer ich stredov urcime ako novy stred stromu.
     * Nasledne okolo neho vytvorime obdlznik tak, aby obsiahol vsetky data.
     * @return suradnice urcujuce optimalny koren stromu
     */
    private Coordinate[] calculateOptimalCoordinatesOfTree(){
        Queue<Double> widthPositions = new LinkedList<Double>(); // - bude skladovat pozicie vysok stredov jednotlivych prvkov pre vysku N
        Queue<Double> lengthPositions = new LinkedList<Double>(); // - bude skladovat dlzok vysok stredov jednotlivych prvkov pre dlzku E
        Queue<Integer> numberOfCollisions = new LinkedList<Integer>(); // - bude skladovat pocty prekryti jednotlivych prvkov
        ArrayList<Integer> numberOfCollisionsForIndex = new ArrayList<Integer>(); // - skladovat pocet prekryti pre dany index
        ArrayList<Integer> numberOfDataForCollisions = new ArrayList<Integer>(); // - skladovat pocet prvkov s danym poctom prekryti pre dany index
        Queue<IData> allData = new LinkedList<IData>();
        int numberOfData = 0;
        double northestPosition = 0, southestPosition = 0;
        double westestPosition = 0, eastiestPosition = 0; // - dlzky a sirky budu nastavovane podla suradnicoveho systemu ploch teda southestPosition je najextremnejsia hodnota maxCoordinate cize najnizsie polozena


        // Prejde vsetky nody, vytiahne a pouklada vsetky data a potrebne informacie o nich
        Queue<QuadTreeNode> sectors = new LinkedList<QuadTreeNode>();
        sectors.add(this.root);

        while (!sectors.isEmpty()) {
            QuadTreeNode currentNode = sectors.remove();
            for (IData data : currentNode.getDataList()){
                allData.add(data);
                if (numberOfData > 0) {
                    northestPosition = Math.max(northestPosition, data.getCoordinates()[0].getNorthWidthPosition());
                    southestPosition = Math.min(southestPosition, data.getCoordinates()[1].getNorthWidthPosition());
                    westestPosition = Math.min(westestPosition, data.getCoordinates()[0].getEastLengthPosition());
                    eastiestPosition = Math.max(eastiestPosition, data.getCoordinates()[1].getEastLengthPosition());
                }

                Coordinate middlePoint = data.getCoordinates()[0].getMiddle(data.getCoordinates()[1]);
                widthPositions.add(middlePoint.getNorthWidthPosition());
                lengthPositions.add(middlePoint.getEastLengthPosition());
                numberOfData++;

                ArrayList<IData> dataColided = this.find(data.getCoordinates()[0], data.getCoordinates()[1]);
                int collisions = dataColided.size();
                numberOfCollisions.add(collisions);
                if (!numberOfCollisionsForIndex.contains(collisions)) {
                    numberOfCollisionsForIndex.add(collisions);
                    numberOfDataForCollisions.add(1);
                } else {
                    int index = numberOfCollisionsForIndex.indexOf(collisions);
                    numberOfDataForCollisions.set(index, numberOfDataForCollisions.get(index) + 1);
                }
            }

            if (currentNode.gotChildren())
                sectors.addAll(Arrays.asList(currentNode.getChildren()));

        }

        //bubblesort pre collisions for index (chcel som quicksort ale nebol cas na prerabku z rekurzie)
        for (int i = 0; i < numberOfCollisionsForIndex.size(); i++) {
            boolean swapped = false;
            for (int j = 0; j < numberOfCollisionsForIndex.size() - i - 1; j++) {
                if (numberOfCollisionsForIndex.get(j) < numberOfCollisionsForIndex.get(j + 1)) {
                    int tmp = numberOfCollisionsForIndex.get(j);
                    numberOfCollisionsForIndex.set(j, numberOfCollisionsForIndex.get(j + 1));
                    numberOfCollisionsForIndex.set(j + 1, tmp);

                    tmp = numberOfDataForCollisions.get(j);
                    numberOfDataForCollisions.set(j, numberOfDataForCollisions.get(j + 1));
                    numberOfDataForCollisions.set(j + 1, tmp);

                    swapped = true;
                }
            }
            if (!swapped)
                break;
        }

        //vytiahneme si len tie pocty kolizii, ktore potrebujeme
        int numberOfDataToFind = (int) (numberOfData * 0.2);
        int numberOfDataFitting = 0;
        ArrayList<Integer> newNumberOfCollisionsForIndex = new ArrayList<Integer>();
        ArrayList<Integer> newNumberOfDataForCollisions = new ArrayList<Integer>();
        for (int i = 0; i < numberOfCollisionsForIndex.size(); i++) {
            numberOfDataFitting += numberOfCollisionsForIndex.get(i);
            newNumberOfCollisionsForIndex.add(numberOfCollisionsForIndex.get(i));
            newNumberOfDataForCollisions.add(numberOfDataForCollisions.get(i));
            if (numberOfDataFitting >= numberOfDataToFind)
                break;
        }
        numberOfCollisionsForIndex.clear();
        numberOfDataForCollisions.clear();


        // vypocet vysky a dlzky stredneho bodu noveho rozsahu stromu
        double averageWidth = 0, averageLength = 0;
        int numberOfDataFound = 0;
        while (!numberOfCollisions.isEmpty()) {
            double currentWidth = widthPositions.remove();
            double currentLength = lengthPositions.remove();
            int currentNumberOfCollisions = numberOfCollisions.remove();
            if (newNumberOfCollisionsForIndex.contains(currentNumberOfCollisions)) {
                if (numberOfDataFound > 0) {
                    averageWidth = (averageWidth + currentWidth) / 2;
                    averageLength = (averageLength + currentLength) / 2;
                    numberOfDataFound++;
                } else {
                    averageWidth = currentWidth;
                    averageLength = currentLength;
                    numberOfDataFound++;
                }
            }
        }

        //Vypocita
        double biggestWidthDifference = Math.max(Math.abs(northestPosition - averageWidth), Math.abs(averageWidth - southestPosition)) + 1;
        double biggestLengthDifference = Math.max(Math.abs(westestPosition - averageLength), Math.abs(averageLength - eastiestPosition)) + 1;
        return new Coordinate[]{new Coordinate(Width.N, averageWidth + biggestWidthDifference, Length.E, averageLength - biggestLengthDifference),
                new Coordinate(Width.N, averageWidth - biggestWidthDifference, Length.E, averageLength + biggestLengthDifference)};
    }
}

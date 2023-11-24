package Model.QuadTree;

import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.CoordinateComputer;
import Model.QuadTree.Data.IData;

import java.util.ArrayList;

public class QuadTreeNode {
    private QuadTreeNode[] children = new QuadTreeNode[4];
    private Coordinate[] coordinates;
    private QuadTreeNode parent;
    private ArrayList<IData> dataList;
    private int depth;

    public QuadTreeNode(Coordinate minCoordinate, Coordinate maxCoordinate, QuadTreeNode parent, int actualDepth) {
        this.depth = actualDepth;
        this.coordinates = CoordinateComputer.normalizeCoordinates(minCoordinate, maxCoordinate);
        this.parent = parent;
        this.dataList = new ArrayList<IData>();
    }

    /**
     * Metoda predpoklada,ze list v ktorom je zavolana je najnizsi najdeny
     * @param newData
     * @param increaseDepthPossibility
     * @return
     * @throws Exception
     */
    public boolean tryInsert(IData newData, boolean increaseDepthPossibility) throws Exception {
        if (this.dataList.size() == 0 || !increaseDepthPossibility) {
            newData.setCoordinates(CoordinateComputer.normalizeCoordinates(newData.getCoordinates()[0], newData.getCoordinates()[1]));
            this.dataList.add(newData);
            return true;
        }

        if (!this.gotChildren()) {
            this.increaseDepth();
        } else {
            for (IData data : this.dataList) {
                if (newData.equals(data)) {
                    throw new Exception("Data with same keys are alrady in the list");
                }
            }
            this.dataList.add(newData);
            return true;
        }

        return false;
    }

    public IData remove(IData data) {
        IData result = null;
        for (int i = 0; i < this.dataList.size(); i++) {
            if (this.dataList.get(i).equals(data)) {
                result = this.dataList.remove(i);
            }
        }
        
        this.optimiseNode();
        
        return result;
    }

    public int getDepth() {
        return depth;
    }

    public Coordinate[] getCoordinates() {
        return coordinates;
    }

    public QuadTreeNode[] getChildren() {
        return children;
    }

    public ArrayList<IData> getDataList() {
        return dataList;
    }

    public QuadTreeNode getParent() {
        return parent;
    }

    /**
     *
     * @param coordinates
     * @return vrati subnode do ktoreho obe suradnice patria. Vrati sameho seba ak patri do viacerych potomkov
     */
    public QuadTreeNode findNextNode(Coordinate[] coordinates){
        if (children[0] == null)
            return this;

        for (QuadTreeNode child : children) {
            boolean containsCoordinates = true;
            for (Coordinate coordinate : coordinates) {
                if (!child.containsCoordinate(coordinate))
                    containsCoordinates = false;
            }
            if (containsCoordinates)
                return child;
        }
        return this;
    }

    /**
     * Optimalizuje node v pripade ze jeho deti nemaju deti cize ak nema data, zisti, ze nema vnukov a data obsahuje max
     * jedno z jeho deti tak si ulozi data toho dietata presunie do seba a deti vymaze
     * @return true ak operacie optimalizacie prebehli. false ak nie.
     */
    public boolean optimiseNode(){
        boolean optimalisationNeed = false;

        if (this.dataList.size() == 0) {
            if (this.gotChildren()) {
                int filledChildren = 0;
                optimalisationNeed = true;

                for (QuadTreeNode child : this.children) {
                    if (child.gotChildren()) {
                        optimalisationNeed = false;
                        break;
                    }
                    if (child.dataList.size() > 0) {
                        filledChildren++;
                        if (filledChildren > 1) {
                            optimalisationNeed = false;
                            break;
                        }
                    }
                }

                if (optimalisationNeed) {
                    this.tryToReduceDepth();
                }
            }
        }

        return optimalisationNeed;
    }

    /**
     *
     * @param coordinate
     * @return true ak sa suradnica nachadza v danom node
     */
    public boolean containsCoordinate(Coordinate coordinate) {
        return CoordinateComputer.containsCoordinate(this.coordinates[0], this.coordinates[1], coordinate);
    }

    public boolean tryToIncreaseMyDepth() {
        if (!this.gotChildren() && this.dataList.size() > 1){
            this.increaseDepth();
            return true;
        }
        return false;
    }

    private void increaseDepth() {
        Coordinate middleCoordinate = coordinates[0].getMiddle(coordinates[1]);
        this.children[0] = new QuadTreeNode(this.coordinates[0], middleCoordinate, this, this.depth + 1);
        this.children[1] = new QuadTreeNode(
                new Coordinate(this.coordinates[0].getWidth(), this.coordinates[0].getWidthPosition(),
                        middleCoordinate.getLength(), middleCoordinate.getLengthPosition()),
                new Coordinate(middleCoordinate.getWidth(), middleCoordinate.getWidthPosition(),
                        this.coordinates[1].getLength(), this.coordinates[1].getLengthPosition()), this, this.depth + 1);
        this.children[2] = new QuadTreeNode(
                new Coordinate(middleCoordinate.getWidth(), middleCoordinate.getWidthPosition(),
                        this.coordinates[0].getLength(), this.coordinates[0].getLengthPosition()),
                new Coordinate(this.coordinates[1].getWidth(), this.coordinates[1].getWidthPosition(),
                        middleCoordinate.getLength(), middleCoordinate.getLengthPosition()), this, this.depth + 1);
        this.children[3] = new QuadTreeNode(middleCoordinate, this.coordinates[1], this, this.depth + 1);

        ArrayList<IData> newDataList = new ArrayList<IData>();
        for (IData data : this.dataList) {
            QuadTreeNode nextNode = this.findNextNode(data.getCoordinates());
            if (nextNode == this) {
                newDataList.add(data);
            } else {
                nextNode.dataList.add(data);
            }
        }
        this.dataList = newDataList;
    }

    /**
     * Ak nema vnucata zrusi potomkov a ich data si privlastni
     * @return
     */
    public boolean tryToReduceDepth() {
        if (!this.gotChildren())
            return false;

        for (QuadTreeNode child : this.children) {
            if (child.gotChildren())
                return false;
        }

        for (int i = 0; i < this.children.length; i++) {
            this.dataList.addAll(this.children[i].dataList);
            this.children[i] = null;
        }
        return true;
    }
    
    public boolean gotChildren() {
        boolean got = false;
        for (QuadTreeNode node : this.children) {
            if (node != null) {
                got = true;
                break;
            }
        }
        return got;
    }
}

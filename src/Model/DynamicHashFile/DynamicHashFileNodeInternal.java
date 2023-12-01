package Model.DynamicHashFile;

import java.util.BitSet;

public class DynamicHashFileNodeInternal extends DynamicHashFileNode {
    private DynamicHashFileNode leftSon, rightSon;

    public DynamicHashFileNodeInternal(int depth, DynamicHashFileNode parent, DynamicHashFileNode leftSon, DynamicHashFileNode rightSon) {
        super(depth, parent);
        this.leftSon = leftSon;
        this.rightSon = rightSon;
        this.leftSon.setParent(this);
        this.rightSon.setParent(this);
    }

    public DynamicHashFileNode getLeftSon() {
        return leftSon;
    }

    public DynamicHashFileNode getRightSon() {
        return rightSon;
    }

    public void setLeftSon(DynamicHashFileNode leftSon) {
        this.leftSon = leftSon;
    }

    public void setRightSon(DynamicHashFileNode rightSon) {
        this.rightSon = rightSon;
    }

    public DynamicHashFileNode getNextNode(BitSet hash) {
        if (hash.get(this.depth)) {
            return this.rightSon;
        }
        return this.leftSon;
    }
}

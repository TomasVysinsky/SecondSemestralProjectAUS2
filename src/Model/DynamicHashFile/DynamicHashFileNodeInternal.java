package Model.DynamicHashFile;

import java.util.BitSet;

public class DynamicHashFileNodeInternal extends DynamicHashFileNode {
    private DynamicHashFileNode leftSon, rightSon;

    public DynamicHashFileNodeInternal(int depth, DynamicHashFileNode parent, DynamicHashFileNode leftSon, DynamicHashFileNode rightSon) {
        super(depth, parent);
        this.leftSon = leftSon;
        this.rightSon = rightSon;
        if (this.leftSon != null)
            this.leftSon.setParent(this);
        if (this.rightSon != null)
            this.rightSon.setParent(this);
    }

    public DynamicHashFileNode getLeftSon() {
        return leftSon;
    }

    public DynamicHashFileNode getRightSon() {
        return rightSon;
    }

    public void setLeftSon(DynamicHashFileNode leftSon) {
        if (leftSon != null) {
            this.leftSon = leftSon;
            this.leftSon.setParent(this);
        }
    }

    public void setRightSon(DynamicHashFileNode rightSon) {
        if (rightSon != null) {
            this.rightSon = rightSon;
            this.rightSon.setParent(this);
        }
    }

    public DynamicHashFileNode getNextNode(BitSet hash) {
        int size = hash.size();

        /*for (int i = 0; i < size; i++) {
            System.out.print(hash.get(i));
        }
        System.out.println();
        System.out.println(hash);*/
        if (size == 0) {
//            System.out.println(hash.get(this.depth));
            if (hash.get(this.depth))
                return this.rightSon;
        } else if (hash.get(size - this.depth)) {
//            System.out.println(hash.get(size - this.depth));
            return this.rightSon;
        }

        return this.leftSon;
    }
}

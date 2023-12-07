package Model.DynamicHashFile;

public abstract class DynamicHashFileNode {
    protected int depth;
    protected DynamicHashFileNode parent;

    public DynamicHashFileNode(int depth, DynamicHashFileNode parent) {
        this.depth = depth;
        this.parent = parent;
    }

    public void setParent(DynamicHashFileNode parent) {
        this.parent = parent;
    }

    public DynamicHashFileNode getParent() {
        return parent;
    }

    public int getDepth() {
        return depth;
    }
    public void increaseDepthBy(int number) { this.depth += number; }
}

package Model.DynamicHashFile;

import Model.DynamicHashFile.Data.IRecord;

public class DynamicHashFileNodeExternal extends DynamicHashFileNode {
    private int count;
    private int address;

    public DynamicHashFileNodeExternal(int count, int depth, DynamicHashFileNode parent) {
        super(depth, parent);
        this.address = -1;
        this.count = count;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    public void setCount(int count) { this.count = count; }

    public int getAddress() { return address; }

    public int getCount() { return count; }
}

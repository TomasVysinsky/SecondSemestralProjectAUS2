package Model.DynamicHashFile.Data;

import java.util.ArrayList;

public class Block<T extends IRecord> {
    private ArrayList<IRecord> records;
    private int size;

    public Block(int size) {
        this.size = size;
    }

    public int getSize(){
        return this.size;
    }

    public int getValidCount() {
        return this.records.size();
    }
}

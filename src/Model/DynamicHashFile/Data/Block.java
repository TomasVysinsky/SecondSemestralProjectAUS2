package Model.DynamicHashFile.Data;

import Model.QuadTree.Coordinates.Coordinate;

import java.nio.ByteBuffer;

public class Block <T extends IRecord> {
    private IRecord[] records;
    private int validCount;
    private int nextBlock;
    private Class<T> type;

    public Block(int blockFactor, Class<T> type) {
        this.type = type;
        this.validCount = 0;
        this.records = new IRecord[blockFactor];
        this.nextBlock = -1;
//        for (int i = 0; i < this.records.length; i++) {
//            this.records[i] = this.getInstance();
//        }
    }

    public boolean insert(T record) {
        if (this.validCount < this.records.length) {
            this.records[this.validCount] = record;
            this.validCount++;
            return true;
        }
        return false;
    }

    public T find(IRecord keyRecord) {
        for (int i = 0; i < this.validCount; i++)
            if (this.records[i].getHash().equals(keyRecord.getHash()))
                return (T)this.records[i];
        return null;
    }

    public IRecord delete(T record) {
        IRecord deleted = null;
        for (int i = 0; i < this.validCount; i++) {
            if (this.records[i].equals(record)){
                deleted = this.records[i];
                this.records[i] = this.records[this.validCount];
                this.validCount--;
                break;
            }
        }
        return deleted;
    }

    public void setNextBlock(int nextBlock) {
        this.nextBlock = nextBlock;
    }

    public int getValidCount(){
        return this.validCount;
    }

    public IRecord[] getRecords() {
        return records;
    }

    /**
     *
     * @return velkost blocku v bytoch
     */
    public int getSize() {
        try {
            return Integer.BYTES * 2 + this.type.newInstance().getSize() * this.records.length;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getSize());
        byteBuffer.putInt(this.validCount);
        byteBuffer.putInt(this.nextBlock);
        for (int i = 0; i < this.validCount; i++) {
            byteBuffer.put(this.records[i].toByteArray());
        }
        return byteBuffer.array();
    }
    public void fromByteArray(byte[] bytes) {
        if (bytes.length == this.getSize()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
            this.validCount = byteBuffer.getInt();
            this.nextBlock = byteBuffer.getInt();
            for (int i = 0; i < this.validCount; i++) {
                byte[] newArray;
                try {
                    newArray = new byte[this.type.newInstance().getSize()];
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }
                for (int j = 0; j < newArray.length; j++) {
                    newArray[j] = byteBuffer.get();
                }
                try {
                    this.records[i] = this.type.newInstance();
                    this.records[i].fromByteArray(newArray);
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }
            }
            return;
        }
        throw new ArrayIndexOutOfBoundsException("Pole ma inu dlzku");
    }
}

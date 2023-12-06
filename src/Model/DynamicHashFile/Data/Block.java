package Model.DynamicHashFile.Data;

import java.nio.ByteBuffer;

public class Block <T extends IRecord> {
    boolean active;
    private IRecord[] records;
    private int validCount;
    private int nextBlock;
    private Class<T> type;

    public Block(int blockFactor, Class<T> type) {
        this.active = false;
        this.type = type;
        this.validCount = 0;
        this.records = new IRecord[blockFactor];
        this.nextBlock = -1;
//        for (int i = 0; i < this.records.length; i++) {
//            this.records[i] = this.getInstance();
//        }
    }

    public boolean insert(T record) {
        if (this.active && this.validCount < this.records.length) {
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setNextBlock(int nextBlock) {
        this.nextBlock = nextBlock;
    }

    /**
     * Ak je block neplatny, nastavi predchadzajuci block do atributu validCount.
     * V opacnom pripade nespravi nic
     * @param previousBlock
     */
    public void setPreviousBlockIfInactive(int previousBlock ) {
        if (!this.active) {
            this.validCount = previousBlock;
        }
    }

    public void setValidCount(int validCount) {
        if (!this.active) {
            this.validCount = validCount;
        }
    }

    public boolean isActive() {
        return active;
    }

    /**
     *
     * @return Ak je block platny vrati pocet platnych blockov. Ak nie, vrati 0.
     */
    public int getValidCount(){
        if (this.active) {
            return this.validCount;
        }
        return 0;
    }

    /**
     *
     * @return Ak je block neplatny vrati poziciu predchadzajuceho blocku. Ak nie, vrati -2.
     */
    public int getPreviousBlockIfInactive() {
        if (!this.active) {
            return this.validCount;
        }
        return -2;
    }

    public int getNextBlock() {
        return nextBlock;
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
            // active + nextBlock + validCount + records size
            return Character.BYTES + Integer.BYTES * 2 + this.type.newInstance().getSize() * this.records.length;
        } catch (Exception e) {
            System.out.println(e);
            return -1;
        }
    }

    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(this.getSize());
        if (this.active) {
            byteBuffer.putChar('T');
        } else {
            byteBuffer.putChar('F');
        }
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
            this.active = (byteBuffer.getChar() == 'T');
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

package Model.DynamicHashFile;

public class DynamicHashFileNodeExternal extends DynamicHashFileNode {
    private int count;
    private int address;
    private int capacity;

    public DynamicHashFileNodeExternal(int count, int depth, DynamicHashFileNode parent) {
        super(depth, parent);
        this.address = -1;
        this.count = count;
        this.capacity = 0;
    }

    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * Zvysi count o zadany parameter
     * @param count
     */
    public void increaseCountBy(int count) { this.count += count; }

    /**
     * Zvysi capacity o zadany parameter
     * @param capacity
     */
    public void increaseCapacityBy(int capacity) { this.capacity += capacity; }

    public int getAddress() { return address; }

    public int getCount() { return count; }

    /**
     * Vrati pocet volnych alokovanych miest
     * @return
     */
    public int getFreeCapacity() { return this.capacity - this.count; }
}

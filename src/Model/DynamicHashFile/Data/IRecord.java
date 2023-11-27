package Model.DynamicHashFile.Data;

import java.util.BitSet;

public interface IRecord {
    public boolean equals(IRecord other);
    public BitSet getHash();
    public int getSize();
    public byte[] toByteArray(char[] array);
    public char[] fromByteArray(byte[] bytes);
}

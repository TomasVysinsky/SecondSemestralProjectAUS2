package Model.DynamicHashFile.Data;

import java.util.BitSet;

public interface IRecord {
    public boolean equals(IRecord other);
    public BitSet getHash();

    /**
     *
     * @return Velkost v bytoch
     */
    public int getSize();
    public byte[] toByteArray();
    public void fromByteArray(byte[] bytes);
}

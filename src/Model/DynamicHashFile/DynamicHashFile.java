package Model.DynamicHashFile;

import Model.DynamicHashFile.Data.Block;
import Model.DynamicHashFile.Data.IRecord;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class DynamicHashFile <T extends IRecord> {
    private DynamicHashFileNode root;
    private int blockFactor;
    private int maxDepth;
    private int firstFreeBlock;
    private RandomAccessFile file, overflowFile;
    private Class<T> type;

    /**
     *
     * @param blockFactor
     * @param maxDepth
     * @param fileName String, adresa binarneho suboru bez suffixu
     */
    public DynamicHashFile(int blockFactor, int maxDepth, String fileName, Class<T> type) {
        this.blockFactor = blockFactor;
        this.maxDepth = maxDepth;
        this.type = type;
        try {
            this.file = new RandomAccessFile(fileName + ".bin", "rw");
        } catch (Exception e) {
            return;
        }
        this.root = new DynamicHashFileNodeExternal<T>(0, 0, null);
        this.firstFreeBlock = -1;
    }

    public boolean insert(T record) {
        if (record != null) {
            DynamicHashFileNode current = this.root;
            while (current instanceof DynamicHashFileNodeInternal) {
                current = ((DynamicHashFileNodeInternal) current).getNextNode(record.getHash());
            }
            DynamicHashFileNodeExternal<T> external = (DynamicHashFileNodeExternal<T>) current;

            if (external.getAddress() == -1) {
                //Vetva v pripade ze externy node nema alokovany block

                Block<T> newBlock = new Block<T>(this.blockFactor, this.type);
                if (this.firstFreeBlock == -1) {
                    try {
                        this.file.setLength(this.file.length() + newBlock.getSize());
                        external.setAddress((int)(this.file.length() / newBlock.getSize()));
                    } catch (Exception e) {
                        System.out.println(e);
                        return false;
                    }
                } else {
                    //TODO pouzit existujuci prazdny block - dorobit po delete
                }
                newBlock.insert(record);
                external.setCount(1);
                try {
                    this.writeBlock(external.getAddress(), this.file, newBlock);
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
                return true;
            } else {
                boolean inserted = false;
                while (!inserted) {
                    if (external.getCount() == this.blockFactor && external.getDepth() < this.maxDepth) {
                        // Vetva v pripade ze je externy node naplneny
                        //TODO reorganizacia stromu


                        DynamicHashFileNodeExternal<T> leftSon = new DynamicHashFileNodeExternal<T>(0, external.getDepth() + 1, null);
                        DynamicHashFileNodeExternal<T> rightSon = new DynamicHashFileNodeExternal<T>(0, external.getDepth() + 1, null);
                        DynamicHashFileNodeInternal newInternal = new DynamicHashFileNodeInternal(external.getDepth(), external.getParent(), leftSon, rightSon);
                        Block<T> leftSonBlock = new Block<T>(this.blockFactor, this.type);
                        Block<T> rigthSonBlock = new Block<T>(this.blockFactor, this.type);
                        Block<T> originalBlock = this.readBlock(external.getAddress(), file);
                        if (originalBlock == null) {
                            System.out.println("Chyba pri nacitani blocku v inserte");
                            return false;
                        }
                        this.freeTheBlock(external.getAddress());
                        IRecord[] originalArray = originalBlock.getRecords();

                        for (IRecord currentRecord : originalArray) {
                            DynamicHashFileNodeExternal<T> nextNode = (DynamicHashFileNodeExternal<T>) newInternal.getNextNode(currentRecord.getHash());
                            if (nextNode.getAddress() == -1) {
                                if (this.getFreeBlock() == -1) {
                                    try {
                                        this.file.setLength(this.file.length() + originalBlock.getSize());
                                        nextNode.setAddress((int) (this.file.length() / originalBlock.getSize()));
                                    } catch (Exception e) {
                                        System.out.println(e);
                                        return false;
                                    }
                                } else {
                                    // TODO prerobit ked budem mat aj delete
                                    nextNode.setAddress(this.getFreeBlock());
                                    this.firstFreeBlock = -1;
                                }
                            }
                            if (nextNode == leftSon) {
                                leftSonBlock.insert((T) currentRecord);
                            } else {
                                rigthSonBlock.insert((T) currentRecord);
                            }
                            nextNode.setCount(nextNode.getCount() + 1);
                        }

                        external = (DynamicHashFileNodeExternal<T>) newInternal.getNextNode(record.getHash());
                        if (external.getCount() < this.blockFactor) {
                            if (external == leftSon) {
                                leftSonBlock.insert((T) record);
                            } else {
                                rigthSonBlock.insert((T) record);
                            }
                            external.setCount(external.getCount() + 1);
                            try {
                                this.writeBlock(leftSon.getAddress(), this.file, leftSonBlock);
                                this.writeBlock(rightSon.getAddress(), this.file, rigthSonBlock);
                            } catch (IOException e) {
                                System.out.println(e);
                            }
                            inserted = true;
                        } else {
                            try {
                                this.writeBlock(leftSon.getAddress(), this.file, leftSonBlock);
                                this.writeBlock(rightSon.getAddress(), this.file, rigthSonBlock);
                            } catch (IOException e) {
                                System.out.println(e);
                            }
                        }
                    } else {
                        //TODO vlozenie do aktualneho blocku
                        if (external.getCount() < this.blockFactor) {
                            Block<T> currentBlock = this.readBlock(external.getAddress(), this.file);
                            if (currentBlock == null) {
                                System.out.println("Chyba pri nacitani blocku v inserte");
                                return false;
                            }
                            currentBlock.insert(record);
                            external.setCount(external.getCount() + 1);
                            try {
                                this.writeBlock(external.getAddress(), this.file, currentBlock);
                            } catch (IOException e) {
                                System.out.println(e);
                            }
                        } else {
                            // TODO preplnovaci subor
                            return false;
                        }
                        inserted = true;
                    }
                }
                return true;
            }
        }
        return false;
    }
    public T find(IRecord record) {
        // TODO spravit z tohoto kusku samostatnu metodu
        DynamicHashFileNode current = this.root;
        while (current instanceof DynamicHashFileNodeInternal) {
            current = ((DynamicHashFileNodeInternal) current).getNextNode(record.getHash());
        }
        DynamicHashFileNodeExternal<T> external = (DynamicHashFileNodeExternal<T>) current;

        return this.readBlock(external.getAddress(), file).find(record);
    }
    public boolean delete() { return false; }


    private Block<T> readBlock(int address, RandomAccessFile file) {
        Block<T> newBlock = new Block<T>(this.blockFactor, this.type);
        byte[] byteArray = new byte[newBlock.getSize()];
        try {
            file.read(byteArray, address * byteArray.length, byteArray.length);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        newBlock.fromByteArray(byteArray);
        return newBlock;
    }

    private void writeBlock(int address, RandomAccessFile file, Block<T> block) throws IOException {
        file.write(block.toByteArray(), address * block.getSize(), block.getSize());
    }

    public int getFreeBlock() {
        return firstFreeBlock;
    }

    public void freeTheBlock(int address) {
        // TODO freeTheBolck
        this.firstFreeBlock = address;
    }

    public DynamicHashFileNode getRoot() {
        return root;
    }

    public ArrayList<Block<T>> getAllBlocks() {
        ArrayList<Block<T>> blocks = new ArrayList<Block<T>>();
        int currentAddress = 0;
        Block<T> block = new Block<T>(this.blockFactor, this.type);
        long fileSize = 0;
        try {
            fileSize = this.file.length();
        } catch (Exception e) {
            System.out.println(e);
            return blocks;
        }

        while ((long) block.getSize() * currentAddress < fileSize) {
            blocks.add(this.readBlock(currentAddress, file));
            currentAddress++;
        }

        return blocks;
    }
}

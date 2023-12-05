package Model.DynamicHashFile;

import Model.DynamicHashFile.Data.Block;
import Model.DynamicHashFile.Data.IRecord;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class DynamicHashFile <T extends IRecord> {
    private DynamicHashFileNode root;
    private int blockFactor, overflowBlockFactor;
    private int maxDepth;
    private int firstFreeBlock;
    private RandomAccessFile file, overflowFile;
    private String fileName;
    private Class<T> type;

    /**
     *
     * @param blockFactor
     * @param maxDepth
     * @param fileName String, adresa binarneho suboru bez suffixu
     */
    public DynamicHashFile(int blockFactor, int overflowBlockFactor, int maxDepth, String fileName, Class<T> type) {
        this.blockFactor = blockFactor;
        this.overflowBlockFactor = overflowBlockFactor;
        this.maxDepth = maxDepth;
        this.type = type;
        try {
            this.file = new RandomAccessFile(fileName + ".bin", "rw");
            this.overflowFile = new RandomAccessFile(fileName + "Overflow.bin", "rw");
        } catch (Exception e) {
            return;
        }
        this.root = new DynamicHashFileNodeExternal(0, 0, null);
        this.firstFreeBlock = -1;
        this.fileName = fileName;


        File backup = new File(this.fileName + ".txt");
        if (backup.exists()) {
            System.out.println("Subor existuje");
        } else {
            System.out.println("Subor neexistuje");
        }
    }

    public boolean insert(T record) {
        if (record != null) {
            DynamicHashFileNode current = this.root;
            while (current instanceof DynamicHashFileNodeInternal) {
                current = ((DynamicHashFileNodeInternal) current).getNextNode(record.getHash());
            }
            DynamicHashFileNodeExternal external = (DynamicHashFileNodeExternal) current;

            if (external.getAddress() == -1) {
                // Vetva v pripade ze externy node nema alokovany block

                Block<T> newBlock = new Block<T>(this.blockFactor, this.type);
                if (this.firstFreeBlock == -1) {
                    try {
                        this.file.setLength(this.file.length() + newBlock.getSize());
                        external.setAddress((int)(this.file.length() / newBlock.getSize()) - 1);
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


                        DynamicHashFileNodeExternal leftSon = new DynamicHashFileNodeExternal(0, external.getDepth() + 1, null);
                        DynamicHashFileNodeExternal rightSon = new DynamicHashFileNodeExternal(0, external.getDepth() + 1, null);
                        DynamicHashFileNodeInternal newInternal = new DynamicHashFileNodeInternal(external.getDepth(), external.getParent(), leftSon, rightSon);
                        if (this.root == external) {
                            this.root = newInternal;
                        } else {
                            if (external.getParent() instanceof DynamicHashFileNodeInternal) {
                                DynamicHashFileNodeInternal parent = (DynamicHashFileNodeInternal) external.getParent();
                                if (parent.getLeftSon() == external) {
                                    parent.setLeftSon(newInternal);
                                } else {
                                    parent.setRightSon(newInternal);
                                }
                            }
                        }
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
                            DynamicHashFileNodeExternal nextNode = (DynamicHashFileNodeExternal) newInternal.getNextNode(currentRecord.getHash());
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

                        external = (DynamicHashFileNodeExternal) newInternal.getNextNode(record.getHash());
                        if (external.getCount() < this.blockFactor) {
                            if (external == leftSon) {
                                leftSonBlock.insert((T) record);
                            } else {
                                rigthSonBlock.insert((T) record);
                            }
                            external.setCount(external.getCount() + 1);
                            try {
                                if (leftSon.getAddress() != -1)
                                    this.writeBlock(leftSon.getAddress(), this.file, leftSonBlock);
                                if (rightSon.getAddress() != -1)
                                    this.writeBlock(rightSon.getAddress(), this.file, rigthSonBlock);
                            } catch (IOException e) {
                                System.out.println(e);
                            }
                            inserted = true;
                        } else {
                            try {
                                if (leftSon.getAddress() != -1)
                                    this.writeBlock(leftSon.getAddress(), this.file, leftSonBlock);
                                if (rightSon.getAddress() != -1)
                                    this.writeBlock(rightSon.getAddress(), this.file, rigthSonBlock);
                            } catch (IOException e) {
                                System.out.println(e);
                            }
                        }
                    } else {
                        // Vlozenie do aktualneho blocku
                        if (external.getCount() < this.blockFactor) {
                            Block<T> currentBlock = this.readBlock(external.getAddress(), this.file);
                            if (currentBlock == null) {
                                System.out.println("Chyba pri nacitani blocku v inserte");
                                return false;
                            }
                            // TODO kontrola ci sa moze vlozit do blocku (nesmie tam vyjst equals true)
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
        DynamicHashFileNodeExternal external = (DynamicHashFileNodeExternal) current;

        return this.readBlock(external.getAddress(), file).find(record);
    }
    public boolean delete() { return false; }


    private Block<T> readBlock(int address, RandomAccessFile file) {
        Block<T> newBlock = new Block<T>(this.blockFactor, this.type);
        byte[] byteArray = new byte[newBlock.getSize()];
        try {
            file.seek((long) address * byteArray.length);
//            file.read(byteArray, address * byteArray.length, byteArray.length);
            file.read(byteArray, address * byteArray.length, byteArray.length);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        newBlock.fromByteArray(byteArray);
        return newBlock;
    }

    private void writeBlock(int address, RandomAccessFile file, Block<T> block) throws IOException {
        file.seek((long) address * block.getSize());
        file.write(block.toByteArray());
    }

    public int getFreeBlock() {
        return firstFreeBlock;
    }

    public void freeTheBlock(int address) {
        // TODO freeTheBlock
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

    public String getTrieAsString() {
        Queue<DynamicHashFileNode> nodeQueue = new LinkedList<>();
        nodeQueue.add(this.root);
        boolean allDone = false;
        String result = "";

        while (!allDone) {
            DynamicHashFileNode currentNode = nodeQueue.remove();
            if (currentNode instanceof DynamicHashFileNodeExternal) {
                DynamicHashFileNodeExternal externalNode = (DynamicHashFileNodeExternal)currentNode;
                result = result + "\nExternal node, Depth: " + currentNode.getDepth() +
                        " Address: " + externalNode.getAddress() + " Count: " + externalNode.getCount();
//                System.out.println("External node, Depth: " + currentNode.getDepth() + " Address: " + externalNode.getAddress() + " Count: " + externalNode.getCount());
            } else {
                result = result + "\nInternal Node, Depth: " + currentNode.getDepth();
//                System.out.println("Internal Node, Depth: " + currentNode.getDepth());
                nodeQueue.add(((DynamicHashFileNodeInternal)currentNode).getLeftSon());
                nodeQueue.add(((DynamicHashFileNodeInternal)currentNode).getRightSon());
            }

            if (nodeQueue.isEmpty())
                allDone = true;
        }
        return result;
    }

    @Override
    protected void finalize(){
        try {
            this.file.close();
        } catch (IOException e) {
            System.out.println(e);
        }

    }
}

package Model.DynamicHashFile;

import Model.DynamicHashFile.Data.Block;
import Model.DynamicHashFile.Data.IRecord;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class DynamicHashFile <T extends IRecord> {
    private DynamicHashFileNode root;
    private int maxDepth;
    private BinaryFileManager<T> regularFile, overflowFile;
    private String fileName;
    private Class<T> type;

    /**
     *
     * @param blockFactor
     * @param maxDepth
     * @param fileName String, adresa binarneho suboru bez suffixu
     */
    public DynamicHashFile(int blockFactor, int overflowBlockFactor, int maxDepth, String fileName, Class<T> type) {
        this.maxDepth = maxDepth;
        this.type = type;
        this.regularFile = new BinaryFileManager<T>(blockFactor, fileName, type);
        this.overflowFile = new BinaryFileManager<T>(overflowBlockFactor, fileName + "Overflow", type);
        this.root = new DynamicHashFileNodeExternal(0, 1, null);
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
            DynamicHashFileNodeExternal external = this.findExternalNode(record);

            if (external.getAddress() == -1) {
                // Vetva v pripade ze externy node nema alokovany block
                int freeBlock = this.regularFile.getRemovedFreeBlock();
                if (freeBlock == -1)
                    return false;
                external.setAddress(freeBlock);
                Block<T> newBlock = this.regularFile.readBlock(external.getAddress());
                newBlock.insert(record);
                external.setCount(1);
                try {
                    this.regularFile.writeBlock(external.getAddress(), newBlock);
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
                return true;

            } else {
                boolean inserted = false;
                while (!inserted) {
                    if (external.getCount() == this.regularFile.getBlockFactor() && external.getDepth() < this.maxDepth) {
                        // Vetva v pripade ze je externy node naplneny a trie nie je na dne

                        // Vytvorenie novych nodov a umiestnenie
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

                        // Vytvorenie a naplnenie novych blockov existujucimi zaznamami
                        Block<T> leftSonBlock = new Block<T>(this.regularFile.getBlockFactor(), this.type);
                        Block<T> rigthSonBlock = new Block<T>(this.regularFile.getBlockFactor(), this.type);
                        leftSonBlock.setActive(true);
                        rigthSonBlock.setActive(true);
                        Block<T> originalBlock = this.regularFile.readBlock(external.getAddress());
                        if (originalBlock == null) {
                            System.out.println("Chyba pri nacitani blocku v inserte");
                            return false;
                        }
                        this.regularFile.freeTheBlock(external.getAddress());
                        IRecord[] originalArray = originalBlock.getRecords();

                        for (IRecord currentRecord : originalArray) {
                            DynamicHashFileNodeExternal nextNode = (DynamicHashFileNodeExternal) newInternal.getNextNode(currentRecord.getHash());
                            if (nextNode.getAddress() == -1) {
                                // Vetva ak node este nema alokovany block
                                int freeBlock = this.regularFile.getRemovedFreeBlock();
                                if (freeBlock == -1)
                                    return false;
                                nextNode.setAddress(freeBlock);
                            }

                            // Vlozenie aktualne triedeneho zaznamu do spravneho blocku
                            if (nextNode == leftSon) {
                                leftSonBlock.insert((T) currentRecord);
                            } else {
                                rigthSonBlock.insert((T) currentRecord);
                            }
                            nextNode.setCount(nextNode.getCount() + 1);
                        }

                        // Ak je to mozne, vlozi vkladany prvok do prislusneho blocku a nastavi inserted na true aby sa ukoncil cyklus
                        external = (DynamicHashFileNodeExternal) newInternal.getNextNode(record.getHash());
                        if (external.getCount() < this.regularFile.getBlockFactor()) {
                            if (external == leftSon) {
                                leftSonBlock.insert((T) record);
                            } else {
                                rigthSonBlock.insert((T) record);
                            }
                            external.setCount(external.getCount() + 1);
                            inserted = true;
                        }

                        //Pokial je to potrebne, ulozi blocky prislusnych nodov na prislusne adresy
                        try {
                            if (leftSon.getAddress() != -1)
                                this.regularFile.writeBlock(leftSon.getAddress(), leftSonBlock);
                            if (rightSon.getAddress() != -1)
                                this.regularFile.writeBlock(rightSon.getAddress(), rigthSonBlock);
                        } catch (IOException e) {
                            System.out.println(e);
                        }

                    } else {
                        // Vlozenie do aktualneho nodu
                        Block<T> currentBlock = this.regularFile.readBlock(external.getAddress());
                        if (external.getCount() < this.regularFile.getBlockFactor()) {
                            // Vetva ak je v blocku regularneho suboru miesto
                            if (currentBlock == null) {
                                System.out.println("Chyba pri nacitani blocku v inserte");
                                return false;
                            }
                            // TODO kontrola ci sa moze vlozit do blocku (nesmie tam vyjst equals true)
                            currentBlock.insert(record);
                            external.setCount(external.getCount() + 1);
                            try {
                                this.regularFile.writeBlock(external.getAddress(), currentBlock);
                            } catch (IOException e) {
                                System.out.println(e);
                            }
                        } else {
                            // Vetva aku uz nie je v blocku regularneho suboru miesto, teda ukladanie do preplnovacieho suboru
                            boolean blockWithFreeSpaceFound = false;
                            int currentAddress = external.getAddress();
                            int cycleCounter = 0;

                            while (!blockWithFreeSpaceFound) {
                                int nextBlock = currentBlock.getNextBlock();

                                if (nextBlock == -1) {
                                    // Vetva pokial nema nastaveneho nasledovnika teda vytvorenie noveho nasledovnika v preplnovacom subore
                                    int freeBlock = this.overflowFile.getRemovedFreeBlock();
                                    if (freeBlock == -1)
                                        return false;
                                    currentBlock.setNextBlock(freeBlock);
                                    Block<T> newBlock = this.overflowFile.readBlock(currentBlock.getNextBlock());
                                    newBlock.insert(record);
                                    external.setCount(external.getCount() + 1);
                                    try {
                                        if (cycleCounter == 0) {
                                            this.regularFile.writeBlock(currentAddress, currentBlock);
                                        } else {
                                            this.overflowFile.writeBlock(currentAddress, currentBlock);
                                        }
                                        this.overflowFile.writeBlock(currentBlock.getNextBlock(), newBlock);
                                        blockWithFreeSpaceFound = true;
                                    } catch (Exception e) {
                                        System.out.println(e);
                                        return false;
                                    }

                                } else {
                                    // Vetva pokial uz ma nasledovnika v preplnovacom subore
                                    currentBlock = this.overflowFile.readBlock(nextBlock);
                                    currentAddress = nextBlock;

                                    if (currentBlock.getValidCount() < this.overflowFile.getBlockFactor()) {
                                        // Pokial ma block miesto, tak ho ulozi don
                                        currentBlock.insert(record);
                                        external.setCount(external.getCount() + 1);
                                        try {
                                            this.overflowFile.writeBlock(nextBlock, currentBlock);
                                            blockWithFreeSpaceFound = true;
                                        } catch (Exception e) {
                                            System.out.println(e);
                                            return false;
                                        }
                                    }
                                }
                                cycleCounter++;
                            }
                        }
                        inserted = true;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Ak sa prvok s hashom vlozeneho IRecordu nachadza v strukture, vrati ho ako navratovu hodnotu.
     * Ak nie, vrati null.
     * @param record
     * @return
     */
    public T find(IRecord record) {
        // Najprv sa najde prislusny node a prezrie prislusny block v hlavnom subore
        DynamicHashFileNodeExternal external = this.findExternalNode(record);
        Block<T> blockFound = this.regularFile.readBlock(external.getAddress());
        T found = blockFound.find(record);

        if (found == null && blockFound.getNextBlock() != -1) {
            // Vetva na prehladavanie preplnovacieho suboru
            boolean needToLookForAnotherBlock = true;

            while (needToLookForAnotherBlock) {
                int currentAddress = blockFound.getNextBlock();
                blockFound = this.overflowFile.readBlock(currentAddress);
                found = blockFound.find(record);

                if (found != null || blockFound.getNextBlock() == -1)
                    needToLookForAnotherBlock = false;
            }
        }

        return found;
    }
    public T delete(IRecord record) {
        T found = null;
        if (record != null) {
            DynamicHashFileNodeExternal external = this.findExternalNode(record);
            Block<T> blockFound = this.regularFile.readBlock(external.getAddress());
            found = blockFound.find(record);

//            if (found)
        }
        return found;
    }

    public DynamicHashFileNode getRoot() {
        return root;
    }

    /**
     * Metoda, ktora vrati vsetky blocky v regularnom subore
     * @return
     */
    public ArrayList<Block<T>> getAllRegularBlocks() {
        return this.regularFile.getAllBlocks();
    }

    /**
     * Metoda, ktora vrati vsetky blocky v preplnovacom subore
     * @return
     */
    public ArrayList<Block<T>> getAllOverflowBlocks() {
        return this.overflowFile.getAllBlocks();
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

    private DynamicHashFileNodeExternal findExternalNode(IRecord record) {
        DynamicHashFileNode current = this.root;
        while (current instanceof DynamicHashFileNodeInternal) {
            current = ((DynamicHashFileNodeInternal) current).getNextNode(record.getHash());
        }
        return (DynamicHashFileNodeExternal) current;
    }
}

package Model.DynamicHashFile;

import Model.DynamicHashFile.Data.Block;
import Model.DynamicHashFile.Data.IRecord;
import Model.QuadTree.Data.IData;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
        this.root = this.getFileAsTrie(fileName);
//        this.root = new DynamicHashFileNodeExternal(0, 1, null);
        this.fileName = fileName;
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
                external.increaseCapacityBy(this.regularFile.getBlockFactor());
                Block<T> newBlock = this.regularFile.readBlock(external.getAddress());
                newBlock.insert(record);
                external.increaseCountBy(1);
                try {
                    this.regularFile.writeBlock(external.getAddress(), newBlock);
                } catch (Exception e) {
                    System.out.println(e);
                    return false;
                }
                return true;

            } else {
                boolean inserted = false;
                // Kontrola ci je zaznam unikatny
                if (!this.isUnique(external, record))
                    return false;

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

                        for (int i = 0; i < originalBlock.getValidCount(); i++) {
//                        for (IRecord currentRecord : originalArray) {
                            DynamicHashFileNodeExternal nextNode = (DynamicHashFileNodeExternal) newInternal.getNextNode(originalArray[i].getHash());
                            if (nextNode.getAddress() == -1) {
                                // Vetva ak node este nema alokovany block
                                int freeBlock = this.regularFile.getRemovedFreeBlock();
                                if (freeBlock == -1)
                                    return false;
                                nextNode.setAddress(freeBlock);
                                nextNode.increaseCapacityBy(this.regularFile.getBlockFactor());
                            }

                            // Vlozenie aktualne triedeneho zaznamu do spravneho blocku
                            if (nextNode == leftSon) {
                                leftSonBlock.insert((T) originalArray[i]);
                            } else {
                                rigthSonBlock.insert((T) originalArray[i]);
                            }
                            nextNode.increaseCountBy(1);
                        }

                        // Ak je to mozne, vlozi vkladany prvok do prislusneho blocku a nastavi inserted na true aby sa ukoncil cyklus
                        external = (DynamicHashFileNodeExternal) newInternal.getNextNode(record.getHash());
                        if (external.getCount() < this.regularFile.getBlockFactor()) {
                            if (external == leftSon) {
                                leftSonBlock.insert((T) record);
                            } else {
                                rigthSonBlock.insert((T) record);
                            }
                            external.increaseCountBy(1);
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
                        if (currentBlock.getValidCount() < this.regularFile.getBlockFactor()) {
                            // Vetva ak je v blocku regularneho suboru miesto
                            if (currentBlock == null) {
                                System.out.println("Chyba pri nacitani blocku v inserte");
                                return false;
                            }
                            currentBlock.insert(record);
                            external.increaseCountBy(1);
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
                                    external.increaseCapacityBy(this.overflowFile.getBlockFactor());
                                    Block<T> newBlock = this.overflowFile.readBlock(currentBlock.getNextBlock());
                                    newBlock.insert(record);
                                    external.increaseCountBy(1);
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
                                        external.increaseCountBy(1);
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
        if (blockFound == null)
            return null;
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
            // Najprv sa pokusi vymazat data z aktualneho blocku
            DynamicHashFileNodeExternal external = this.findExternalNode(record);
            if (external.getAddress() == -1)
                return null;
            Block<T> blockFound = this.regularFile.readBlock(external.getAddress());
            int currentAddress = external.getAddress();
            int blocksFromRegular = 0;
            int previousAddress = -1;
            Block<T> previousBlock = null;
            found = blockFound.delete(record);

            if (found == null && blockFound.getNextBlock() != -1) {
                // Ak sa data v blocku nenasli a su este neprebadane blocky v poradi zacne prechadzat preplnovaci block
                boolean needToLookForAnotherBlock = true;

                while (needToLookForAnotherBlock) {
                    previousAddress = currentAddress;
                    currentAddress = blockFound.getNextBlock();
                    previousBlock = blockFound;
                    blockFound = this.overflowFile.readBlock(currentAddress);
                    blocksFromRegular++;
                    found = blockFound.delete(record);

                    if (found != null) {
                        external.increaseCountBy(-1);
                        try {
                            this.overflowFile.writeBlock(currentAddress, blockFound);
                        } catch (IOException e) {
                            System.out.println(e);
                        }
                        needToLookForAnotherBlock = false;
                    } else if (blockFound.getNextBlock() == -1) {
                        needToLookForAnotherBlock = false;
                    }
                }
            } else if (found != null){
                // Vetva ked sa podari odstranit subor z blocku v hlavnom subore
                external.increaseCountBy(-1);
                try {
                    this.regularFile.writeBlock(currentAddress, blockFound);
                } catch (IOException e) {
                    System.out.println(e);
                }
            }

            if (found != null) {
                if (external.getCount() >= this.regularFile.getBlockFactor() && external.getFreeCapacity() >= this.overflowFile.getBlockFactor()) {
                    // V pripade ze je volna kapacita aspon o velkosti block factoru preplnovacieho suboru, prebieha striasanie
                    while (blockFound.getNextBlock() != -1) {
                        // Najprv sa najde posledny node v poradi
                        previousAddress = currentAddress;
                        currentAddress = blockFound.getNextBlock();
                        previousBlock = blockFound;
                        blockFound = this.overflowFile.readBlock(currentAddress);
                        blocksFromRegular++;
                    }

                    // Nacitanie zaznamov z posledneho blocku do queue
                    Queue<T> recordsToReinsert = new LinkedList<T>();
                    for (int i = 0; i < blockFound.getValidCount(); i++)
                        recordsToReinsert.add((T) blockFound.getRecords()[i]);

                    //Ulozenie predchadzajuceho blocku
                    this.overflowFile.freeTheBlock(currentAddress);
                    previousBlock.setNextBlock(-1);
                    external.increaseCapacityBy(-this.overflowFile.getBlockFactor());
                    try {
                        if (blocksFromRegular == 1) {
                            this.regularFile.writeBlock(previousAddress, previousBlock);
                        } else {
                            this.overflowFile.writeBlock(previousAddress, previousBlock);
                        }
                    } catch (IOException e) {
                        System.out.println(e);
                    }

                    // Prechadzanie blockov nodu a hladanie volneho miesta pre uvolnene zaznamy
                    currentAddress = external.getAddress();
                    blocksFromRegular = 0;
                    blockFound = this.regularFile.readBlock(currentAddress);
                    while (!recordsToReinsert.isEmpty()) {
                        int freeCapacity = blockFound.getFreeCapacity();
                        for (int i = 0; i < freeCapacity; i++)
                            blockFound.insert(recordsToReinsert.remove());

                        try {
                            if (blocksFromRegular == 0) {
                                this.regularFile.writeBlock(currentAddress, blockFound);
                            } else {
                                this.overflowFile.writeBlock(currentAddress, blockFound);
                            }
                        } catch (IOException e) {
                            System.out.println(e);
                        }

                        currentAddress = blockFound.getNextBlock();
                        blocksFromRegular++;
                        if (currentAddress != -1)
                            blockFound = this.overflowFile.readBlock(currentAddress);
                    }
                }

                if (external.getCapacity() <= this.regularFile.getBlockFactor()) {
                    // Vetva ak sa vyprazdni block v hlavnom subore a nema uz ziadnych nasledovnikov v preplnovacom subore
                    if (external.getCount() == 0) {
                        this.regularFile.freeTheBlock(external.getAddress());
                        external.setAddress(-1);
                        external.increaseCapacityBy(-this.regularFile.getBlockFactor());
                    }

                    if (external.getParent() != null) {
                        boolean checkNeeded = true;

                        while (checkNeeded) {
                            // Najprv sa ziska rodic a jeho druhy syn
                            DynamicHashFileNodeInternal internalParent = (DynamicHashFileNodeInternal) external.getParent();
                            DynamicHashFileNode otherSon = null;
                            if (internalParent.getLeftSon() == external) {
                                otherSon = internalParent.getRightSon();
                            } else {
                                otherSon = internalParent.getLeftSon();
                            }

                            if (otherSon instanceof DynamicHashFileNodeExternal) {
                                // Zmerguje potomkov svojho predka do jedneho nodu
                                DynamicHashFileNodeExternal mergedNode = this.mergeExternalNodes((DynamicHashFileNodeExternal)otherSon, external);

                                if (mergedNode != null) {
                                    // Ak merge prebehol, prebehne krok zlucovania
                                    if (internalParent.getParent() != null) {
                                        // Ak nie je rodic korenom tak len prenastavi prislusneho syna prarodica na externy node
                                        // cim sa zrusi referencia na rodica a jeho druheho potomka
                                        DynamicHashFileNodeInternal internalGrandParent = (DynamicHashFileNodeInternal) internalParent.getParent();
                                        if (internalGrandParent.getLeftSon() == internalParent) {
                                            internalGrandParent.setLeftSon(mergedNode);
                                        } else {
                                            internalGrandParent.setRightSon(mergedNode);
                                        }
                                        external = mergedNode;

                                    } else {
                                        // Ak je rodic korenom tak len nastavi koren na externy node a ukonci cyklus
                                        mergedNode.setParent(internalParent.getParent());
                                        this.root = mergedNode;
                                        checkNeeded = false;
                                    }
                                    mergedNode.increaseDepthBy(-1);
                                } else {
                                    checkNeeded = false;
                                }
                            } else {
                                // Ak jeho druhy syn nie je externy, je interny a ak je tak obsahuje data. V oboch pripadoch sa cyklus Mergovania ukoncuje.
                                checkNeeded = false;
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    public boolean edit(T record) {
        // Najprv sa najde prislusny node a prezrie prislusny block v hlavnom subore
        DynamicHashFileNodeExternal external = this.findExternalNode(record);
        if (external.getAddress() == -1)
            return false;
        Block<T> blockFound = this.regularFile.readBlock(external.getAddress());
        int blocksFromRegular = 0;
        int currentAddress = external.getAddress();
        T found = blockFound.find(record);

        if (found == null && blockFound.getNextBlock() != -1) {
            // Vetva na prehladavanie preplnovacieho suboru
            boolean needToLookForAnotherBlock = true;

            while (needToLookForAnotherBlock) {
                currentAddress = blockFound.getNextBlock();
                blockFound = this.overflowFile.readBlock(currentAddress);
                blocksFromRegular++;
                found = blockFound.find(record);

                if (found != null || blockFound.getNextBlock() == -1)
                    needToLookForAnotherBlock = false;
            }
        }

        if (found != null) {
            // Pokial sa nasiel hladany prvok, zmaze ho z povodneho blocku a na jeho miesto zapise upraveny
            blockFound.delete(found);
            blockFound.insert(record);
            try {
                if (blocksFromRegular == 0)
                    this.regularFile.writeBlock(currentAddress, blockFound);
                else
                    this.overflowFile.writeBlock(currentAddress, blockFound);
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
            return true;
        }

        return false;
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
                result = result + "\nExternal node Depth: " + currentNode.getDepth() +
                        " Address: " + externalNode.getAddress() + " Count: " + externalNode.getCount() + " Capacity: " + externalNode.getCapacity();
//                System.out.println("External node, Depth: " + currentNode.getDepth() + " Address: " + externalNode.getAddress() + " Count: " + externalNode.getCount());
            } else {
                result = result + "\nInternal node Depth: " + currentNode.getDepth();
//                System.out.println("Internal Node, Depth: " + currentNode.getDepth());
                nodeQueue.add(((DynamicHashFileNodeInternal)currentNode).getLeftSon());
                nodeQueue.add(((DynamicHashFileNodeInternal)currentNode).getRightSon());
            }

            if (nodeQueue.isEmpty())
                allDone = true;
        }
        return result;
    }

    /**
     * Metoda urcena na prepis flie zapisaneho v style getTrieAsString do stromu.
     * Ak sa file najde a metoda v poriadku prebehne, vrati adresu korena vytvoreneho stromu. Ak nie, vrati prazdny nealokovany externy node ako koren.
     * @param fileName nazov suboru bez .txt pripony
     * @return Koren stromu vytvoreneho v operacnej pamati
     */
    public DynamicHashFileNode getFileAsTrie(String fileName) {
        File file = new File(fileName + "Trie.txt");
        DynamicHashFileNode newRoot = new DynamicHashFileNodeExternal(0, 1, null);
        if (file.exists()) {
            try {
                FileReader fileReader = new FileReader(file);
                BufferedReader buffer = new BufferedReader(fileReader);
                String line = buffer.readLine();
                line = buffer.readLine();
                Queue<DynamicHashFileNodeInternal> unassignedInternals = new LinkedList<>();
                while (line != null) {
                    ArrayList<String> lineWords = new ArrayList<>(Arrays.asList(line.split(" ")));
                    if (lineWords.get(0).equals("Internal")) {
                        // Znovuvytvorenie interneho nodu
                        DynamicHashFileNodeInternal newInternal = new DynamicHashFileNodeInternal(Integer.parseInt(lineWords.get(3)), null, null, null);
                        if (!unassignedInternals.isEmpty()) {
                            // Ak interny node nie je prvy v strome, zaradi sa do stromu.
                            // Potomkovia su zoradeni v poradi najprv lavy potom pravy takze interny node vymazavame z poradovnika az ked sa mu naplni pravy node.
                            if (unassignedInternals.peek().getLeftSon() == null) {
                                unassignedInternals.peek().setLeftSon(newInternal);
                            } else {
                                unassignedInternals.remove().setRightSon(newInternal);
                            }
                        } else {
                            newRoot = newInternal;
                        }
                        unassignedInternals.add(newInternal);
                    } else {
                        // Znovuvytvorenie externeho nodu
                        DynamicHashFileNodeExternal newExternal = new DynamicHashFileNodeExternal(Integer.parseInt(lineWords.get(7)), Integer.parseInt(lineWords.get(3)), null);
                        newExternal.setAddress(Integer.parseInt(lineWords.get(5)));
                        newExternal.increaseCapacityBy(Integer.parseInt(lineWords.get(9)));
                        if (!unassignedInternals.isEmpty()) {
                            // Potomkovia su zoradeni v poradi najprv lavy potom pravy takze interny node vymazavame z poradovnika az ked sa mu naplni pravy node.
                            if (unassignedInternals.peek().getLeftSon() == null) {
                                unassignedInternals.peek().setLeftSon(newExternal);
                            } else {
                                unassignedInternals.remove().setRightSon(newExternal);
                            }
                        } else {
                            newRoot = newExternal;
                        }
                    }

                    line = buffer.readLine();
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }

        return newRoot;
    }

    /**
     * Ulozi aktualny trie do .txt suboru
     */
    public void saveCurrentSetup() {
        try {
            FileWriter fileWriter = new FileWriter(this.fileName + "Trie.txt");
            fileWriter.write(this.getTrieAsString());
            fileWriter.close();
        } catch (Exception e) {
            System.out.println("Error occured while writing into the file: \n" + e);
        }

    }

    /**
     * Metoda ktora skontroluje unikatnost zaznamu v danom node
     * @param external
     * @param record
     * @return true ak je record unikatny, false ak nie
     */
    private boolean isUnique(DynamicHashFileNodeExternal external, IRecord record) {
        if (external.getAddress() == -1)
            return true;
        Block<T> currentBlock = this.regularFile.readBlock(external.getAddress());
        boolean endFound = false;
        while (!endFound) {
            if (currentBlock.find(record) != null)
                return false;
            if (currentBlock.getNextBlock() == -1) {
                endFound = true;
            } else {
                currentBlock = this.overflowFile.readBlock(currentBlock.getNextBlock());
            }
        }

        return true;
    }

    private DynamicHashFileNodeExternal findExternalNode(IRecord record) {
        DynamicHashFileNode current = this.root;
        while (current instanceof DynamicHashFileNodeInternal) {
            current = ((DynamicHashFileNodeInternal) current).getNextNode(record.getHash());
        }
        return (DynamicHashFileNodeExternal) current;
    }

    /**
     * Pokial je mozne zlucit dva externe nody do jedneho s jednym blockom v zakladnom subore, vrati taky externy node s
     * hotovou upravou v prislusnom subore. Ak nie, vrati null.
     * @param external1
     * @param external2
     * @return
     */
    private DynamicHashFileNodeExternal mergeExternalNodes(DynamicHashFileNodeExternal external1, DynamicHashFileNodeExternal external2) {
        if (external1 == external2 || external1 == null || external2 == null)
            return null;

        // Ak ma jeden z nodov vacsiu kapacitu ako block zakladneho suboru, nie je merge vykonany
        if (external1.getCapacity() > this.regularFile.getBlockFactor() || external2.getCapacity() > this.regularFile.getBlockFactor())
            return null;

        // Ak jeden node nema alokovany zakladny block, nie je potrebny merge ale staci vratit druhy node
        if (external1.getAddress() == -1)
            return external2;
        if (external2.getAddress() == -1)
            return external1;

        // Ak sa prvky jedneho nedaju vlozit do druheho, merge sa neda vykonat
        if (external1.getFreeCapacity() < external2.getCount())
            return null;

        // Merge prvkov dvoch nodov do nodu external1
        Block<T> block1 = this.regularFile.readBlock(external1.getAddress());
        Block<T> block2 = this.regularFile.readBlock(external2.getAddress());
        IRecord[] records2 = block2.getRecords();
        for (int i = 0; i < block2.getValidCount(); i++) {
            block1.insert((T)records2[i]);
        }
        external1.increaseCountBy(external2.getCount());

        // Block zapise na tu adresu, ktora je dalej od konca suboru
        if (external1.getAddress() < external2.getAddress()) {
            this.regularFile.freeTheBlock(external2.getAddress());
        } else {
            this.regularFile.freeTheBlock(external1.getAddress());
            external1.setAddress(external2.getAddress());
        }
        try {
            this.regularFile.writeBlock(external1.getAddress(), block1);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return external1;
    }
}

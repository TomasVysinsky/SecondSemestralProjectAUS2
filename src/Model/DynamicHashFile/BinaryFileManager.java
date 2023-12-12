package Model.DynamicHashFile;

import Model.DynamicHashFile.Data.Block;
import Model.DynamicHashFile.Data.IRecord;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;

public class BinaryFileManager<T extends IRecord> {
    private int blockFactor;
    private RandomAccessFile file;
    private Class<T> type;
    private int firstFreeBlock;

    /**
     *
     * @param fileName String, adresa binarneho suboru bez suffixu
     */
    public BinaryFileManager(int blockFactor, String fileName, Class<T> type) {
        this.blockFactor = blockFactor;
        this.type = type;
        this.firstFreeBlock = -1;
        try {
            this.file = new RandomAccessFile(fileName + ".bin", "rw");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int getBlockFactor() {
        return blockFactor;
    }

    public Block<T> readBlock(int address) {
        Block<T> newBlock = new Block<T>(this.blockFactor, this.type);
        byte[] byteArray = new byte[newBlock.getSize()];
        try {
            this.file.seek((long) address * byteArray.length);
            this.file.read(byteArray);
        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        newBlock.fromByteArray(byteArray);
        return newBlock;
    }

    public void writeBlock(int address, Block<T> block) throws IOException {
        this.file.seek((long) address * block.getSize());
        this.file.write(block.toByteArray());
    }

    /**
     * Odoberie prvy volny block zo zoznamu volnych blockov a da jeho adresu ako navratovu hodnotu.
     * Ak je zoznam volnych blockov prazdny, alokuje v subore nove miesto na block a vrati jeho adresu.
     * Ak pri vytvarani volneho blocku dojde k chybe, vrati -1
     * @return
     */
    public int getRemovedFreeBlock() {
        if (this.firstFreeBlock == -1) {
            // Vetva v pripade, ze je zoznam prazdnych blockov prazdny
            Block<T> newBlock = new Block<T>(this.blockFactor, this.type);
            newBlock.setActive(true);
            newBlock.setValidCount(0);
            try {
                this.file.setLength(this.file.length() + newBlock.getSize());
                int address = this.getLastBlockAddress();
                this.writeBlock(address, newBlock);
                return address;
            } catch (Exception e) {
                System.out.println(e);
                return -1;
            }
        }

        int freeBlock = this.firstFreeBlock;
        if (this.removeFreeBlock(freeBlock))
            return freeBlock;
        return -1;
    }

    /**
     * Ak je block uprostred suboru, nastavi block na prislusnu poziciu v poradovniku volnych blockov tak, aby zaciatok
     * poradovnika bol co najviac na zaciatku suboru.
     * Ak je block na konci, skrati subor tak, aby na konci ostal platny block
     * @param address
     */
    public void freeTheBlock(int address) {
        Block<T> blockToSetFree = this.readBlock(address);
        int lastBlockAddress = this.getLastBlockAddress();

        if(address < lastBlockAddress) {
            // TODO queue na blocky
            blockToSetFree.setActive(false);
            blockToSetFree.setNextBlock(this.firstFreeBlock);

            // Skontroluje ci uz tam nejaky block bol ulozeny, ak nie tak len nastavi prvy volny block, ak ano, najde mu miesto na vlozenie
            if (this.firstFreeBlock != -1) {
                Block<T> nextBlock = this.readBlock(this.firstFreeBlock);
                int nextBlockAddress = this.firstFreeBlock;

                if (nextBlockAddress < address) {
                    // Ak block nie je najvhodnejsie vlozit na zaciatok zretazenia, zapocne cyklus hladania optimalneho miesta
                    boolean placeFound = false;
                    while (!placeFound) {
                        if (nextBlock.getNextBlock() != -1) {
                            // Ak nasledujuci block obsahuje nasledovnika, skusi ci nie je vhodne vlozit block medzi ne
                            int previousAddress = nextBlockAddress;
                            Block<T> previousBlock = nextBlock;
                            nextBlockAddress = nextBlock.getNextBlock();
                            nextBlock = this.readBlock(nextBlockAddress);

                            if (address < nextBlockAddress) {
                                // Vlozenie blocku medzi dva blocky ak sa jeho adresa nachadza medzi nimi
                                previousBlock.setNextBlock(address);
                                try {
                                    this.writeBlock(previousAddress, previousBlock);
                                } catch (Exception e) {
                                    System.out.println(e);
                                    return;
                                }
                                nextBlock.setPreviousBlockIfInactive(address);
                                blockToSetFree.setPreviousBlockIfInactive(previousAddress);
                                blockToSetFree.setNextBlock(nextBlockAddress);
                                placeFound = true;
                            }
                        } else {
                            // Ak nasledujuci block uz nema nasledovnika, vlozi sa block na koniec zretazenia
                            nextBlock.setNextBlock(address);
                            blockToSetFree.setPreviousBlockIfInactive(nextBlockAddress);
                            blockToSetFree.setNextBlock(-1);
                            placeFound = true;
                        }
                    }
                } else {
                    // Vkladanie blocku na zaciatok zretazenia ak ma najmensiu adresu
                    nextBlock.setPreviousBlockIfInactive(address);
                    this.firstFreeBlock = address;
                    blockToSetFree.setPreviousBlockIfInactive(-1);
                }

                try {
                    this.writeBlock(nextBlockAddress, nextBlock);
                } catch (Exception e) {
                    System.out.println(e);
                    return;
                }
            } else {
                this.firstFreeBlock = address;
                blockToSetFree.setPreviousBlockIfInactive(-1);
            }

            try {
                this.writeBlock(address, blockToSetFree);
            } catch (Exception e) {
                System.out.println(e);
                return;
            }

        } else if (address == lastBlockAddress) {
            // Cast ktora skracuje subor
            // Najprv oddstrani posledny subor
            try {
                this.file.setLength(this.file.length() - blockToSetFree.getSize());
            } catch (Exception e) {
                System.out.println(e);
            }

            // Potom spusti while cyklus, ktory funguje dokym na konci nie je platny block alebo dokym nie je subor prazdny
            boolean fileCutted = false;
            while (!fileCutted) {
                lastBlockAddress = this.getLastBlockAddress();
                if (lastBlockAddress == -1) {
                    // Cast ak je subor prazdny
                    fileCutted = true;
                } else {
                    if (this.removeFreeBlock(lastBlockAddress)) {
                        // Ak sa podarilo odstranit posledny block zo zoznamu neplatnych blockov (lebo sa v nom nachadzal)
                        // skrati subor o velkost jedneho blocku
                        try {
                            this.file.setLength(this.file.length() - blockToSetFree.getSize());
                        } catch (Exception e) {
                            System.out.println(e);
                        }
                    } else {
                        // Ak sa posledny block odstranit nepodarilo, znamena to ze je aktivny takze subor uz netreba skracovat
                        fileCutted = true;
                    }
                }
            }
        }
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

        // Precitanie vsetkych blockovjedneho po druhom
        while ((long) block.getSize() * currentAddress < fileSize) {
            blocks.add(this.readBlock(currentAddress));
            currentAddress++;
        }

        return blocks;
    }

    /**
     *
     * @param address
     * @return true ak block uspesne uvolni, false ak dojde k pokusu o uvolnenie platneho blocku
     */
    private boolean removeFreeBlock(int address) {
        Block<T> blockToRemove = this.readBlock(address);
        if (blockToRemove.isActive())
            return false;

        // Ak ma nasledovnika, nastavi mu addresu predchodcu na svojho predchodcu
        if (blockToRemove.getNextBlock() != -1) {
            Block<T> nextBlock = this.readBlock(blockToRemove.getNextBlock());
            nextBlock.setPreviousBlockIfInactive(blockToRemove.getPreviousBlockIfInactive());
            try {
                this.writeBlock(blockToRemove.getNextBlock(), nextBlock);
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
        }

        // Ak nema predchodcu, nastavi korenu svojho nasledovnika
        if (blockToRemove.getPreviousBlockIfInactive() == -1) {
            this.firstFreeBlock = blockToRemove.getNextBlock();
        } else {
            // Ak ma predchodcu tak mu nastavi nasledovnika na svojho nasledovnika
            Block<T> previousBlock = this.readBlock(blockToRemove.getPreviousBlockIfInactive());
            previousBlock.setNextBlock(blockToRemove.getNextBlock());
            try {
                this.writeBlock(blockToRemove.getPreviousBlockIfInactive(), previousBlock);
            } catch (Exception e) {
                System.out.println(e);
                return false;
            }
        }

        // Vynuluje a nastavi block ako aktivny
        blockToRemove.setActive(true);
        blockToRemove.setValidCount(0);
        blockToRemove.setNextBlock(-1);
        try {
            this.writeBlock(address, blockToRemove);
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        return true;
    }

    /**
     * Vrati index posledneho blocku v subore.
     * Vrati -1 ak subor neobsahuje ziadne blocky
     * @return
     */
    private int getLastBlockAddress() {
        try {
            Block<T> newBlock = new Block<T>(this.blockFactor, this.type);
            return (int)(this.file.length() / newBlock.getSize()) - 1;
        } catch (Exception e) {
            System.out.println(e);
            return -3;
        }
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

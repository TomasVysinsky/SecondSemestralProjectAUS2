package Model.QuadTree.Coordinates;

import java.lang.*;
import java.nio.ByteBuffer;

public class Coordinate {
    private Width width;
    private double widthPosition;
    private Length length;
    private double lengthPosition;

    public Coordinate() {
        this.width = Width.N;
        this.widthPosition = 0;
        this.length = Length.W;
        this.widthPosition = 0;
    }

    public Coordinate(Width width, double widthPosition, Length length, double lengthPosition) {
        if (widthPosition < 0) {
            if(width == Width.N){
                width = Width.S;
            } else {
                width = Width.N;
            }
            widthPosition = widthPosition * -1;
        }
        this.width = width;
        this.widthPosition = widthPosition;

        if (lengthPosition < 0) {
            if (length == Length.E) {
                length = Length.W;
            } else {
                length = Length.E;
            }
            lengthPosition = lengthPosition * -1;
        }
        this.length = length;
        this.lengthPosition = lengthPosition;
    }

    public Width getWidth() {
        return width;
    }
    public char getWidthAsChar() {
        if (width == Width.N)
            return 'N';
        return 'S';
    }

    public double getWidthPosition() {
        return widthPosition;
    }

    public Length getLength() {
        return length;
    }
    public char getLengthAsChar() {
        if (length == Length.E)
            return 'E';
        return 'W';
    }

    public double getLengthPosition() {
        return lengthPosition;
    }

    public static int getSize() { return Character.BYTES * 2 + Double.BYTES * 2; }

    /**
     *
     * @param other
     * @return coordinate ktora je v strede medzi tymito dvoma
     */
    public Coordinate getMiddle(Coordinate other){
        Width newWidth;
        Length newLength;
        double newWidthPosition = 0, newLengthPosition = 0;
        if(other != null) {
            if (this.width == Width.N) {
                if (other.width == Width.N) {
                    newWidth = Width.N;
                    newWidthPosition = Math.min(this.widthPosition, other.widthPosition) +
                            ((Math.max(this.widthPosition, other.widthPosition) - Math.min(this.widthPosition, other.widthPosition)) / 2);
                } else {
                    newWidthPosition = (Math.max(this.widthPosition, other.widthPosition) - Math.min(this.widthPosition, other.widthPosition)) / 2;
                    if (this.widthPosition >= other.widthPosition) {
                        newWidth = Width.N;
                    } else {
                        newWidth = Width.S;
                    }
                }
            } else {
                if (other.width == Width.S) {
                    newWidth = Width.S;
                    newWidthPosition = Math.min(this.widthPosition, other.widthPosition) +
                            ((Math.max(this.widthPosition, other.widthPosition) - Math.min(this.widthPosition, other.widthPosition)) / 2);
                } else {
                    newWidthPosition = (Math.max(this.widthPosition, other.widthPosition) - Math.min(this.widthPosition, other.widthPosition)) / 2;
                    if (this.widthPosition > other.widthPosition) {
                        newWidth = Width.S;
                    } else {
                        newWidth = Width.N;
                    }
                }
            }
            if (this.length == Length.E) {
                if (other.length == Length.E) {
                    newLength = Length.E;
                    newLengthPosition = Math.min(this.lengthPosition, other.lengthPosition) +
                            ((Math.max(this.lengthPosition, other.lengthPosition) - Math.min(this.lengthPosition, other.lengthPosition)) / 2);
                } else {
                    newLengthPosition = (Math.max(this.lengthPosition, other.lengthPosition) - Math.min(this.lengthPosition, other.lengthPosition)) / 2;
                    if (this.lengthPosition >= other.lengthPosition) {
                        newLength = Length.E;
                    } else {
                        newLength = Length.W;
                    }
                }
            } else {
                if (other.length == Length.W) {
                    newLength = Length.W;
                    newLengthPosition = Math.min(this.lengthPosition, other.lengthPosition) +
                            ((Math.max(this.lengthPosition, other.lengthPosition) - Math.min(this.lengthPosition, other.lengthPosition)) / 2);
                } else {
                    newLengthPosition = (Math.max(this.lengthPosition, other.lengthPosition) - Math.min(this.lengthPosition, other.lengthPosition)) / 2;
                    if (this.lengthPosition >= other.lengthPosition) {
                        newLength = Length.W;
                    } else {
                        newLength = Length.E;
                    }
                }
            }
            return new Coordinate(newWidth, newWidthPosition, newLength, newLengthPosition);
        }
        return null;
    }

    /**
     *
     * @return dva smery (dlzku a sirku) predstavujuce kvadrant kde sa zadana suradnica nachadza v zavislosti od tejto
     * 1 na indexe 0 je ak sa nachadza hore resp. vpravo na indexe 1, -1 ak dole a 0 ak su totozne.
     */
    public int[] position(Coordinate other){
        int[] result = new int[2];
        if(this.width == Width.N){
            if (other.width == Width.N) {
                if(this.widthPosition < other.widthPosition) {
                    result[0] = 1;
                } else if (this.widthPosition > other.widthPosition) {
                    result[0] = -1;
                } else {
                    result[0] = 0;
                }
            } else {
                if(this.widthPosition == 0 && other.widthPosition == 0) {
                    result[0] = 0;
                } else {
                    result[0] = -1;
                }
            }
        } else {
            if (other.width == Width.N) {
                if(this.widthPosition == 0 && other.widthPosition == 0) {
                    result[0] = 0;
                } else {
                    result[0] = 1;
                }
            } else {
                if(this.widthPosition < other.widthPosition) {
                    result[0] = -1;
                } else if (this.widthPosition > other.widthPosition) {
                    result[0] = 1;
                } else {
                    result[0] = 0;
                }
            }
        }
        if(this.length == Length.E){
            if (other.length == Length.E) {
                if(this.lengthPosition < other.lengthPosition) {
                    result[1] = 1;
                } else if (this.lengthPosition > other.lengthPosition) {
                    result[1] = -1;
                } else {
                    result[1] = 0;
                }
            } else {
                if(this.lengthPosition == 0 && other.lengthPosition == 0) {
                    result[1] = 0;
                } else {
                    result[1] = -1;
                }
            }
        } else {
            if (other.length == Length.E) {
                if(this.lengthPosition == 0 && other.lengthPosition == 0) {
                    result[1] = 0;
                } else {
                    result[1] = 1;
                }
            } else {
                if(this.lengthPosition < other.lengthPosition) {
                    result[1] = -1;
                } else if (this.lengthPosition > other.lengthPosition) {
                    result[1] = 1;
                } else {
                    result[1] = 0;
                }
            }
        }
        return result;
    }

    /**
     *
     * @return poziciu pre width = N
     */
    public double getNorthWidthPosition(){
        if(this.width == Width.N)
            return this.widthPosition;
        return this.widthPosition * -1;
    }

    /**
     *
     * @return poziciu pre length = E
     */
    public double getEastLengthPosition(){
        if(this.length == Length.E)
            return this.lengthPosition;
        return this.lengthPosition * -1;
    }

    /**
     * Vrati pole double ktore predstavuje vektor urcujuci polohu aktualnej suradnice pricom kladny smer osi y je
     * smer N ulozeny na indexe 1 a kladny smer osi X predstavuje smer E ulozeny na indexe 0
     * @return pole double ktore predstavuje vektor urcujuci polohu aktualnej suradnice
     */
    public double[] getVectorPosition(){
        return new double[]{this.getEastLengthPosition(), this.getNorthWidthPosition()};
    }

    private boolean charToWidth(char width) {
        if (width == 'N') {
            this.width = Width.N;
            return true;
        } else if (width == 'S') {
            this.width = Width.S;
            return true;
        }
        return false;
    }

    private boolean charToLength(char length) {
        if (length == 'E') {
            this.length = Length.E;
            return true;
        } else if (length == 'W') {
            this.length = Length.W;
            return true;
        }
        return false;
    }

    public byte[] toByteArray() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Coordinate.getSize());
        byteBuffer.putChar(this.getWidthAsChar());
        byteBuffer.putDouble(this.widthPosition);
        byteBuffer.putChar(this.getLengthAsChar());
        byteBuffer.putDouble(this.lengthPosition);
        return byteBuffer.array();
    }

    public void fromByteArray(byte[] array) {
        if (array.length == Coordinate.getSize()) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(array);
            if (!this.charToWidth(byteBuffer.getChar()))
                throw new RuntimeException("Nespravne data pri nacitani sirky");
            this.widthPosition = byteBuffer.getDouble();
            if (!this.charToLength(byteBuffer.getChar()))
                throw new RuntimeException("Nespravne data pri nacitani dlzky");
            this.lengthPosition = byteBuffer.getDouble();
            return;
        }
        throw new ArrayIndexOutOfBoundsException("Pole ma inu dlzku");
    }
}

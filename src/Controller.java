import Model.Model;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;
import Model.Data.Building;
import Model.Data.Log;
import Model.Data.Parcel;
import Model.QuadTree.Data.IData;

import java.util.ArrayList;

public class Controller {
    private View view;
    private Model model;

    public Controller(View view, int maxDepthQuadTree, int maxHashSize, int blockFactor, int overflowBlockFactor, String filename,
                      String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                      String  maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        this.model = new Model(maxDepthQuadTree, maxHashSize, blockFactor, overflowBlockFactor, filename,
                this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition,
                this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition);
        this.view = view;
    }

    public void createBuilding(int supisneCislo, String description,
            String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
            String  maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        Width eMinWidth = this.stringToWidth(minWidth),
                eMaxWidth = this.stringToWidth(maxWidth);
        Length eMinLength = this.stringToLength(minLength),
                eMaxLength = this.stringToLength(maxLength);

        if (this.model.insertBuilding(supisneCislo, description,
                new Coordinate(eMinWidth, minWidthPosition, eMinLength, minLengthPosition),
                new Coordinate(eMaxWidth, maxWidthPosition, eMaxLength, maxLengthPosition))) {
            System.out.println("Budova sa uspesne vlozila");
            this.view.infoObjektSaVytvoril();
        } else {
            this.view.errorObjektSaNevytvoril();
        }
    }

    public void createParcel(String description,
                               String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                               String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        Width eMinWidth = this.stringToWidth(minWidth),
                eMaxWidth = this.stringToWidth(maxWidth);
        Length eMinLength = this.stringToLength(minLength),
                eMaxLength = this.stringToLength(maxLength);

        if (this.model.insertParcel(description,
                new Coordinate(eMinWidth, minWidthPosition, eMinLength, minLengthPosition),
                new Coordinate(eMaxWidth, maxWidthPosition, eMaxLength, maxLengthPosition))) {
            System.out.println("Parcel sa uspesne vlozil");
            this.view.infoObjektSaVytvoril();
        } else {
            this.view.errorObjektSaNevytvoril();
        }
    }

    public void generateBuildings(int numberOfBuildings) {
        if (this.model.generateBuildings(numberOfBuildings)) {
            System.out.println("Budovy vygenerovane");
            this.view.infoDataBoliVygenerovane();
        } else {
            this.view.errorDataNeboliVygenerovane();
        }
    }

    public void generateParcels(int numberOfParcels) {
        if (this.model.generateParcels(numberOfParcels)) {
            System.out.println("Parcely vygenerovane");
            this.view.infoDataBoliVygenerovane();
        } else {
            this.view.errorDataNeboliVygenerovane();
        }
    }

    public void findBuilding(long id) {
        // TODO findBuilding na zaklade id
    }
    public void findBuildings(long[] ids, int validBuildings) {
        // TODO implementovat to do viewu
        ArrayList<Log> foundList = this.model.findBuildingsWithIDs(ids, validBuildings);
        if (foundList == null) {
            this.view.errorOutOfBorders();
            return;
        }
        this.view.showResults(foundList);
    }

    public void findBuildings(String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                              String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        ArrayList<Log> foundList = this.model.findBuildingsAtField(
                new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
        if (foundList == null) {
            this.view.errorOutOfBorders();
            return;
        }
        this.view.showResults(foundList);
    }

    public void findParcel(long id) {
        // TODO findParcel na zaklade id
    }

    public void findParcels(long[] ids, int validParcels) {
        // TODO implementovat to do viewu
        ArrayList<Log> foundList = this.model.findParcelsWithIDs(ids, validParcels);
        if (foundList == null) {
            this.view.errorOutOfBorders();
            return;
        }
        this.view.showResults(foundList);
    }

    public void findParcels(String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                               String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        ArrayList<Log> foundList = this.model.findParcelsAtField(
                new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
        if (foundList == null) {
            this.view.errorOutOfBorders();
            return;
        }
        this.view.showResults(foundList);
    }

    /*public void findProperties(String width, double widthPosition, String length, double lengthPosition) {
        ArrayList<Log> foundList = this.model.findPropertiesAtCoordinate(
                new Coordinate(this.stringToWidth(width), widthPosition, this.stringToLength(length), lengthPosition));
        if (foundList == null) {
            this.view.errorOutOfBorders();
            return;
        }
        this.view.showResults(foundList);
    }

    public void findProperties(String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                              String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        ArrayList<Log> foundList = this.model.findPropertiesAtField(
                new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
        if (foundList == null) {
            this.view.errorOutOfBorders();
            return;
        }
        this.view.showResults(foundList);
    }*/

    public void findAllProperties() {
        // TODO findAllProperties
//        ArrayList<Log> foundList = this.model.findAllProperties();
//        this.view.showResults(foundList);
    }

    public Log edit(Log oldLog, long id, int cislo, String description,
                     String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                     String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        Log logToReturn = oldLog;
        if (oldLog instanceof Building) {
            Building newLog = new Building(id, cislo, description,
                    new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                    new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
            logToReturn = this.model.editBuilding((Building) oldLog, newLog);

            /*if (oldLog.equals((IData) newLog)) {
                oldLog.edit(newLog);
            } else {
                if (model.insertBuilding(newLog)) {
                    this.model.remove(oldLog);
                } else {
                    this.view.errorOutOfBorders();
                    return oldLog;
                }
            }
            return newLog;*/
        } else if (oldLog instanceof Parcel) {
            Parcel newLog = new Parcel(id, description,
                    new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                    new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
            logToReturn = this.model.editParcel((Parcel) oldLog, newLog);
            /*if (oldLog.equals((IData) newLog)) {
                oldLog.edit(newLog);
            } else {
                if (this.model.insertParcel(newLog)) {
                    this.model.remove(oldLog);
                } else {
                    this.view.errorOutOfBorders();
                    return oldLog;
                }
            }
            return logToReturn;*/
        }
        return logToReturn;
    }

    public void delete(Log log) {
        this.model.remove(log);
    }

    public void saveInFile() {
        if (this.model.saveInFile()) {
            this.view.infoSuborBolUlozeny();
        } else {
            this.view.errorSuborNebolUlozeny();
        }

    }

    /*public void loadFromFile(String fileName) {
        if (this.model.loadFromFile(fileName)) {
            this.view.infoSuborSaNacital();
        } else {
            this.view.errorSuborSaNenacital();
        }
    }*/

    private Width stringToWidth(String width) {
        if (width.equals("North")) {
            return Width.N;
        } else {
            return Width.S;
        }
    }

    private Length stringToLength(String length) {
        if (length.equals("East")) {
            return Length.E;
        } else {
            return Length.W;
        }
    }
}

import Model.Model;
import Model.QuadTree.Coordinates.Coordinate;
import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;
import Model.QuadTree.Data.Building;
import Model.QuadTree.Data.Log;
import Model.QuadTree.Data.Parcel;

import java.util.ArrayList;

public class Controller {
    private View view;
    private Model model;

    public Controller(View view, int maxDepth,
                      String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                      String  maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        this.model = new Model(maxDepth,
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

    public void createParcel(int cisloParcely, String description,
                               String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                               String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        Width eMinWidth = this.stringToWidth(minWidth),
                eMaxWidth = this.stringToWidth(maxWidth);
        Length eMinLength = this.stringToLength(minLength),
                eMaxLength = this.stringToLength(maxLength);

        if (this.model.insertParcel(cisloParcely, description,
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

    public void findBuildings(String width, double widthPosition, String length, double lengthPosition) {
        ArrayList<Log> foundList = this.model.findBuildingsAtCoordinate(
                new Coordinate(this.stringToWidth(width), widthPosition, this.stringToLength(length), lengthPosition));
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

    public void findParcels(String width, double widthPosition, String length, double lengthPosition) {
        ArrayList<Log> foundList = this.model.findParcelsAtCoordinate(
                new Coordinate(this.stringToWidth(width), widthPosition, this.stringToLength(length), lengthPosition));
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

    public void findProperties(String width, double widthPosition, String length, double lengthPosition) {
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
    }

    public void findAllProperties() {
        ArrayList<Log> foundList = this.model.findAllProperties();
        this.view.showResults(foundList);
    }

    public Log edit(Log oldLog, long id, int cislo, String description,
                     String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                     String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {

        if (oldLog instanceof Building) {
            Building newLog = new Building(id, cislo, description,
                    new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                    new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
            if (oldLog.equals(newLog)) {
                oldLog.edit(newLog);
            } else {
                if (model.insertBuilding(newLog)) {
                    this.model.remove(oldLog);
                } else {
                    this.view.errorOutOfBorders();
                    return oldLog;
                }
            }
            return newLog;
        } else if (oldLog instanceof Parcel) {
            Parcel newLog = new Parcel(id, cislo, description,
                    new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                    new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
            if (oldLog.equals(newLog)) {
                oldLog.edit(newLog);
            } else {
                if (this.model.insertParcel(newLog)) {
                    this.model.remove(oldLog);
                } else {
                    this.view.errorOutOfBorders();
                    return oldLog;
                }
            }
            return newLog;
        }
        return oldLog;
    }

    public void delete(Log log) {
        this.model.remove(log);
    }

    public void saveInFile(String fileName) {
        if (this.model.saveInFile(fileName)) {
            this.view.infoSuborBolUlozeny();
        } else {
            this.view.errorSuborNebolUlozeny();
        }

    }

    public void loadFromFile(String fileName) {
        if (this.model.loadFromFile(fileName)) {
            this.view.infoSuborSaNacital();
        } else {
            this.view.errorSuborSaNenacital();
        }
    }

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

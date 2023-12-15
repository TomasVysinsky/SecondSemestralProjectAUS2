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
        ArrayList<Log> foundList = new ArrayList<Log>();
        Building found = this.model.findBuilding(id);
        if (found != null) {
            foundList.add(found);
            ArrayList<Log> foundParcels = this.model.findParcelsWithIDs(found.getParcels(), found.getValidParcels());
            if (foundParcels != null)
                foundList.addAll(foundParcels);
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
        ArrayList<Log> foundList = new ArrayList<Log>();
        Parcel found = this.model.findParcel(id);
        if (found != null) {
            foundList.add(found);
            ArrayList<Log> foundParcels = this.model.findBuildingsWithIDs(found.getBuildings(), found.getValidBuildings());
            if (foundParcels != null)
                foundList.addAll(foundParcels);
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

    public Log edit(Log oldLog, long id, int cislo, String description,
                     String minWidth, double minWidthPosition, String minLength, double minLengthPosition,
                     String maxWidth, double maxWidthPosition, String maxLength, double maxLengthPosition) {
        Log logToReturn = oldLog;
        if (oldLog instanceof Building) {
            Building newLog = new Building(id, cislo, description,
                    new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                    new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
            logToReturn = this.model.editBuilding((Building) oldLog, newLog);

        } else if (oldLog instanceof Parcel) {
            Parcel newLog = new Parcel(id, description,
                    new Coordinate(this.stringToWidth(minWidth), minWidthPosition, this.stringToLength(minLength), minLengthPosition),
                    new Coordinate(this.stringToWidth(maxWidth), maxWidthPosition, this.stringToLength(maxLength), maxLengthPosition));
            logToReturn = this.model.editParcel((Parcel) oldLog, newLog);
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

    public String getBuildingFileAsString() {
        return this.model.getBuildingFileAsString();
    }

    public String getParcelFileAsString() {
        return this.model.getParcelFileAsString();
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

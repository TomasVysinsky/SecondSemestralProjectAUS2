import Model.QuadTree.Coordinates.Length;
import Model.QuadTree.Coordinates.Width;
import Model.Data.Building;
import Model.Data.Log;
import Model.Data.Parcel;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class View extends JFrame {
    private Controller controller;
    private JPanel databaseManipulator;
    private JScrollPane Results;
    private JTextField minLengthPositionField;
    private JButton createBuildingButton;
    private JTextField minWidthPositionField;
    private JTextField maxWidthPositionField;
    private JTextField maxLengthPositionField;
    private JButton findBuildingsButton;
    private JButton findAllPropertiesButton;
    private JButton findParcelsButton;
    private JButton generateBuildingsButton;
    private JPanel Buttons;
    private JTextField supisneCisloTextField;
    private JTextField popisTextField;
    private JTextField pocetGenerovanychPrvkovTextField;
    private JPanel RestOfInputs;
    private JList resultList;
    private JButton generateParcelsButton;
    private JButton createParcelButton;
    private JButton intervalFindButton;
    private JButton generateButton;
    private JPanel Menu;
    private JPanel Coordinates;
    private JPanel maxCoordinates;
    private JPanel minCoordinates;
    private JTextField treeDepthTextField;
    private JLabel number;
    private JComboBox minWidthComboBox;
    private JButton editButton;
    private JButton deleteButton;
    private JComboBox minLengthComboBox;
    private JComboBox maxWidthComboBox;
    private JComboBox maxLengthComboBox;
    private JButton createButton;
    private JButton pointFindButton;
    private JButton fileBackupButton;
    private JLabel hlbkaOrFile;
    private JButton findAllPropertiesInButton;
    private JLabel pocetPrvkovTextField;
    private JTextArea BuildingFileTextArea;
    private JTextArea ParcelFileTextArea;
    private JScrollPane BuildingFile;
    private JScrollPane ParcelFile;
    private DefaultListModel<String> model = new DefaultListModel<>();
    private ActionListener buildingCreator, parcelCreator, treeCreator,
            pointFindParcel, pointFindBuilding, intervalFindParcel, intervalFindBuilding, intervalFindAll;
    private ArrayList<Log> currentLogs;
    int currentIndex;

    public View () {
        setContentPane(databaseManipulator);
        setTitle("Property Database");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 700);
        setLocationRelativeTo(null);
        setVisible(true);

        createBuildingButton.setEnabled(false);
        createParcelButton.setEnabled(false);
        intervalFindButton.setEnabled(false);
        pointFindButton.setEnabled(false);
        generateButton.setEnabled(false);
        fileBackupButton.setEnabled(false);
        findAllPropertiesInButton.setEnabled(false);
        supisneCisloTextField.setEnabled(false);
        popisTextField.setEnabled(false);
        pocetGenerovanychPrvkovTextField.setEnabled(true);
        pocetPrvkovTextField.setText("Nazov suborov");
        showEditButtons(false);
        showFindButtons(false);
        showGenerateButtons(false);
        createButton.setText("Create new tree");

        currentIndex = -1;
        resultList.setModel(model);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


        treeCreator = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (treeDepthTextField.getText().isEmpty() || !bothCoordinatesFilledUp() || pocetGenerovanychPrvkovTextField.getText().isEmpty()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                createController();
            }
        };

        createButton.addActionListener(treeCreator);

        buildingCreator = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!buildingAttributesFilledUp() || !bothCoordinatesFilledUp()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.createBuilding(Integer.parseInt(supisneCisloTextField.getText()), popisTextField.getText(),
                        (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                        (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                        (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                        (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));
            }
        };

        parcelCreator = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (popisTextField.getText().isEmpty() || !bothCoordinatesFilledUp()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.createParcel(popisTextField.getText(),
                        (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                        (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                        (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                        (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));
            }
        };



        createBuildingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentLogs != null) {
                    currentLogs.clear();
                    showResults();
                }
                createButtonVisualisation();
                enableBuildingAttributes();
                createButton.setText("Create new Building");
                createButton.removeActionListener(treeCreator);
                createButton.removeActionListener(parcelCreator);
                createButton.addActionListener(buildingCreator);
                clearFieldsAfterEditing();
            }
        });

        createParcelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentLogs != null) {
                    currentLogs.clear();
                    showResults();
                }
                createButtonVisualisation();
                enableParcelAttributes();
                createButton.setText("Create new Parcel");
                createButton.removeActionListener(treeCreator);
                createButton.removeActionListener(buildingCreator);
                createButton.addActionListener(parcelCreator);
                clearFieldsAfterEditing();
            }
        });

        intervalFindBuilding = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!bothCoordinatesFilledUp()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.findBuildings(
                        (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                        (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                        (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                        (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));
            }
        };

        intervalFindParcel = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!bothCoordinatesFilledUp()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.findParcels(
                        (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                        (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                        (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                        (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));
            }
        };

        findAllPropertiesButton.addActionListener(intervalFindAll = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!bothCoordinatesFilledUp()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                errorOutOfBorders();
                /*controller.findProperties(
                        (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                        (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                        (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                        (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));*/
            }
        });

        pointFindBuilding = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pocetGenerovanychPrvkovTextField.getText().isEmpty()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.findBuilding(Integer.parseInt(pocetGenerovanychPrvkovTextField.getText()));
            }
        };

        pointFindParcel = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pocetGenerovanychPrvkovTextField.getText().isEmpty()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.findParcel(Integer.parseInt(pocetGenerovanychPrvkovTextField.getText()));
            }
        };

        intervalFindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentLogs != null) {
                    currentLogs.clear();
                    showResults();
                }
                currentIndex = -1;
                resultList.setSelectedIndex(currentIndex);
                enableCoordinateFields();
                findVisualisation();
                findBuildingsButton.removeActionListener(pointFindBuilding);
                findBuildingsButton.addActionListener(intervalFindBuilding);
                findParcelsButton.removeActionListener(pointFindParcel);
                findParcelsButton.addActionListener(intervalFindParcel);
//                findAllPropertiesButton.removeActionListener(pointFindAll);
//                findAllPropertiesButton.addActionListener(intervalFindAll);
            }
        });

        pointFindButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentLogs != null) {
                    currentLogs.clear();
                    showResults();
                }
                currentIndex = -1;
                resultList.setSelectedIndex(currentIndex);
                enableOneCoordinateField();
                findVisualisation();
                findBuildingsButton.removeActionListener(intervalFindBuilding);
                findBuildingsButton.addActionListener(pointFindBuilding);
                findParcelsButton.removeActionListener(intervalFindParcel);
                findParcelsButton.addActionListener(pointFindParcel);
//                findAllPropertiesButton.removeActionListener(intervalFindAll);
//                findAllPropertiesButton.addActionListener(pointFindAll);
            }
        });

        generateBuildingsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pocetGenerovanychPrvkovTextField.getText().isEmpty()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.generateBuildings(Integer.parseInt(pocetGenerovanychPrvkovTextField.getText()));
            }
        });

        generateParcelsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pocetGenerovanychPrvkovTextField.getText().isEmpty()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                controller.generateParcels(Integer.parseInt(pocetGenerovanychPrvkovTextField.getText()));
            }
        });
        generateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentLogs != null) {
                    currentLogs.clear();
                    showResults();
                }
                disableCoordinateFields();
                supisneCisloTextField.setEnabled(false);
                popisTextField.setEnabled(false);
                createButton.setVisible(false);
                pocetPrvkovTextField.setText("Pocet prvkov");
                pocetGenerovanychPrvkovTextField.setEnabled(true);
                treeDepthTextField.setEnabled(false);
                showEditButtons(false);
                showFindButtons(false);
                showGenerateButtons(true);
                clearFieldsAfterEditing();
            }
        });
        resultList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                currentIndex = resultList.getSelectedIndex();
                if (currentIndex == -1)
                    return;
                Log selectedLog = currentLogs.get(currentIndex);

                minWidthComboBox.setSelectedItem(widthToString(selectedLog.getCoordinates()[0].getWidth()));
                minWidthPositionField.setText(Double.toString(selectedLog.getCoordinates()[0].getWidthPosition()));
                minLengthComboBox.setSelectedItem(lengthToString(selectedLog.getCoordinates()[0].getLength()));
                minLengthPositionField.setText(Double.toString(selectedLog.getCoordinates()[0].getLengthPosition()));

                maxWidthComboBox.setSelectedItem(widthToString(selectedLog.getCoordinates()[1].getWidth()));
                maxWidthPositionField.setText(Double.toString(selectedLog.getCoordinates()[1].getWidthPosition()));
                maxLengthComboBox.setSelectedItem(lengthToString(selectedLog.getCoordinates()[1].getLength()));
                maxLengthPositionField.setText(Double.toString(selectedLog.getCoordinates()[1].getLengthPosition()));

                popisTextField.setText(selectedLog.getDescription());

                if (selectedLog instanceof Building) {
                    enableBuildingAttributes();
                    supisneCisloTextField.setText(Integer.toString(((Building)selectedLog).getSupisneCislo()));
                } else if (selectedLog instanceof Parcel) {
                    enableParcelAttributes();
                    supisneCisloTextField.setText("");
                }

                showFindButtons(false);
                showEditButtons(true);
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO dorobit edit
                currentIndex = resultList.getSelectedIndex();
                if (currentIndex == -1)
                    return;

                if (!bothCoordinatesFilledUp()) {
                    errorNieSuVyplneneVsetkyPolia();
                    return;
                }
                Log oldLog = currentLogs.get(currentIndex);
                Log newLog = oldLog;

                if (oldLog instanceof Building) {
                    if (!buildingAttributesFilledUp()) {
                        errorNieSuVyplneneVsetkyPolia();
                        return;
                    }
                    newLog = controller.edit(oldLog, oldLog.getId(), Integer.parseInt(supisneCisloTextField.getText()), popisTextField.getText(),
                            (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                            (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                            (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                            (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));
                } else {
                    if (popisTextField.getText().isEmpty()) {
                        errorNieSuVyplneneVsetkyPolia();
                        return;
                    }
                    newLog = controller.edit(oldLog, oldLog.getId(), 0, popisTextField.getText(),
                            (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                            (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                            (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                            (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));
                }
                currentLogs.set(currentIndex, newLog);
                showResults();
                disablePropertyAttributes();
                showEditButtons(false);
                resultList.setSelectedIndex(currentIndex);
                infoDataBoliUpravene();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                currentIndex = resultList.getSelectedIndex();
                if (currentIndex == -1)
                    return;
                controller.delete(currentLogs.remove(currentIndex));
                currentIndex = -1;
                resultList.setSelectedIndex(-1);
                showResults();
                clearFieldsAfterEditing();
                disablePropertyAttributes();
                showEditButtons(false);
                infoDataBoliZmazane();
            }
        });
        fileBackupButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentLogs != null) {
                    currentLogs.clear();
                    showResults();
                }
                controller.saveInFile();

                disableCoordinateFields();
                disablePropertyAttributes();
                pocetGenerovanychPrvkovTextField.setEnabled(false);
                createButton.setVisible(false);
                showEditButtons(false);
                showFindButtons(false);
                showGenerateButtons(false);
                treeDepthTextField.setEnabled(false);
            }
        });

        findAllPropertiesInButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.findAllProperties();
                clearFieldsAfterEditing();
                disablePropertyAttributes();
                pocetGenerovanychPrvkovTextField.setEnabled(false);
                treeDepthTextField.setEnabled(false);
                showEditButtons(false);
                showFindButtons(false);
                showGenerateButtons(false);
            }
        });
    }

    public void showResults() {
        model.clear();
        for (Log current : currentLogs) {
            model.addElement(current.getFullDescription());
        }
    }

    public void showResults(ArrayList<Log> results) {
        model.clear();
        for (Log current : results) {
            model.addElement(current.getFullDescription());
        }
        currentLogs = results;
    }

    public void errorOutOfBorders() {
        JOptionPane.showMessageDialog(this, "Suradnice ktore ste zadali su mimo aktualnych suradnic stromu!",
                "Property Database", JOptionPane.ERROR_MESSAGE);
    }

    public void errorNieSuVyplneneVsetkyPolia() {
        JOptionPane.showMessageDialog(this, "Prosim vyplnte vsetky polia!",
                "Property Database", JOptionPane.ERROR_MESSAGE);
    }

    public void errorDataNeboliVygenerovane() {
        JOptionPane.showMessageDialog(this, "Pri generovani dat doslo k chybe!",
                "Property Database", JOptionPane.ERROR_MESSAGE);
    }

    public void infoDataBoliVygenerovane() {
        JOptionPane.showMessageDialog(this, "Data sa uspesne vygenerovali!",
                "Property Database", JOptionPane.INFORMATION_MESSAGE);
    }

    public void errorObjektSaNevytvoril() {
        JOptionPane.showMessageDialog(this, "Pri vytvarani objektu doslo k chybe!",
                "Property Database", JOptionPane.ERROR_MESSAGE);
    }

    public void infoObjektSaVytvoril() {
        JOptionPane.showMessageDialog(this, "Objekt sa uspesne vytvoril!",
                "Property Database", JOptionPane.INFORMATION_MESSAGE);
    }

    public void errorSuborSaNenacital() {
        JOptionPane.showMessageDialog(this, "Subor sa nepodarilo nacitat. Skontrolujte rozsah svojho stromu.",
                "Property Database", JOptionPane.ERROR_MESSAGE);
    }

    public void infoSuborSaNacital() {
        JOptionPane.showMessageDialog(this, "Subor sa uspesne nacital!",
                "Property Database", JOptionPane.INFORMATION_MESSAGE);
    }

    public void errorSuborNebolUlozeny() {
        JOptionPane.showMessageDialog(this, "Pri ukladani dat do suboru doslo k chybe.",
                "Property Database", JOptionPane.ERROR_MESSAGE);
    }

    public void infoSuborBolUlozeny() {
        JOptionPane.showMessageDialog(this, "Data sa uspesne ulozili!",
                "Property Database", JOptionPane.INFORMATION_MESSAGE);
    }

    public void infoDataBoliUpravene() {
        JOptionPane.showMessageDialog(this, "Data sa uspesne upravili!",
                "Property Database", JOptionPane.INFORMATION_MESSAGE);
    }

    public void infoDataBoliZmazane() {
        JOptionPane.showMessageDialog(this, "Data sa uspesne zmazali!",
                "Property Database", JOptionPane.INFORMATION_MESSAGE);
    }

    private void createController() {
        controller = new Controller(this, Integer.parseInt(treeDepthTextField.getText()),
                10, 3, 4, pocetGenerovanychPrvkovTextField.getText(),
                (String) minWidthComboBox.getSelectedItem(), Double.parseDouble(minWidthPositionField.getText()),
                (String) minLengthComboBox.getSelectedItem(), Double.parseDouble(minLengthPositionField.getText()),
                (String) maxWidthComboBox.getSelectedItem(), Double.parseDouble(maxWidthPositionField.getText()),
                (String) maxLengthComboBox.getSelectedItem(), Double.parseDouble(maxLengthPositionField.getText()));
        System.out.println("Strom sa vytvoril");
        createBuildingButton.setEnabled(true);
        createParcelButton.setEnabled(true);
        intervalFindButton.setEnabled(true);
        pointFindButton.setEnabled(true);
        generateButton.setEnabled(true);
        fileBackupButton.setEnabled(true);
        findAllPropertiesInButton.setEnabled(true);
        treeDepthTextField.setEnabled(false);
        treeDepthTextField.setText("");
        pocetGenerovanychPrvkovTextField.setEnabled(false);
        pocetPrvkovTextField.setText("Pocet prvkov");
        pocetGenerovanychPrvkovTextField.setText("");
        createButton.setVisible(false);
    }

    public void createButtonVisualisation() {
        createButton.setVisible(true);
        pocetGenerovanychPrvkovTextField.setEnabled(false);
        treeDepthTextField.setEnabled(false);
        showEditButtons(false);
        showFindButtons(false);
        showGenerateButtons(false);
    }

    public void findVisualisation(){
        // TODO rozdelit na dve podla toho ci bodove alebo intervalove
        supisneCisloTextField.setEnabled(false);
        popisTextField.setEnabled(false);
        createButton.setVisible(false);
        pocetGenerovanychPrvkovTextField.setEnabled(false);
        treeDepthTextField.setEnabled(false);
        showEditButtons(false);
        showFindButtons(true);
        showGenerateButtons(false);
        clearFieldsAfterEditing();
    }

    public void disablePropertyAttributes() {
        supisneCisloTextField.setEnabled(false);
        popisTextField.setEnabled(false);
        disableCoordinateFields();
    }

    public void enableBuildingAttributes() {
        enableParcelAttributes();
        supisneCisloTextField.setEnabled(true);
        number.setText("Supisne Cislo");
    }

    public void enableParcelAttributes() {
        supisneCisloTextField.setEnabled(false);
        popisTextField.setEnabled(true);
        enableCoordinateFields();
        number.setText("Cislo Parcelu");
    }

    public void enableCoordinateFields() {
        minWidthComboBox.setEnabled(true);
        minWidthPositionField.setEnabled(true);
        minLengthComboBox.setEnabled(true);
        minLengthPositionField.setEnabled(true);
        maxWidthComboBox.setEnabled(true);
        maxWidthPositionField.setEnabled(true);
        maxLengthComboBox.setEnabled(true);
        maxLengthPositionField.setEnabled(true);
    }

    public void enableOneCoordinateField() {
        minWidthComboBox.setEnabled(true);
        minWidthPositionField.setEnabled(true);
        minLengthComboBox.setEnabled(true);
        minLengthPositionField.setEnabled(true);
        maxWidthComboBox.setEnabled(false);
        maxWidthPositionField.setEnabled(false);
        maxLengthComboBox.setEnabled(false);
        maxLengthPositionField.setEnabled(false);
    }

    public void disableCoordinateFields() {
        minWidthComboBox.setEnabled(false);
        minWidthPositionField.setEnabled(false);
        minLengthComboBox.setEnabled(false);
        minLengthPositionField.setEnabled(false);
        maxWidthComboBox.setEnabled(false);
        maxWidthPositionField.setEnabled(false);
        maxLengthComboBox.setEnabled(false);
        maxLengthPositionField.setEnabled(false);
    }

    public void clearFieldsAfterEditing() {
        supisneCisloTextField.setText("");
        popisTextField.setText("");
    }

    public String widthToString(Width width) {
        if (width == Width.N) {
            return "North";
        }
        return "South";
    }

    public String lengthToString(Length length) {
        if (length == Length.E) {
            return "East";
        }
        return "West";
    }

    public void showEditButtons(boolean show) {
        editButton.setVisible(show);
        deleteButton.setVisible(show);
    }

    public void showFindButtons(boolean show) {
        findBuildingsButton.setVisible(show);
        findParcelsButton.setVisible(show);
        findAllPropertiesButton.setVisible(show);
    }

    public void showGenerateButtons(boolean show) {
        generateBuildingsButton.setVisible(show);
        generateParcelsButton.setVisible(show);
    }

    public boolean bothCoordinatesFilledUp() {
        return this.minCoordinateFilledUp() ||
                !maxWidthPositionField.getText().isEmpty() || !maxLengthPositionField.getText().isEmpty();
    }

    public boolean minCoordinateFilledUp() {
        return !minWidthPositionField.getText().isEmpty() || !minLengthPositionField.getText().isEmpty();
    }

    public boolean buildingAttributesFilledUp() {
        return !supisneCisloTextField.getText().isEmpty() || !popisTextField.getText().isEmpty();
    }

    public void showBuildingFile() {
        // TODO showBuildingFile
    }

    public void showParcelFile() {
        // TODO showParcelFile
    }
}

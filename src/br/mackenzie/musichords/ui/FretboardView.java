package br.mackenzie.musichords.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

import java.util.HashSet;
import java.util.Set;

public class FretboardView extends GridPane {

    private static final int STRINGS = 6;
    private static final int FRETS = 5; // Open string (0) + 4 frets

    // Standard tuning: e, B, G, D, A, E
    private final String[] openStrings = {"E", "B", "G", "D", "A", "E"};
    
    // Notes matrix mapping [string][fret]
    private final String[][] notesMap = {
        {"E", "F", "F#", "G", "G#"},
        {"B", "C", "C#", "D", "D#"},
        {"G", "G#", "A", "A#", "B"},
        {"D", "D#", "E", "F", "F#"},
        {"A", "A#", "B", "C", "C#"},
        {"E", "F", "F#", "G", "G#"}
    };

    private Set<String> selectedCoordinates = new HashSet<>();
    private Runnable selectionCallback;
    private boolean isInteractive = true;

    public FretboardView() {
        this.setPadding(new Insets(20));
        this.setHgap(0);
        this.setVgap(0);
        this.setAlignment(Pos.CENTER);
        this.getStyleClass().add("fretboard");

        drawFretboard();
    }

    public void setInteractive(boolean interactive) {
        this.isInteractive = interactive;
    }

    public void setSelectionCallback(Runnable callback) {
        this.selectionCallback = callback;
    }

    public Set<String> getSelectedNotes() {
        Set<String> notes = new HashSet<>();
        for (String coord : selectedCoordinates) {
            String[] parts = coord.split("_");
            int s = Integer.parseInt(parts[0]);
            int f = Integer.parseInt(parts[1]);
            notes.add(notesMap[s][f]);
        }
        return notes;
    }

    public void clearSelection() {
        selectedCoordinates.clear();
        this.getChildren().clear();
        drawFretboard();
    }

    // Displays specific shape
    public void displayShape(int[] positions) {
        // positions array: length 6 (one for each string, top to bottom)
        // value: -1 for not played, 0 for open string, 1-4 for fret
        this.getChildren().clear();
        selectedCoordinates.clear();
        drawFretboard();
        
        for (int string = 0; string < STRINGS; string++) {
            int fret = positions[string];
            if (fret >= 0 && fret < FRETS) {
                selectedCoordinates.add(string + "_" + fret);
            }
        }
        
        // Redraw with the selection
        this.getChildren().clear();
        drawFretboard();
    }

    private void drawFretboard() {
        // Headers
        Label lblSolta = new Label("solta");
        lblSolta.getStyleClass().add("fret-header");
        this.add(lblSolta, 0, 0);

        for (int f = 1; f < FRETS; f++) {
            Label lblFret = new Label(String.valueOf(f));
            lblFret.getStyleClass().add("fret-header");
            lblFret.setPrefWidth(60);
            lblFret.setAlignment(Pos.CENTER);
            this.add(lblFret, f, 0);
        }

        // Strings & Frets
        for (int s = 0; s < STRINGS; s++) {
            for (int f = 0; f < FRETS; f++) {
                StackPane cell = new StackPane();
                cell.setPrefSize(60, 40);

                // Draw String Line
                Rectangle stringLine = new Rectangle(60, 2, Color.web("#cccccc"));
                cell.getChildren().add(stringLine);

                // Draw Fret Line (only for frets > 0, at the right edge)
                if (f > 0) {
                    Rectangle fretLine = new Rectangle(4, 40, Color.web("#888888"));
                    StackPane.setAlignment(fretLine, Pos.CENTER_RIGHT);
                    cell.getChildren().add(fretLine);
                }

                // Draw Note Circle
                String note = notesMap[s][f];
                String coord = s + "_" + f;
                boolean isSelected = selectedCoordinates.contains(coord); 

                Circle circle = new Circle(14);
                circle.getStyleClass().add("note-circle");
                if (isSelected) {
                    circle.getStyleClass().add("note-circle-selected");
                } else if (f == 0) {
                    // Open string circle
                    circle.setFill(Color.TRANSPARENT);
                    circle.setStroke(Color.web("#888888"));
                    circle.setRadius(10);
                }

                Label lblNote = new Label(note);
                lblNote.getStyleClass().add("note-label");
                if (isSelected) {
                    lblNote.getStyleClass().add("note-label-selected");
                }

                if (f == 0 || isSelected || isInteractive) {
                     // always show open string letters and selected. 
                     // If interactive, show all for ease of use (like the wireframe shows all notes)
                     cell.getChildren().addAll(circle, lblNote);
                }

                if (isInteractive) {
                    cell.setOnMouseClicked(e -> {
                        if (selectedCoordinates.contains(coord)) {
                            selectedCoordinates.remove(coord);
                        } else {
                            selectedCoordinates.add(coord);
                        }
                        // Re-render
                        this.getChildren().clear();
                        drawFretboard();
                        
                        if (selectionCallback != null) {
                            selectionCallback.run();
                        }
                    });
                }

                this.add(cell, f, s + 1);
            }
        }
    }
}

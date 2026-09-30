package br.mackenzie.musichords.controller;

import br.mackenzie.musichords.model.ChordIdentifier;
import br.mackenzie.musichords.model.Grafo;
import br.mackenzie.musichords.model.Progressao;
import br.mackenzie.musichords.model.Vertice;
import br.mackenzie.musichords.ui.FretboardView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MainController {

    private BorderPane root;
    private int currentStep = 1;
    
    // Domain Model
    private Grafo grafo;
    private String identifiedChord = "";
    private List<Vertice> vertexRoles = new ArrayList<>();
    private List<Progressao> topProgressions = new ArrayList<>();

    // UI Elements
    private VBox contentArea;
    private HBox stepperArea;
    private Label stepTitle;
    private Label stepSubtitle;
    private HBox buttonArea;
    
    private Button btnNext;
    private Button btnPrev;
    private Button btnIdentify;
    
    // Feature views
    private FretboardView fretboardView;

    public MainController() {
        initDomain();
        initUI();
        showStep(1);
    }
    
    private void initDomain() {
        grafo = new Grafo();
        // Load the actual graph structure from the provided file
        File file = new File("src/grafo.txt");
        if (file.exists()) {
            grafo.lerArquivo(file.getAbsolutePath());
        } else {
            System.err.println("Atenção: Arquivo grafo.txt não encontrado em " + file.getAbsolutePath());
        }
    }

    public BorderPane getView() {
        return root;
    }

    private void initUI() {
        root = new BorderPane();
        root.setPadding(new Insets(30));
        root.getStyleClass().add("root-pane");

        // Stepper (dots)
        stepperArea = new HBox(10);
        stepperArea.setAlignment(Pos.CENTER);
        stepperArea.setPadding(new Insets(0, 0, 20, 0));

        // Titles
        stepTitle = new Label();
        stepTitle.getStyleClass().add("step-title");
        stepSubtitle = new Label();
        stepSubtitle.getStyleClass().add("step-subtitle");

        VBox header = new VBox(10, stepperArea, stepTitle, stepSubtitle);
        header.setAlignment(Pos.CENTER);
        root.setTop(header);

        // Content Area
        contentArea = new VBox(20);
        contentArea.setAlignment(Pos.CENTER);
        contentArea.setPadding(new Insets(20, 0, 20, 0));
        root.setCenter(contentArea);

        // Buttons
        btnPrev = new Button("Voltar");
        btnPrev.getStyleClass().add("btn-secondary");
        btnPrev.setOnAction(e -> showStep(currentStep - 1));

        btnNext = new Button("Avançar");
        btnNext.getStyleClass().add("btn-primary");
        btnNext.setOnAction(e -> showStep(currentStep + 1));
        
        btnIdentify = new Button("Identificar acorde");
        btnIdentify.getStyleClass().add("btn-primary");
        btnIdentify.setOnAction(e -> {
            identifyChord();
            showStep(2);
        });

        buttonArea = new HBox(15);
        buttonArea.setAlignment(Pos.CENTER);
        root.setBottom(buttonArea);
        
        // Initialize Fretboard
        fretboardView = new FretboardView();
        fretboardView.setSelectionCallback(() -> {
            updateButtonState();
        });
    }

    private void updateStepper() {
        stepperArea.getChildren().clear();
        for (int i = 1; i <= 6; i++) {
            Circle dot = new Circle(4);
            if (i == currentStep) {
                dot.getStyleClass().add("stepper-dot-active");
            } else {
                dot.getStyleClass().add("stepper-dot");
            }
            stepperArea.getChildren().add(dot);
        }
    }
    
    private void updateButtonState() {
        if (currentStep == 1) {
            btnIdentify.setDisable(fretboardView.getSelectedNotes().size() < 3);
        }
    }

    private void showStep(int step) {
        if (step < 1 || step > 6) return;
        this.currentStep = step;
        updateStepper();
        contentArea.getChildren().clear();
        buttonArea.getChildren().clear();

        switch (step) {
            case 1:
                stepTitle.setText("PASSO 1\nToque no braço da guitarra");
                stepSubtitle.setText("Selecione pelo menos 3 notas para formar um acorde.");
                fretboardView.setInteractive(true);
                contentArea.getChildren().add(fretboardView);
                
                Button btnClear = new Button("Limpar");
                btnClear.getStyleClass().add("btn-secondary");
                btnClear.setOnAction(e -> {
                    fretboardView.clearSelection();
                    updateButtonState();
                });
                
                buttonArea.getChildren().addAll(btnClear, btnIdentify);
                updateButtonState();
                break;
                
            case 2:
                stepTitle.setText("PASSO 2\nIdentificação do acorde");
                stepSubtitle.setText("");
                
                VBox identifyBox = new VBox(10);
                identifyBox.setAlignment(Pos.CENTER);
                identifyBox.getStyleClass().add("card");
                
                Label lblNotes = new Label("Notas selecionadas: " + String.join(", ", fretboardView.getSelectedNotes()));
                Label lblChord = new Label(identifiedChord);
                lblChord.getStyleClass().add("chord-title");
                
                identifyBox.getChildren().addAll(lblNotes, lblChord);
                contentArea.getChildren().add(identifyBox);
                
                buttonArea.getChildren().addAll(btnPrev, btnNext);
                break;
                
            case 3:
                stepTitle.setText("PASSO 3\nMapeamento em grafos de campos harmônicos");
                stepSubtitle.setText("Vértices no grafo correspondentes ao acorde físico.");
                
                VBox casesBox = new VBox(15);
                casesBox.setAlignment(Pos.CENTER);
                
                // Fetch real vertices from Graph
                vertexRoles = grafo.buscarVerticesPorAcorde(identifiedChord);
                
                if (vertexRoles.isEmpty()) {
                    casesBox.getChildren().add(createCard("NENHUM PAPEL ENCONTRADO", "O acorde não foi mapeado no grafo."));
                } else {
                    int caseNum = 1;
                    for (Vertice v : vertexRoles) {
                        String roleDesc = translateRole(v.getRotulo());
                        casesBox.getChildren().add(createCard("CASO " + caseNum + " — " + roleDesc.toUpperCase(), "Vértice: " + v.getRotulo()));
                        caseNum++;
                    }
                }
                
                ScrollPane scrollPane = new ScrollPane(casesBox);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                scrollPane.setPrefHeight(300);
                
                Label lblFooter = new Label("acorde físico → múltiplos vértices no grafo");
                lblFooter.setStyle("-fx-text-fill: #888888;");
                
                contentArea.getChildren().addAll(scrollPane, lblFooter);
                buttonArea.getChildren().addAll(btnPrev, btnNext);
                break;
                
            case 4:
                stepTitle.setText("PASSO 4\nProgressões sugeridas");
                stepSubtitle.setText("Calculadas usando algoritmo de caminho no grafo (Dijkstra/DFS).");
                
                VBox progBox = new VBox(15);
                progBox.setAlignment(Pos.CENTER);
                
                // Generate progressions from the graph logic
                topProgressions.clear();
                for (Vertice v : vertexRoles) {
                    List<Progressao> paths = grafo.buscarProgressoes(v, 4);
                    topProgressions.addAll(paths);
                }
                Collections.sort(topProgressions);
                
                if (topProgressions.isEmpty()) {
                    progBox.getChildren().add(createProgCard("Nenhuma progressão", "Aviso", "Não há caminhos de 4 acordes válidos partindo deste vértice."));
                } else {
                    int count = 0;
                    for (Progressao p : topProgressions) {
                        if (count >= 5) break; // Display top 5
                        String diffBadge = count == 0 ? "Mais fácil" : "Custo Físico: " + p.getCustoTotal();
                        progBox.getChildren().add(createProgCard(p.toString(), diffBadge, "Progression gerada automaticamente pelo algoritmo"));
                        count++;
                    }
                }
                
                ScrollPane scrollProg = new ScrollPane(progBox);
                scrollProg.setFitToWidth(true);
                scrollProg.setStyle("-fx-background-color: transparent; -fx-border-color: transparent;");
                scrollProg.setPrefHeight(300);
                
                contentArea.getChildren().add(scrollProg);
                
                Button btnViewShapes = new Button("Ver shapes");
                btnViewShapes.getStyleClass().add("btn-primary");
                btnViewShapes.setDisable(topProgressions.isEmpty());
                btnViewShapes.setOnAction(e -> showStep(5));
                buttonArea.getChildren().addAll(btnPrev, btnViewShapes);
                break;
                
            case 5:
                stepTitle.setText("PASSO 5\nShapes da progressão escolhida");
                stepSubtitle.setText("");
                
                if (topProgressions.isEmpty()) {
                    contentArea.getChildren().add(new Label("Nenhuma progressão para exibir."));
                    buttonArea.getChildren().addAll(btnPrev, btnNext);
                    break;
                }
                
                Progressao selectedProgression = topProgressions.get(0);
                
                FretboardView shapeView = new FretboardView();
                shapeView.setInteractive(false);
                // Since we don't have a real shape database yet, we fallback to a mock C Major shape.
                shapeView.displayShape(new int[] {0, 1, 0, 2, 3, -1});
                
                Label lblShapeName = new Label(selectedProgression.toString());
                lblShapeName.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
                
                HBox pagination = new HBox(15);
                pagination.setAlignment(Pos.CENTER);
                Button btnL = new Button("<");
                btnL.getStyleClass().add("btn-circle");
                Button btnR = new Button(">");
                btnR.getStyleClass().add("btn-circle");
                pagination.getChildren().addAll(btnL, lblShapeName, btnR);
                
                contentArea.getChildren().addAll(shapeView, pagination);
                
                Button btnExtras = new Button("Ver extras");
                btnExtras.getStyleClass().add("btn-primary");
                btnExtras.setOnAction(e -> showStep(6));
                
                buttonArea.getChildren().addAll(btnPrev, btnExtras);
                break;
                
            case 6:
                stepTitle.setText("EXTRAS\nContinue treinando");
                stepSubtitle.setText("");
                
                VBox extrasBox = new VBox(20);
                extrasBox.setAlignment(Pos.CENTER);
                
                VBox musicBox = createCard("MÚSICAS COM ESSA PROGRESSÃO", "• Faixa sugerida A\n• Faixa sugerida B\n• Faixa sugerida C");
                VBox scaleBox = createCard("ESCALAS PARA TREINAR", "• Escala maior da tonalidade identificada\n• Pentatônica relativa");
                
                extrasBox.getChildren().addAll(musicBox, scaleBox);
                contentArea.getChildren().add(extrasBox);
                
                Button btnRestart = new Button("Recomeçar");
                btnRestart.getStyleClass().add("btn-primary");
                btnRestart.setOnAction(e -> {
                    fretboardView.clearSelection();
                    showStep(1);
                });
                buttonArea.getChildren().addAll(btnPrev, btnRestart);
                break;
        }
    }
    
    private void identifyChord() {
        Set<String> notes = fretboardView.getSelectedNotes();
        identifiedChord = ChordIdentifier.identifyChord(notes);
    }
    
    private String translateRole(String label) {
        String[] parts = label.split("_");
        if(parts.length < 2) return "Desconhecido";
        String key = parts[0];
        String deg = parts[1];
        
        String role = "Desconhecido";
        switch (deg.toLowerCase()) {
            case "i": role = "Tônica"; break;
            case "ii": role = "Super Tônica"; break;
            case "iii": role = "Mediante"; break;
            case "iv": role = "Subdominante"; break;
            case "v": role = "Dominante"; break;
            case "vi": role = "Super Dominante"; break;
            case "vii": role = "Sensível"; break;
        }
        return role + " de " + key + " Maior";
    }
    
    private VBox createCard(String title, String content) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        Label lblT = new Label(title);
        lblT.setStyle("-fx-text-fill: #666666; -fx-font-size: 12px; -fx-font-weight: bold;");
        Label lblC = new Label(content);
        lblC.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        card.getChildren().addAll(lblT, lblC);
        return card;
    }
    
    private VBox createProgCard(String prog, String badge, String desc) {
        VBox card = new VBox(5);
        card.getStyleClass().add("card");
        
        HBox header = new HBox(10);
        Label lblT = new Label(prog);
        lblT.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        Label lblB = new Label(badge);
        lblB.setStyle("-fx-background-color: #E6F0FF; -fx-text-fill: #0052CC; -fx-padding: 3 8 3 8; -fx-background-radius: 12; -fx-font-size: 11px;");
        header.getChildren().addAll(lblT, lblB);
        
        Label lblD = new Label(desc);
        lblD.setWrapText(true);
        lblD.setStyle("-fx-text-fill: #555555;");
        
        card.getChildren().addAll(header, lblD);
        return card;
    }
}

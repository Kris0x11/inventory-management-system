package myapp.Applicazione;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.stage.Modality;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Text;
import javafx.util.converter.IntegerStringConverter;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.List;

public class GUI {
    private TableView<Articolo> tableView;
    private Button delete;
    private Button aggiorna;
    private Button salva;
    private Button excel;

    private ObservableList<Articolo> articoli = FXCollections.observableArrayList();
   
    private ObservableList<RigaFattura> righePreventivo = FXCollections.observableArrayList();

    public GUI() {
        configuraTabella();
        delete();
        aggiorna();
        tabellaExcel(); 
    }

    // ---------- PREVENTIVO / EXCEL DIALOG ----------
    @SuppressWarnings("unchecked")
    public void tabellaExcel() {
        excel = new Button("Preventivo / Excel");
        excel.setOnAction(ev -> {
            Stage excelStage = new Stage();
            excelStage.initModality(Modality.APPLICATION_MODAL);
            excelStage.setTitle("Seleziona articoli - Preventivo");

            // Table preventivo (righe)
            TableView<RigaFattura> preventiviTable = new TableView<>();
            TableColumn<RigaFattura, String> pcod = new TableColumn<>("Codice");
            pcod.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArticolo().getCodiceArticolo()));

            TableColumn<RigaFattura, String> pnome = new TableColumn<>("Nome");
            pnome.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArticolo().getNome()));

            TableColumn<RigaFattura, String> pdesc = new TableColumn<>("Descrizione");
            pdesc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getArticolo().getDescrizione()));
            pdesc.setCellFactory(column -> {
                return new javafx.scene.control.TableCell<RigaFattura, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (item == null || empty) setText(null);
                        else {
                            setText(item);
                            Text text = new Text(item);
                            double textWidth = text.getLayoutBounds().getWidth() + 10;
                            if (textWidth > getTableColumn().getPrefWidth()) getTableColumn().setPrefWidth(textWidth);
                        }
                    }
                };
            });

            TableColumn<RigaFattura, Number> pprezzo = new TableColumn<>("Prezzo");
            pprezzo.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzoUnitario()));

            TableColumn<RigaFattura, Integer> pquant = new TableColumn<>("Quantità");
            pquant.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantita()).asObject());

            TableColumn<RigaFattura, Number> ptot = new TableColumn<>("Totale");
            ptot.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getTotale()));

            preventiviTable.getColumns().addAll(pcod, pnome, pdesc, pprezzo, pquant, ptot);
            preventiviTable.setItems(righePreventivo);

            // Combobox articoli + quantità
            ComboBox<Articolo> scatola = new ComboBox<>();
            loadArticlesFromDatabase(scatola);

            TextField quantityField = new TextField();
            quantityField.setPromptText("Quantità");

            Button addButton = new Button("Aggiungi");
            addButton.setOnAction(e -> {
                Articolo selected = scatola.getValue();
                if (selected == null) {
                    new Alert(Alert.AlertType.WARNING, "Seleziona un articolo.").showAndWait();
                    return;
                }
                String qtxt = quantityField.getText();
                if (qtxt == null || qtxt.isBlank()) {
                    new Alert(Alert.AlertType.WARNING, "Inserisci quantità.").showAndWait();
                    return;
                }
                int q;
                try { q = Integer.parseInt(qtxt); }
                catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.WARNING, "Quantità non valida.").showAndWait();
                    return;
                }
                if (q <= 0) {
                    new Alert(Alert.AlertType.WARNING, "Quantità deve essere > 0.").showAndWait();
                    return;
                }

                // Controllo sulla disponibilità (usa getDisponibile())
                Articolo fresh = GestioneArticoli.getArticoloById(selected.getId());
                if (fresh == null) {
                    new Alert(Alert.AlertType.ERROR, "Articolo non trovato su DB.").showAndWait();
                    return;
                }
                if (q > fresh.getDisponibile()) {
                    new Alert(Alert.AlertType.ERROR, "Quantità non disponibile. Disponibile: " + fresh.getDisponibile()).showAndWait();
                    return;
                }

                // Impegna in DB
                boolean ok = GestioneArticoli.impegnaArticolo(fresh.getId(), q);
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR, "Errore impegno articolo nel DB.").showAndWait();
                    return;
                }

                // Aggiungi riga preventivo (usa copia dell'articolo per visualizzare snapshot)
                Articolo artCopy = new Articolo(fresh);
                artCopy.setQuantita(q); // nella riga mostriamo la quantità richiesta
                RigaFattura r = new RigaFattura(artCopy, q, fresh.getPrezzo());
                righePreventivo.add(r);

                // Aggiorna combo (rileggi DB)
                scatola.getItems().clear();
                loadArticlesFromDatabase(scatola);

                scatola.setValue(null);
                quantityField.clear();
            });

            Button removeRow = new Button("Rimuovi riga");
            removeRow.setOnAction(e -> {
                RigaFattura sel = preventiviTable.getSelectionModel().getSelectedItem();
                if (sel == null) {
                    new Alert(Alert.AlertType.WARNING, "Seleziona una riga da rimuovere.").showAndWait();
                    return;
                }
                // libera nel DB
                boolean ok = GestioneArticoli.liberaArticolo(sel.getArticolo().getId(), sel.getQuantita());
                if (!ok) {
                    new Alert(Alert.AlertType.ERROR, "Errore liberazione articolo nel DB.").showAndWait();
                    return;
                }
                righePreventivo.remove(sel);
                // ricarica combo
                scatola.getItems().clear();
                loadArticlesFromDatabase(scatola);
            });

            Button generaPreventivoExcel = new Button("Esporta Preventivo (.xlsx)");
            generaPreventivoExcel.setOnAction(e -> {
                if (righePreventivo.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Preventivo vuoto.").showAndWait();
                    return;
                }
                // genera file preventivo.xlsx
                generaExcelPreventivo(preventiviTable);
            });

            Button confermaVendita = new Button("Conferma Vendita (Fattura)");
            confermaVendita.setOnAction(e -> {
                if (righePreventivo.isEmpty()) {
                    new Alert(Alert.AlertType.WARNING, "Preventivo vuoto.").showAndWait();
                    return;
                }
                // ricontrollo disponibilità su DB per ogni riga
                for (RigaFattura r : righePreventivo) {
                    Articolo a = GestioneArticoli.getArticoloById(r.getArticolo().getId());
                    if (a == null || r.getQuantita() > a.getDisponibile()) {
                        new Alert(Alert.AlertType.ERROR, "Quantità insufficiente per articolo: " + (a != null ? a.getNome() : r.getArticolo().getCodiceArticolo())).showAndWait();
                        return;
                    }
                }

                // esegui vendita riga per riga
                boolean allOk = true;
                for (RigaFattura r : righePreventivo) {
                    boolean ok = GestioneArticoli.vendita(r.getArticolo().getId(), r.getQuantita());
                    if (!ok) {
                        allOk = false;
                        break;
                    }
                }
                if (!allOk) {
                    new Alert(Alert.AlertType.ERROR, "Errore durante la vendita. Operazione interrotta.").showAndWait();
                    // Nota: per robustezza in produzione usare transazione DB
                    return;
                }

                // tutto ok: crea oggetto Fattura e genera excel
                int numeroFattura = (int) (System.currentTimeMillis() / 1000L);
                Fattura fattura = new Fattura(numeroFattura);
                for (RigaFattura r : righePreventivo) {
                    fattura.addRiga(r.getArticolo(), r.getQuantita(), r.getPrezzoUnitario());
                }

                boolean saved = GeneratoreExcel.creaFatturaExcel(fattura, "fattura_" + numeroFattura + ".xlsx");
                if (saved) {
                    new Alert(Alert.AlertType.INFORMATION, "Vendita confermata e fattura generata.").showAndWait();
                    righePreventivo.clear();
                    // aggiorna tabella principale
                    refresh();
                    // ricarica combo items
                    scatola.getItems().clear();
                    loadArticlesFromDatabase(scatola);
                } else {
                    new Alert(Alert.AlertType.ERROR, "Vendita confermata ma errore salvataggio fattura.").showAndWait();
                }
            });

            VBox layout = new VBox(8);
            layout.setPadding(new Insets(10));
            ToolBar tb = new ToolBar(scatola, quantityField, addButton, removeRow, generaPreventivoExcel, confermaVendita);
            layout.getChildren().addAll(tb, preventiviTable);

            Scene scene = new Scene(layout, 800, 400);
            excelStage.setScene(scene);
            excelStage.showAndWait();
        });
    }

    // ---------- TABLE VIEW PRINCIPALE ----------
    public void configuraTabella() {
        tableView = new TableView<>();

        TableColumn<Articolo, String> idColonna = new TableColumn<>("Codice");
        idColonna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCodiceArticolo()));

        TableColumn<Articolo, String> nomeColonna = new TableColumn<>("Nome");
        nomeColonna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNome()));

        TableColumn<Articolo, String> descColonna = new TableColumn<>("Descrizione");
        descColonna.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDescrizione()));
        descColonna.setCellFactory(column -> {
            return new javafx.scene.control.TableCell<Articolo, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) setText(null);
                    else {
                        setText(item);
                        Text text = new Text(item);
                        double textWidth = text.getLayoutBounds().getWidth() + 10;
                        if (textWidth > getTableColumn().getPrefWidth()) getTableColumn().setPrefWidth(textWidth);
                    }
                }
            };
        });

        TableColumn<Articolo, Number> prezzoColonna = new TableColumn<>("Prezzo");
        prezzoColonna.setCellValueFactory(c -> new SimpleDoubleProperty(c.getValue().getPrezzo()));

        TableColumn<Articolo, Integer> quantColonna = new TableColumn<>("Quantità");
        quantColonna.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantita()).asObject());
        quantColonna.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        quantColonna.setOnEditCommit(event -> {
            Articolo a = event.getRowValue();
            int newQ = event.getNewValue();
            a.setQuantita(newQ);
            
            boolean ok = GestioneArticoli.aggiornaArticolo(a.getCodiceArticolo(), a.getNome(), a.getDescrizione(), a.getPrezzo(), newQ, a.getId());
            if (!ok) {
                new Alert(Alert.AlertType.ERROR, "Errore aggiornamento quantità su DB").showAndWait();
                refresh();
            }
        });

        TableColumn<Articolo, Integer> impegnataCol = new TableColumn<>("Impegnata");
        impegnataCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getQuantitaImpegnata()).asObject());

        TableColumn<Articolo, Integer> disponibileCol = new TableColumn<>("Disponibile");
        disponibileCol.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getDisponibile()).asObject());

        tableView.getColumns().addAll(idColonna, nomeColonna, descColonna, prezzoColonna, quantColonna, impegnataCol, disponibileCol);
        

        // inizializza dati
        refresh();
    }

    // ---------- CRUD UI ----------
    public void delete() {
        delete = new Button("Cancella");
        delete.setOnAction(e -> {
            Articolo sel = tableView.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            boolean success = GestioneArticoli.cancArticolo(sel.getId());
            if (success) {
                new Alert(Alert.AlertType.INFORMATION, "Articolo cancellato").showAndWait();
                refresh();
            } else {
                new Alert(Alert.AlertType.ERROR, "Errore cancellazione").showAndWait();
            }
        });
    }

    public void inserisci() {
        Stage stage = new Stage();

        TextField codice = new TextField();
        TextField nome = new TextField();
        TextField descr = new TextField();
        TextField prezzo = new TextField();
        TextField quant = new TextField();

        Label codicel = new Label("Codice");
        Label nomel = new Label("Nome");
        Label descrl = new Label("Descrizione");
        Label prezzol = new Label("Prezzo");
        Label quantl = new Label("Quantità");

        Button inserisci = new Button("Inserisci");

        inserisci.setOnAction(e -> {
            String nomeCodice = codice.getText().trim();
            String nomeText = nome.getText().trim();
            String descText = descr.getText().trim();

            double prezzoText;
            int quantText;

            try {
                prezzoText = Double.parseDouble(prezzo.getText());
                quantText = Integer.parseInt(quant.getText());
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Prezzo o quantità non validi").showAndWait();
                return;
            }

            if (nomeCodice.isEmpty() || nomeText.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Codice e Nome sono obbligatori.").showAndWait();
                return;
            }

            boolean evento = GestioneArticoli.inserisciArticolo(
                nomeCodice, nomeText, descText, prezzoText, quantText
            );

            if (evento) {
                new Alert(Alert.AlertType.INFORMATION, "Articolo inserito!").showAndWait();
                refresh();  // 🔥 aggiorna immediatamente la tabella principale
                stage.close();  // 🔥 chiude la finestra dopo inserimento
            } else {
                new Alert(Alert.AlertType.ERROR, "Errore durante l'inserimento").showAndWait();
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(8);

        grid.add(codicel, 0, 0);
        grid.add(codice, 1, 0);
        grid.add(nomel, 0, 1);
        grid.add(nome, 1, 1);
        grid.add(descrl, 0, 2);
        grid.add(descr, 1, 2);
        grid.add(quantl, 0, 3);
        grid.add(quant, 1, 3);
        grid.add(prezzol, 0, 4);
        grid.add(prezzo, 1, 4);
        grid.add(inserisci, 1, 5);

        Scene scene = new Scene(grid, 360, 280);
        stage.setTitle("Inserisci Articolo");
        stage.setScene(scene);
        stage.show();
        tableView.refresh();
        
    }

    public VBox getLayout() {
        VBox layout = new VBox(10);

        layout.getChildren().addAll(tableView, delete, aggiorna, excel);
        return layout;
    }


    public TableView<Articolo> getTableView() {
        return tableView;
    }

    public void refresh() {
        List<Articolo> list = GestioneArticoli.getArticoli();
        articoli.setAll(list);
        tableView.setItems(articoli);
    }

    // legacy method left for compatibility but now uses GestioneArticoli.getArticoli()
    public void setArticoli(ResultSet rs) {
        // not used in new flow; kept for compatibility with older code
        refresh();
    }

    public void aggiorna() {
        aggiorna = new Button("Aggiorna");
        salva = new Button("Salva");

        aggiorna.setOnAction(e -> {
            Articolo selezionato = tableView.getSelectionModel().getSelectedItem();
            if (selezionato == null) return;

            Stage finestra = new Stage();
            finestra.setTitle("Aggiorna articolo");
            VBox layout = new VBox(10);
            layout.setPadding(new Insets(20));

            TextField codiceField = new TextField(selezionato.getCodiceArticolo());
            TextField nomeField = new TextField(selezionato.getNome());
            TextField descrizioneField = new TextField(selezionato.getDescrizione());
            TextField prezzoField = new TextField(String.valueOf(selezionato.getPrezzo()));
            TextField quantitaField = new TextField(String.valueOf(selezionato.getQuantita()));

            salva.setOnAction(a -> {
                // applica i cambiamenti al modello e DB
                selezionato.setCodiceArticolo(codiceField.getText());
                selezionato.setNome(nomeField.getText());
                selezionato.setDescrizione(descrizioneField.getText());
                double p;
                int q;
                try {
                    p = Double.parseDouble(prezzoField.getText());
                    q = Integer.parseInt(quantitaField.getText());
                } catch (NumberFormatException ex) {
                    new Alert(Alert.AlertType.ERROR, "Prezzo/quantità non validi").showAndWait();
                    return;
                }
                selezionato.setPrezzo(p);
                selezionato.setQuantita(q);

                boolean successo = GestioneArticoli.aggiornaArticolo(selezionato.getCodiceArticolo(), selezionato.getNome(),
                        selezionato.getDescrizione(), selezionato.getPrezzo(), selezionato.getQuantita(), selezionato.getId());
                if (successo) new Alert(Alert.AlertType.INFORMATION, "Articolo aggiornato").showAndWait();
                else new Alert(Alert.AlertType.ERROR, "Errore aggiornamento").showAndWait();

                finestra.close();
                refresh();
            });

            layout.getChildren().addAll(new Label("Codice:"), codiceField,
                    new Label("Nome:"), nomeField,
                    new Label("Descrizione:"), descrizioneField,
                    new Label("Prezzo:"), prezzoField,
                    new Label("Quantità:"), quantitaField,
                    salva);

            Scene scena = new Scene(layout, 360, 320);
            finestra.setScene(scena);
            finestra.showAndWait();
        });
    }

    // ---------- EXCEL EXPORT for PREVENTIVO ----------
    private void generaExcelPreventivo(TableView<RigaFattura> preventivi) {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Preventivo");

        int rowIndex = 0;
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Codice");
        header.createCell(1).setCellValue("Nome");
        header.createCell(2).setCellValue("Descrizione");
        header.createCell(3).setCellValue("Prezzo");
        header.createCell(4).setCellValue("Quantità");
        header.createCell(5).setCellValue("Totale");

        double totale = 0;
        for (RigaFattura r : preventivi.getItems()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(r.getArticolo().getCodiceArticolo());
            row.createCell(1).setCellValue(r.getArticolo().getNome());
            Cell desc = row.createCell(2);
            desc.setCellValue(r.getArticolo().getDescrizione());
            CellStyle cs = workbook.createCellStyle();
            cs.setWrapText(true);
            desc.setCellStyle(cs);

            row.createCell(3).setCellValue(r.getPrezzoUnitario());
            row.createCell(4).setCellValue(r.getQuantita());
            row.createCell(5).setCellValue(r.getTotale());
            totale += r.getTotale();
        }

        Row totalRow = sheet.createRow(rowIndex++);
        totalRow.createCell(4).setCellValue("Totale");
        totalRow.createCell(5).setCellValue(totale);

        for (int i = 0; i <= 5; i++) sheet.autoSizeColumn(i);

        try (FileOutputStream out = new FileOutputStream("preventivo.xlsx")) {
            workbook.write(out);
            workbook.close();
            new Alert(Alert.AlertType.INFORMATION, "Preventivo esportato: preventivo.xlsx").showAndWait();
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "Errore esportazione: " + ex.getMessage()).showAndWait();
        }
    }

    // ---------- CARICA ARTICOLI (usa GestioneArticoli per coerenza DB) ----------
    private void loadArticlesFromDatabase(ComboBox<Articolo> articlesComboBox) {
        articlesComboBox.getItems().clear();
        for (Articolo a : GestioneArticoli.getArticoli()) {
            articlesComboBox.getItems().add(a);
        }
    }
}




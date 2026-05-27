package myapp.Applicazione;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class GeneratoreExcel {

    public static boolean creaFatturaExcel(Fattura fattura, String path) {

        Workbook wb = new XSSFWorkbook();
        Sheet sheet = wb.createSheet("Fattura");

        int rowIndex = 0;

        // Intestazione
        Row info = sheet.createRow(rowIndex++);
        info.createCell(0).setCellValue("Fattura n° " + fattura.getNumeroFattura());
        info.createCell(1).setCellValue("Data: " + fattura.getData().toString());

        rowIndex++;

        // Header tabella
        Row header = sheet.createRow(rowIndex++);
        header.createCell(0).setCellValue("Codice");
        header.createCell(1).setCellValue("Nome");
        header.createCell(2).setCellValue("Prezzo");
        header.createCell(3).setCellValue("Quantità");
        header.createCell(4).setCellValue("Totale Riga");

        // Righe fattura
        for (RigaFattura r : fattura.getRighe()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(r.getArticolo().getCodiceArticolo());
            row.createCell(1).setCellValue(r.getArticolo().getNome());
            row.createCell(2).setCellValue(r.getPrezzoUnitario());
            row.createCell(3).setCellValue(r.getQuantita());
            row.createCell(4).setCellValue(r.getTotale());
        }

        // Totale finale
        Row totale = sheet.createRow(rowIndex++);
        totale.createCell(3).setCellValue("Totale:");
        totale.createCell(4).setCellValue(fattura.getTotale());

        // Salva file
        try (FileOutputStream out = new FileOutputStream(path)) {
            wb.write(out);
            wb.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}

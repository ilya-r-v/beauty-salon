package ru.mirea.beautysalon.fx.controller;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.beautysalon.fx.viewmodel.BookingRow;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Экспорт записей в Excel.
// Работает с готовыми BookingRow — всё нужное уже в них, JDBC не нужен.
public class ExcelExporter {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private ExcelExporter() {}

    public static void exportBookings(List<BookingRow> rows, String filePath) throws IOException {
        try (Workbook wb = new XSSFWorkbook(); FileOutputStream out = new FileOutputStream(filePath)) {
            Sheet sheet = wb.createSheet("Записи");

            // Заголовок
            Row header = sheet.createRow(0);
            CellStyle headerStyle = boldStyle(wb);
            String[] columns = {"Клиент", "Мастер", "Услуга", "Цена (руб.)", "Статус", "Время начала"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // Данные
            int rowNum = 1;
            for (BookingRow r : rows) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(r.getClientName());
                row.createCell(1).setCellValue(r.getMasterName());
                row.createCell(2).setCellValue(r.getVariantTitle());
                row.createCell(3).setCellValue(r.getPrice().toPlainString());
                row.createCell(4).setCellValue(r.getStatus());
                String time = r.getStartTime() != null ? r.getStartTime().format(FMT) : "—";
                row.createCell(5).setCellValue(time);
            }

            // Автоширина колонок
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
        }
    }

    private static CellStyle boldStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }
}

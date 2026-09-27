package ru.mirea.beautysalon.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.mirea.beautysalon.model.Booking;
import ru.mirea.beautysalon.model.Client;
import ru.mirea.beautysalon.model.ServiceVariant;
import ru.mirea.beautysalon.repository.ClientRepository;
import ru.mirea.beautysalon.repository.ServiceVariantRepository;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ExcelExporter {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ClientRepository clientRepository;
    private final ServiceVariantRepository serviceVariantRepository;

    public ExcelExporter(ClientRepository clientRepository, ServiceVariantRepository serviceVariantRepository) {
        this.clientRepository = clientRepository;
        this.serviceVariantRepository = serviceVariantRepository;
    }

    public void exportBookings(List<Booking> bookings, String filePath) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Записи на услугу");

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            String[] headers = {"ID записи", "Клиент", "Телефон", "Услуга", "Цена", "Статус", "Дата и время"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIndex = 1;
            for (Booking booking : bookings) {
                Row row = sheet.createRow(rowIndex++);

                Client client = clientRepository.findById(booking.getClientId()).orElse(null);
                ServiceVariant variant = serviceVariantRepository.findById(booking.getServiceVariantId()).orElse(null);

                row.createCell(0).setCellValue(booking.getId().toString());
                row.createCell(1).setCellValue(client != null ? client.getName() : "—");
                row.createCell(2).setCellValue(client != null ? client.getPhone() : "—");
                row.createCell(3).setCellValue(variant != null ? variant.getTitle() : "—");
                row.createCell(4).setCellValue(variant != null ? variant.getPrice().doubleValue() : 0);
                row.createCell(5).setCellValue(booking.getStatus().name());
                row.createCell(6).setCellValue(booking.getTime().format(TIME_FMT));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            try (FileOutputStream out = new FileOutputStream(filePath)) {
                workbook.write(out);
            }
        }
    }
}

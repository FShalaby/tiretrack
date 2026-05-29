package com.aem.tiretrack.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aem.tiretrack.dto.TireImportResponse;
import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.repository.TireRepository;

@Service
public class TireService
{
    private final TireRepository tireRepository;
    private final AuditLogService auditLogService;

    public TireService(TireRepository tireRepository, AuditLogService auditLogService) {
        this.tireRepository = tireRepository;
        this.auditLogService = auditLogService;
    }

    public List<Tire> getAllTires() {
        return tireRepository.findAll();
    }

    public Tire getTireById(Long id) {
        return tireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tire not found"));
    }

    @Transactional
    public Tire saveTire(Tire tire) {
        Tire savedTire = tireRepository.save(tire);
        ensureBatchCodes(savedTire);
        auditLogService.record("INVENTORY_CREATED", "Tire", savedTire.getId(), "Created tire " + savedTire.getBrand() + " " + savedTire.getTireSize(), getCurrentUsername());
        return savedTire;
    }

    @Transactional
    public Tire updateTire(Long id, Tire updatedTire) 
    {
        Tire existingTire = getTireById(id);

        existingTire.setBrand(updatedTire.getBrand());
        existingTire.setModel(updatedTire.getModel());
        existingTire.setWidth(updatedTire.getWidth());
        existingTire.setAspectRatio(updatedTire.getAspectRatio());
        existingTire.setRimSize(updatedTire.getRimSize());
        existingTire.setSeason(updatedTire.getSeason());
        existingTire.setCondition(updatedTire.getCondition());
        existingTire.setQuantity(updatedTire.getQuantity());
        existingTire.setPrice(updatedTire.getPrice());
        existingTire.setLocation(updatedTire.getLocation());

        Tire savedTire = tireRepository.save(existingTire);
        ensureBatchCodes(savedTire);
        auditLogService.record("INVENTORY_UPDATED", "Tire", savedTire.getId(), "Updated tire " + savedTire.getBrand() + " " + savedTire.getTireSize(), getCurrentUsername());
        return savedTire;
    }

    public Tire getTireByBarcode(String barcode) {
        String normalizedBarcode = barcode == null ? "" : barcode.trim();

        if (normalizedBarcode.isBlank()) {
            throw new RuntimeException("Enter a barcode to search");
        }

        return tireRepository.findByBarcodeIgnoreCase(normalizedBarcode)
                .or(() -> tireRepository.findByBatchCodeIgnoreCase(normalizedBarcode))
                .or(() -> findExistingBatchByGeneratedCode(normalizedBarcode))
                .orElseThrow(() -> new RuntimeException("Barcode not found: " + normalizedBarcode));
    }

    public List<Tire> searchByBrand(String brand) {
        return tireRepository.findByBrandContainingIgnoreCase(brand);
    }

    public List<Tire> searchBySize(int width, int aspectRatio, int rimSize) {
        return tireRepository.findByWidthAndAspectRatioAndRimSize(width, aspectRatio, rimSize);
    }

    public List<Tire> searchByCondition(Condition condition) {
        return tireRepository.findByCondition(condition);
    }

    public List<Tire> searchBySeason(String season) {
        return tireRepository.findBySeasonContainingIgnoreCase(season);
    }

    public List<Tire> searchByLocation(String location) {
        return tireRepository.findByLocationContainingIgnoreCase(location);
    }

    public List<Tire> getLowStockTires(int threshold) {
        return tireRepository.findByAvailableQuantityLessThanEqual(threshold);
    }

    @Transactional
    public TireImportResponse importTiresCsv(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Choose a CSV file to import");
        }

        List<List<String>> rows = parseCsv(file);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("CSV file is empty");
        }

        Map<String, Integer> headers = headersByName(rows.get(0));
        int createdCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;
        List<String> errors = new ArrayList<>();

        for (int index = 1; index < rows.size(); index += 1) {
            List<String> row = rows.get(index);
            int rowNumber = index + 1;

            if (isBlankRow(row)) {
                continue;
            }

            try {
                Tire importTire = tireFromCsvRow(headers, row, rowNumber);
                Optional<Tire> existingMatch = findCsvImportMatch(headers, row, importTire);

                if (existingMatch.isPresent()) {
                    Tire existingTire = existingMatch.get();
                    existingTire.setBrand(importTire.getBrand());
                    existingTire.setWidth(importTire.getWidth());
                    existingTire.setAspectRatio(importTire.getAspectRatio());
                    existingTire.setRimSize(importTire.getRimSize());
                    existingTire.setCondition(importTire.getCondition());
                    existingTire.setQuantity(existingTire.getQuantity() + importTire.getQuantity());

                    if (importTire.getModel() != null && !importTire.getModel().isBlank()) {
                        existingTire.setModel(importTire.getModel());
                    }

                    if (importTire.getSeason() != null && !importTire.getSeason().isBlank()) {
                        existingTire.setSeason(importTire.getSeason());
                    }

                    if (importTire.getPrice().compareTo(BigDecimal.ZERO) > 0) {
                        existingTire.setPrice(importTire.getPrice());
                    }

                    if (importTire.getLocation() != null && !importTire.getLocation().isBlank()) {
                        existingTire.setLocation(importTire.getLocation());
                    }

                    Tire savedTire = tireRepository.save(existingTire);
                    ensureBatchCodes(savedTire);
                    updatedCount += 1;
                } else {
                    Tire savedTire = tireRepository.save(importTire);
                    ensureBatchCodes(savedTire);
                    createdCount += 1;
                }
            } catch (IllegalArgumentException exception) {
                skippedCount += 1;
                errors.add("Row " + rowNumber + ": " + exception.getMessage());
            }
        }

        auditLogService.record(
                "INVENTORY_IMPORTED",
                "Tire",
                null,
                "Imported CSV inventory: " + createdCount + " created, " + updatedCount + " refilled, " + skippedCount + " skipped",
                getCurrentUsername());

        return new TireImportResponse(rows.size() - 1, createdCount, updatedCount, skippedCount, errors);
    }

    public void deleteTire(Long id) {
        Tire tire = getTireById(id);
        tireRepository.deleteById(id);
        auditLogService.record("INVENTORY_DELETED", "Tire", id, "Deleted tire " + tire.getBrand() + " " + tire.getTireSize(), getCurrentUsername());
    }

    private void ensureBatchCodes(Tire tire) {
        if (tire.getId() == null || hasBatchCodes(tire)) {
            return;
        }

        String paddedId = String.format("%06d", tire.getId());
        tire.setBatchCode("BATCH-" + paddedId);
        tire.setBarcode("TT-BATCH-" + paddedId);
        tireRepository.save(tire);
    }

    private java.util.Optional<Tire> findExistingBatchByGeneratedCode(String code) {
        String normalizedCode = code.toUpperCase();
        String prefix = normalizedCode.startsWith("TT-BATCH-")
                ? "TT-BATCH-"
                : normalizedCode.startsWith("BATCH-")
                        ? "BATCH-"
                        : normalizedCode.startsWith("TTBATCH") ? "TTBATCH" : "";

        if (prefix.isBlank()) {
            return java.util.Optional.empty();
        }

        try {
            Long id = Long.valueOf(normalizedCode.substring(prefix.length()));
            return tireRepository.findById(id).map(tire -> {
                ensureBatchCodes(tire);
                return tire;
            });
        } catch (NumberFormatException exception) {
            return java.util.Optional.empty();
        }
    }

    private boolean hasBatchCodes(Tire tire) {
        return tire.getBarcode() != null && !tire.getBarcode().isBlank()
                && tire.getBatchCode() != null && !tire.getBatchCode().isBlank();
    }

    private List<List<String>> parseCsv(MultipartFile file) {
        try {
            String content = new String(file.getBytes(), StandardCharsets.UTF_8);
            List<List<String>> rows = new ArrayList<>();
            List<String> row = new ArrayList<>();
            StringBuilder cell = new StringBuilder();
            boolean quoted = false;

            for (int index = 0; index < content.length(); index += 1) {
                char character = content.charAt(index);

                if (quoted) {
                    if (character == '"') {
                        if (index + 1 < content.length() && content.charAt(index + 1) == '"') {
                            cell.append('"');
                            index += 1;
                        } else {
                            quoted = false;
                        }
                    } else {
                        cell.append(character);
                    }
                } else if (character == '"') {
                    quoted = true;
                } else if (character == ',') {
                    row.add(cell.toString());
                    cell.setLength(0);
                } else if (character == '\n') {
                    row.add(cell.toString());
                    rows.add(row);
                    row = new ArrayList<>();
                    cell.setLength(0);
                } else if (character != '\r') {
                    cell.append(character);
                }
            }

            row.add(cell.toString());

            if (!isBlankRow(row)) {
                rows.add(row);
            }

            return rows;
        } catch (IOException exception) {
            throw new IllegalArgumentException("CSV file could not be read");
        }
    }

    private Map<String, Integer> headersByName(List<String> headerRow) {
        Map<String, Integer> headers = new LinkedHashMap<>();

        for (int index = 0; index < headerRow.size(); index += 1) {
            headers.put(normalizeHeader(headerRow.get(index)), index);
        }

        return headers;
    }

    private Tire tireFromCsvRow(Map<String, Integer> headers, List<String> row, int rowNumber) {
        String description = firstOptionalText(headers, row,
                "description",
                "productdescription",
                "itemdescription",
                "product",
                "productname",
                "name");
        Tire tire = new Tire();
        TireSize size = parseTireSize(headers, row, rowNumber, description);
        String brand = firstOptionalText(headers, row,
                "brand",
                "manufacturer",
                "make",
                "mfg",
                "tirebrand");

        if (brand.isBlank()) {
            brand = inferBrand(description, size);
        }

        if (brand.isBlank()) {
            throw new IllegalArgumentException("Brand is required");
        }

        tire.setBrand(brand);
        tire.setModel(firstOptionalText(headers, row,
                "model",
                "pattern",
                "tread",
                "productline",
                "line",
                "style"));
        tire.setSeason(firstOptionalText(headers, row,
                "season",
                "category",
                "application",
                "weather"));
        tire.setCondition(parseCondition(firstOptionalText(headers, row,
                "condition",
                "newused",
                "type",
                "inventorytype",
                "state",
                "status"), description));
        tire.setQuantity(parseRequiredInt(headers, row, rowNumber, "Quantity",
                "quantity",
                "qty",
                "onhand",
                "stock",
                "available",
                "availableqty",
                "inventory",
                "count"));
        tire.setPrice(parseOptionalMoney(headers, row, "Price",
                "price",
                "unitprice",
                "cost",
                "unitcost",
                "wholesale",
                "retail",
                "sellprice",
                "sellingprice"));
        tire.setLocation(firstOptionalText(headers, row,
                "location",
                "bin",
                "rack",
                "warehouse",
                "store",
                "branch"));

        tire.setWidth(size.width());
        tire.setAspectRatio(size.aspectRatio());
        tire.setRimSize(size.rimSize());

        if (tire.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        validateImportedTire(tire);
        return tire;
    }

    private Optional<Tire> findCsvImportMatch(Map<String, Integer> headers, List<String> row, Tire importTire) {
        String barcode = firstOptionalText(headers, row,
                "barcode",
                "bar code",
                "upc",
                "tiretrackbarcode");
        String batchCode = firstOptionalText(headers, row,
                "batchcode",
                "batch",
                "batchid",
                "tiretrackbatch");

        if (!barcode.isBlank()) {
            Optional<Tire> barcodeMatch = tireRepository.findByBarcodeIgnoreCase(barcode)
                    .or(() -> findExistingBatchByGeneratedCode(barcode));

            if (barcodeMatch.isPresent()) {
                return barcodeMatch;
            }
        }

        if (!batchCode.isBlank()) {
            Optional<Tire> batchMatch = tireRepository.findByBatchCodeIgnoreCase(batchCode)
                    .or(() -> findExistingBatchByGeneratedCode(batchCode));

            if (batchMatch.isPresent()) {
                return batchMatch;
            }
        }

        return tireRepository
                .findByWidthAndAspectRatioAndRimSize(
                        importTire.getWidth(),
                        importTire.getAspectRatio(),
                        importTire.getRimSize())
                .stream()
                .filter(tire -> sameText(tire.getBrand(), importTire.getBrand()))
                .filter(tire -> tire.getCondition() == importTire.getCondition())
                .filter(tire -> importTire.getModel().isBlank() || sameText(tire.getModel(), importTire.getModel()))
                .filter(tire -> importTire.getSeason().isBlank() || sameText(tire.getSeason(), importTire.getSeason()))
                .findFirst();
    }

    private TireSize parseTireSize(Map<String, Integer> headers, List<String> row, int rowNumber, String description) {
        String size = firstOptionalText(headers, row,
                "size",
                "tiresize",
                "itemsize",
                "productsize",
                "dimensions");

        if (!size.isBlank()) {
            return parseSizeText(size);
        }

        TireSize descriptionSize = parseOptionalSizeText(description);

        if (descriptionSize != null) {
            return descriptionSize;
        }

        return new TireSize(
                parseRequiredInt(headers, row, rowNumber, "Width",
                        "width",
                        "sectionwidth",
                        "section",
                        "treadwidth"),
                parseRequiredInt(headers, row, rowNumber, "Aspect Ratio",
                        "aspectratio",
                        "aspect",
                        "ratio",
                        "profile"),
                parseRequiredInt(headers, row, rowNumber, "Rim Size",
                        "rimsize",
                        "rim",
                        "diameter",
                        "wheeldiameter",
                        "wheel"));
    }

    private int parseRequiredInt(
            Map<String, Integer> headers,
            List<String> row,
            int rowNumber,
            String label,
            String... keys) {
        String value = requiredText(headers, row, rowNumber, label, keys).replace(",", "");

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a whole number");
        }
    }

    private BigDecimal parseOptionalMoney(Map<String, Integer> headers, List<String> row, String label, String... keys) {
        String value = firstOptionalText(headers, row, keys).replace("$", "").replace(",", "");

        if (value.isBlank()) {
            return BigDecimal.ZERO;
        }

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(label + " must be a valid number");
        }
    }

    private Condition parseCondition(String value, String description) {
        String normalizedValue = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        String normalizedDescription = description == null ? "" : description.toUpperCase(Locale.ROOT);

        if (normalizedValue.isBlank()) {
            if (normalizedDescription.contains("USED") || normalizedDescription.contains("TAKEOFF") || normalizedDescription.contains("TAKE OFF")) {
                return Condition.USED;
            }

            return Condition.NEW;
        }

        if (normalizedValue.equals("N") || normalizedValue.equals("NEW TIRE")) {
            return Condition.NEW;
        }

        if (normalizedValue.equals("U") || normalizedValue.equals("USED TIRE") || normalizedValue.equals("TAKEOFF") || normalizedValue.equals("TAKE OFF")) {
            return Condition.USED;
        }

        try {
            return Condition.valueOf(normalizedValue);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Condition must be NEW or USED");
        }
    }

    private void validateImportedTire(Tire tire) {
        if (tire.getWidth() <= 0 || tire.getWidth() % 5 != 0) {
            throw new IllegalArgumentException("Width must be positive and in increments of 5");
        }

        if (tire.getAspectRatio() <= 0) {
            throw new IllegalArgumentException("Aspect ratio must be positive");
        }

        if (tire.getRimSize() < 13 || tire.getRimSize() > 30) {
            throw new IllegalArgumentException("Rim size must be between 13 and 30");
        }

        if (tire.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
    }

    private String requiredText(
            Map<String, Integer> headers,
            List<String> row,
            int rowNumber,
            String label,
            String... keys) {
        String value = firstOptionalText(headers, row, keys);

        if (value.isBlank()) {
            throw new IllegalArgumentException(label + " is required");
        }

        return value;
    }

    private String firstOptionalText(Map<String, Integer> headers, List<String> row, String... keys) {
        for (String key : keys) {
            String value = optionalText(headers, row, key);

            if (!value.isBlank()) {
                return value;
            }
        }

        return "";
    }

    private String optionalText(Map<String, Integer> headers, List<String> row, String key) {
        Integer index = headers.get(normalizeHeader(key));

        if (index == null || index >= row.size()) {
            return "";
        }

        return row.get(index) == null ? "" : row.get(index).trim();
    }

    private TireSize parseSizeText(String value) {
        TireSize size = parseOptionalSizeText(value);

        if (size == null) {
            throw new IllegalArgumentException("Size must look like 245/35R20");
        }

        return size;
    }

    private TireSize parseOptionalSizeText(String value) {
        String normalizedSize = String.valueOf(value == null ? "" : value)
                .toUpperCase(Locale.ROOT)
                .replace(" ", "");

        if (normalizedSize.isBlank()) {
            return null;
        }

        java.util.regex.Matcher compactMatcher = java.util.regex.Pattern
                .compile(".*?(\\d{3})(\\d{2})(\\d{2}).*")
                .matcher(normalizedSize);

        if (compactMatcher.matches()) {
            return new TireSize(
                    Integer.parseInt(compactMatcher.group(1)),
                    Integer.parseInt(compactMatcher.group(2)),
                    Integer.parseInt(compactMatcher.group(3)));
        }

        java.util.regex.Matcher separatedMatcher = java.util.regex.Pattern
                .compile(".*?(\\d{3})\\D+(\\d{2})\\D*(?:ZR|R)?\\D*(\\d{2}).*")
                .matcher(normalizedSize);

        if (separatedMatcher.matches()) {
            return new TireSize(
                    Integer.parseInt(separatedMatcher.group(1)),
                    Integer.parseInt(separatedMatcher.group(2)),
                    Integer.parseInt(separatedMatcher.group(3)));
        }

        return null;
    }

    private String inferBrand(String description, TireSize size) {
        String text = String.valueOf(description == null ? "" : description).trim();

        if (text.isBlank()) {
            return "";
        }

        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(\\d{3}\\D+\\d{2}\\D*(?:ZR|R)?\\D*\\d{2}|\\d{7})", java.util.regex.Pattern.CASE_INSENSITIVE)
                .matcher(text);

        if (!matcher.find() || matcher.start() <= 0) {
            return text.split("\\s+")[0];
        }

        String beforeSize = text.substring(0, matcher.start()).trim();
        return beforeSize.isBlank() ? "" : beforeSize.split("\\s+")[0];
    }

    private String normalizeHeader(String value) {
        return String.valueOf(value)
                .trim()
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]", "");
    }

    private boolean sameText(String first, String second) {
        return String.valueOf(first == null ? "" : first).trim().equalsIgnoreCase(String.valueOf(second == null ? "" : second).trim());
    }

    private boolean isBlankRow(List<String> row) {
        return row.stream().allMatch(value -> value == null || value.isBlank());
    }

    private record TireSize(int width, int aspectRatio, int rimSize) {}

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getName() != null
                ? authentication.getName()
                : "system";
    }
}

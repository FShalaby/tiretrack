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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.aem.tiretrack.dto.TireImportResponse;
import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.model.Shop;
import com.aem.tiretrack.model.ShopLocation;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.repository.ShopLocationRepository;
import com.aem.tiretrack.repository.ShopRepository;
import com.aem.tiretrack.repository.TireRepository;

@Service
public class TireService
{
    private final TireRepository tireRepository;
    private final ShopRepository shopRepository;
    private final ShopLocationRepository shopLocationRepository;
    private final AuditLogService auditLogService;
    private final ShopContextService shopContextService;
    private final TireRequestService tireRequestService;

    public TireService(
            TireRepository tireRepository,
            ShopRepository shopRepository,
            ShopLocationRepository shopLocationRepository,
            AuditLogService auditLogService,
            ShopContextService shopContextService,
            TireRequestService tireRequestService) {
        this.tireRepository = tireRepository;
        this.shopRepository = shopRepository;
        this.shopLocationRepository = shopLocationRepository;
        this.auditLogService = auditLogService;
        this.shopContextService = shopContextService;
        this.tireRequestService = tireRequestService;
    }

    public List<Tire> getAllTires() {
        return tireRepository.findAll().stream()
                .filter(this::canAccessTire)
                .toList();
    }

    public Tire getTireById(Long id) {
        Tire tire = tireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tire not found"));
        ensureTireAccess(tire);
        return tire;
    }

    @Transactional
    public Tire saveTire(Tire tire) {
        applyOptionalShopAssignment(tire, tire);
        Tire savedTire = tireRepository.save(tire);
        auditLogService.record("INVENTORY_CREATED", "Tire", savedTire.getId(), "Created tire " + savedTire.getBrand() + " " + savedTire.getTireSize(), getCurrentUsername());
        markMatchingTireRequestsAvailable(savedTire);
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
        applyOptionalShopAssignment(existingTire, updatedTire);

        Tire savedTire = tireRepository.save(existingTire);
        auditLogService.record("INVENTORY_UPDATED", "Tire", savedTire.getId(), "Updated tire " + savedTire.getBrand() + " " + savedTire.getTireSize(), getCurrentUsername());
        markMatchingTireRequestsAvailable(savedTire);
        return savedTire;
    }

    public List<Tire> searchByBrand(String brand) {
        return tireRepository.findByBrandContainingIgnoreCase(brand).stream()
                .filter(this::canAccessTire)
                .toList();
    }

    public List<Tire> searchBySize(int width, int aspectRatio, int rimSize) {
        return tireRepository.findByWidthAndAspectRatioAndRimSize(width, aspectRatio, rimSize).stream()
                .filter(this::canAccessTire)
                .toList();
    }

    public List<Tire> searchByCondition(Condition condition) {
        return tireRepository.findByCondition(condition).stream()
                .filter(this::canAccessTire)
                .toList();
    }

    public List<Tire> searchBySeason(String season) {
        return tireRepository.findBySeasonContainingIgnoreCase(season).stream()
                .filter(this::canAccessTire)
                .toList();
    }

    public List<Tire> searchByLocation(String location) {
        return tireRepository.findByLocationContainingIgnoreCase(location).stream()
                .filter(this::canAccessTire)
                .toList();
    }

    public List<Tire> getLowStockTires(int threshold) {
        return tireRepository.findByAvailableQuantityLessThanEqual(threshold).stream()
                .filter(this::canAccessTire)
                .toList();
    }

    public List<Tire> getTiresByShop(Long shopId) {
        return tireRepository.findByShop_Id(shopId);
    }

    public List<Tire> getTiresByLocation(Long locationId) {
        return tireRepository.findByShopLocation_Id(locationId);
    }

    public List<Tire> getTiresByShopAndLocation(Long shopId, Long locationId) {
        return tireRepository.findByShop_IdAndShopLocation_Id(shopId, locationId);
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
            applyCurrentTenantContextIfMissing(importTire);
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
                    markMatchingTireRequestsAvailable(savedTire);
                    updatedCount += 1;
                } else {
                    Tire savedTire = tireRepository.save(importTire);
                    markMatchingTireRequestsAvailable(savedTire);
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

    private void applyOptionalShopAssignment(Tire target, Tire request) {
        Long shopId = request.getShopId();
        Long locationId = request.getLocationId();

        if (shopId == null && locationId == null) {
            applyCurrentTenantContextIfMissing(target);
            return;
        }

        Shop shop = null;
        ShopLocation location = null;

        if (locationId != null) {
            location = shopLocationRepository.findById(locationId)
                    .orElseThrow(() -> new IllegalArgumentException("Shop location not found with id: " + locationId));

            if (!location.isActive()) {
                throw new IllegalArgumentException("Cannot assign inventory to an inactive location");
            }

            shop = location.getShop();
        }

        if (shopId != null) {
            Shop requestedShop = shopRepository.findById(shopId)
                    .orElseThrow(() -> new IllegalArgumentException("Shop not found with id: " + shopId));

            if (!requestedShop.isActive()) {
                throw new IllegalArgumentException("Cannot assign inventory to an inactive shop");
            }

            if (shop != null && !shop.getId().equals(requestedShop.getId())) {
                throw new IllegalArgumentException("Location does not belong to the selected shop");
            }

            shop = requestedShop;
        }

        target.setShop(shop);
        if (location == null && shop != null) {
            Shop resolvedShop = shop;
            location = shopContextService.getCurrentTenantLocation()
                    .filter(currentLocation -> currentLocation.getShop() != null
                            && currentLocation.getShop().getId().equals(resolvedShop.getId()))
                    .orElse(null);
        }
        target.setShopLocation(location);
        if (shop != null) {
            shopContextService.requireShopAccess(shop.getId());
        }
    }

    private void markMatchingTireRequestsAvailable(Tire tire) {
        int updatedRequests = tireRequestService.markMatchingRequestsAvailableForTire(tire);
        if (updatedRequests > 0) {
            auditLogService.record(
                    "TIRE_REQUESTS_AUTO_AVAILABLE",
                    "Tire",
                    tire.getId(),
                    updatedRequests + " tire request(s) marked available after inventory update",
                    getCurrentUsername());
        }
    }

    private void applyCurrentTenantContextIfMissing(Tire target) {
        if (target.getShop() != null) {
            if (target.getShopLocation() == null) {
                shopContextService.getCurrentTenantLocation()
                        .filter(location -> location.getShop() != null
                                && location.getShop().getId().equals(target.getShop().getId()))
                        .ifPresent(target::setShopLocation);
            }
            return;
        }

        shopContextService.getCurrentTenantShop().ifPresent(target::setShop);
        shopContextService.getCurrentTenantLocation().ifPresent(target::setShopLocation);
    }

    private void ensureTireAccess(Tire tire) {
        if (!canAccessTire(tire)) {
            throw new AccessDeniedException("You do not have permission to access this resource.");
        }
    }

    private boolean canAccessTire(Tire tire) {
        return shopContextService.canAccessTenantResource(tire.getShop(), tire.getShopLocation());
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
        List<Tire> sizeMatches = tireRepository.findByWidthAndAspectRatioAndRimSize(
                importTire.getWidth(),
                importTire.getAspectRatio(),
                importTire.getRimSize());

        return sizeMatches
                .stream()
                .filter(this::canAccessTire)
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

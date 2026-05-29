package com.aem.tiretrack.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.aem.tiretrack.dto.TireImportResponse;
import com.aem.tiretrack.dto.TireResponse;
import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.service.TireService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/tires")
public class TireController {

    private final TireService tireService;

    public TireController(TireService tireService) {
        this.tireService = tireService;
    }

    @GetMapping
    public List<TireResponse> getAllTires() {
        return tireService.getAllTires().stream().map(TireResponse::new).toList();
    }

    @GetMapping("/barcode/{barcode}")
    public TireResponse getTireByBarcode(@PathVariable String barcode) {
        return new TireResponse(tireService.getTireByBarcode(barcode));
    }

    @GetMapping("/{id}")
    public TireResponse getTireById(@PathVariable Long id) {
        return new TireResponse(tireService.getTireById(id));
    }

    @PostMapping
    public TireResponse createTire(@Valid @RequestBody Tire tire) {
        return new TireResponse(tireService.saveTire(tire));
    }

    @PostMapping("/import")
    public TireImportResponse importTiresCsv(@RequestParam("file") MultipartFile file) {
        return tireService.importTiresCsv(file);
    }

    @PutMapping("/{id}")
    public TireResponse updateTire(
            @PathVariable Long id,
           @Valid @RequestBody Tire updatedTire) {

        return new TireResponse(tireService.updateTire(id, updatedTire));
    }

    @GetMapping("/search/brand")
    public List<TireResponse> searchByBrand(@RequestParam String brand) {
        return tireService.searchByBrand(brand).stream().map(TireResponse::new).toList();
    }

    @GetMapping("/search/size")
    public List<TireResponse> searchBySize(
            @RequestParam int width,
            @RequestParam int aspectRatio,
            @RequestParam int rimSize) {

        return tireService.searchBySize(width, aspectRatio, rimSize).stream().map(TireResponse::new).toList();
    }

    @GetMapping("/search/condition")
    public List<TireResponse> searchByCondition(@RequestParam Condition condition) {
        return tireService.searchByCondition(condition).stream().map(TireResponse::new).toList();
    }

    @GetMapping("/search/season")
    public List<TireResponse> searchBySeason(@RequestParam String season) {
        return tireService.searchBySeason(season).stream().map(TireResponse::new).toList();
    }

    @GetMapping("/search/location")
    public List<TireResponse> searchByLocation(@RequestParam String location) {
        return tireService.searchByLocation(location).stream().map(TireResponse::new).toList();
    }

    @GetMapping("/low-stock")
    public List<TireResponse> getLowStockTires(
            @RequestParam(defaultValue = "4") int threshold) {

        return tireService.getLowStockTires(threshold).stream().map(TireResponse::new).toList();
    }

    @DeleteMapping("/{id}")
    public void deleteTire(@PathVariable Long id) {
        tireService.deleteTire(id);
    }
}

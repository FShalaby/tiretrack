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
    public List<Tire> getAllTires() {
        return tireService.getAllTires();
    }

    @GetMapping("/{id}")
    public Tire getTireById(@PathVariable Long id) {
        return tireService.getTireById(id);
    }

    @PostMapping
    public Tire createTire(@Valid@RequestBody Tire tire) {
        return tireService.saveTire(tire);
    }

    @PutMapping("/{id}")
    public Tire updateTire(
            @PathVariable Long id,
           @Valid @RequestBody Tire updatedTire) {

        return tireService.updateTire(id, updatedTire);
    }

    @GetMapping("/search/brand")
    public List<Tire> searchByBrand(@RequestParam String brand) {
        return tireService.searchByBrand(brand);
    }

    @GetMapping("/search/size")
    public List<Tire> searchBySize(
            @RequestParam int width,
            @RequestParam int aspectRatio,
            @RequestParam int rimSize) {

        return tireService.searchBySize(width, aspectRatio, rimSize);
    }

    @GetMapping("/search/condition")
    public List<Tire> searchByCondition(@RequestParam Condition condition) {
        return tireService.searchByCondition(condition);
    }

    @GetMapping("/search/season")
    public List<Tire> searchBySeason(@RequestParam String season) {
        return tireService.searchBySeason(season);
    }

    @GetMapping("/search/location")
    public List<Tire> searchByLocation(@RequestParam String location) {
        return tireService.searchByLocation(location);
    }

    @GetMapping("/low-stock")
    public List<Tire> getLowStockTires(
            @RequestParam(defaultValue = "4") int threshold) {

        return tireService.getLowStockTires(threshold);
    }

    @DeleteMapping("/{id}")
    public void deleteTire(@PathVariable Long id) {
        tireService.deleteTire(id);
    }
}
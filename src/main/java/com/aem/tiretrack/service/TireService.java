package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    public Tire saveTire(Tire tire) {
        Tire savedTire = tireRepository.save(tire);
        auditLogService.record("INVENTORY_CREATED", "Tire", savedTire.getId(), "Created tire " + savedTire.getBrand() + " " + savedTire.getTireSize(), getCurrentUsername());
        return savedTire;
    }

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
        auditLogService.record("INVENTORY_UPDATED", "Tire", savedTire.getId(), "Updated tire " + savedTire.getBrand() + " " + savedTire.getTireSize(), getCurrentUsername());
        return savedTire;
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

    public void deleteTire(Long id) {
        Tire tire = getTireById(id);
        tireRepository.deleteById(id);
        auditLogService.record("INVENTORY_DELETED", "Tire", id, "Deleted tire " + tire.getBrand() + " " + tire.getTireSize(), getCurrentUsername());
    }
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() && authentication.getName() != null
                ? authentication.getName()
                : "system";
    }}

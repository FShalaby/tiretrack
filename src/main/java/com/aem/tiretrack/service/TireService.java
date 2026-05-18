package com.aem.tiretrack.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.model.Tire;
import com.aem.tiretrack.repository.TireRepository;

@Service
public class TireService
{
    private final TireRepository tireRepository;

    public TireService(TireRepository tireRepository) {
        this.tireRepository = tireRepository;
    }

    public List<Tire> getAllTires() {
        return tireRepository.findAll();
    }

    public Tire getTireById(Long id) {
        return tireRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tire not found"));
    }

    public Tire saveTire(Tire tire) {
        return tireRepository.save(tire);
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

        return tireRepository.save(existingTire);
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
        tireRepository.deleteById(id);
    }

}

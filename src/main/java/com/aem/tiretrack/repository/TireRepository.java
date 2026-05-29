package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.model.Tire;

public interface TireRepository extends JpaRepository<Tire, Long> 
{

    List<Tire> findByBrandContainingIgnoreCase(String brand);

    List<Tire> findByWidthAndAspectRatioAndRimSize(
            int width,
            int aspectRatio,
            int rimSize
    );

    List<Tire> findByCondition(Condition condition);

    List<Tire> findBySeasonContainingIgnoreCase(String season);

    List<Tire> findByLocationContainingIgnoreCase(String location);

    Optional<Tire> findByBarcode(String barcode);

    Optional<Tire> findByBarcodeIgnoreCase(String barcode);

    Optional<Tire> findByBatchCodeIgnoreCase(String batchCode);

    @Query("SELECT t FROM Tire t WHERE (t.quantity - COALESCE(t.reservedQuantity, 0)) <= :quantity")
    List<Tire> findByAvailableQuantityLessThanEqual(int quantity);

  @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Tire t")
  int getTotalQuantity();

  @Query("SELECT COUNT(t) FROM Tire t WHERE (t.quantity - COALESCE(t.reservedQuantity, 0)) <= :threshold")
    long countLowStockTires(int threshold);
}

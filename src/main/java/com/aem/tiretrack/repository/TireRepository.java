package com.aem.tiretrack.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.aem.tiretrack.enums.Condition;
import com.aem.tiretrack.model.Tire;

public interface TireRepository extends JpaRepository<Tire, Long> 
{

    List<Tire> findByBrandContainingIgnoreCase(String brand);

    List<Tire> findByShop_IdAndBrandContainingIgnoreCase(Long shopId, String brand);

    List<Tire> findByWidthAndAspectRatioAndRimSize(
            int width,
            int aspectRatio,
            int rimSize
    );

    List<Tire> findByShop_IdAndWidthAndAspectRatioAndRimSize(
            Long shopId,
            int width,
            int aspectRatio,
            int rimSize
    );

    List<Tire> findByCondition(Condition condition);

    List<Tire> findByShop_IdAndCondition(Long shopId, Condition condition);

    List<Tire> findBySeasonContainingIgnoreCase(String season);

    List<Tire> findByShop_IdAndSeasonContainingIgnoreCase(Long shopId, String season);

    List<Tire> findByLocationContainingIgnoreCase(String location);

    List<Tire> findByShop_IdAndLocationContainingIgnoreCase(Long shopId, String location);

    List<Tire> findByShop_Id(Long shopId);

    List<Tire> findByShopLocation_Id(Long locationId);

    List<Tire> findByShop_IdAndShopLocation_Id(Long shopId, Long locationId);

    @Query("SELECT t FROM Tire t WHERE (t.quantity - COALESCE(t.reservedQuantity, 0)) <= :quantity")
    List<Tire> findByAvailableQuantityLessThanEqual(int quantity);

    @Query("SELECT t FROM Tire t WHERE t.shop.id = :shopId AND (t.quantity - COALESCE(t.reservedQuantity, 0)) <= :quantity")
    List<Tire> findByShopIdAndAvailableQuantityLessThanEqual(@Param("shopId") Long shopId, @Param("quantity") int quantity);

  @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Tire t")
  int getTotalQuantity();

  @Query("SELECT COALESCE(SUM(t.quantity), 0) FROM Tire t WHERE t.shop.id = :shopId")
  int getTotalQuantityByShopId(@Param("shopId") Long shopId);

  @Query("SELECT COUNT(t) FROM Tire t WHERE (t.quantity - COALESCE(t.reservedQuantity, 0)) <= :threshold")
    long countLowStockTires(int threshold);

  @Query("SELECT COUNT(t) FROM Tire t WHERE t.shop.id = :shopId AND (t.quantity - COALESCE(t.reservedQuantity, 0)) <= :threshold")
    long countLowStockTiresByShopId(@Param("shopId") Long shopId, @Param("threshold") int threshold);
}

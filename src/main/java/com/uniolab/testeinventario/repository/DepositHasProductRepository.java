package com.uniolab.testeinventario.repository;

import com.uniolab.testeinventario.model.DepositHasProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface DepositHasProductRepository extends JpaRepository<DepositHasProduct, Long> {
  @Query("SELECT dhp FROM DepositHasProduct dhp WHERE dhp.deposit.id = :depositId AND dhp.product.id = :productId")
  Optional<DepositHasProduct> findByDepositIdAndProductId(Long depositId, Long productId);
}
package com.uniolab.testeinventario.repository;

import com.uniolab.testeinventario.model.Deposit;
import com.uniolab.testeinventario.model.InventoryReserveEntry;
import com.uniolab.testeinventario.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.time.LocalDateTime;

public interface InventoryReserveEntryRepository extends JpaRepository<InventoryReserveEntry, Long> {
    @Modifying
    void deleteByDateAndProductAndDepositAndQuantity(LocalDateTime localDateTime, Product product, Deposit deposit, int quantity);
}

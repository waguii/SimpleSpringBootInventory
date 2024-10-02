package com.uniolab.testeinventario.repository;

import com.uniolab.testeinventario.enums.InventoryEntryType;
import com.uniolab.testeinventario.model.Deposit;
import com.uniolab.testeinventario.model.InventoryEntry;
import com.uniolab.testeinventario.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

public interface InventoryEntryRepository extends JpaRepository<InventoryEntry, Long> {
    @Query("FROM InventoryEntry " +
            "WHERE product = :product AND deposit = :deposit " +
            "AND date >= :date AND type = :inventoryEntryType")
    Set<InventoryEntry> findFirstByProductAndDepositAndDateAfterEqualAndType(Product product,
                                                                        Deposit deposit,
                                                                        LocalDateTime date,
                                                                        InventoryEntryType inventoryEntryType);

    @Query("FROM InventoryEntry " +
            "WHERE product = :product AND deposit = :deposit " +
            "AND date > :date AND type = :inventoryEntryType")
    Set<InventoryEntry> findFirstByProductAndDepositAndDateAfterAndType(Product product,
                                                                             Deposit deposit,
                                                                             LocalDateTime date,
                                                                             InventoryEntryType inventoryEntryType);

    @Query("SELECT COUNT(ie) FROM InventoryEntry ie " +
            "WHERE ie.product = :product AND ie.deposit = :deposit " +
            "AND ie.date = :date AND ie.type = 'BALANCE'")
    int countBalancesInDate(Product product, Deposit deposit,LocalDateTime date);
}

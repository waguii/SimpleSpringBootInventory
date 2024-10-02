package com.uniolab.testeinventario.service;

import com.uniolab.testeinventario.enums.InventoryEntryType;
import com.uniolab.testeinventario.model.*;
import com.uniolab.testeinventario.repository.DepositHasProductRepository;
import com.uniolab.testeinventario.repository.InventoryEntryRepository;
import com.uniolab.testeinventario.repository.InventoryReserveEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryManager {

    private final InventoryEntryRepository inventoryEntryRepository;
    private final DepositHasProductRepository depositHasProductRepository;
    private final InventoryReserveEntryRepository inventoryReserveEntryRepository;

    public InventoryEntry add(LocalDateTime date, Product product, Deposit deposit, int quantity) {

        InventoryEntry entry = createInventoryEntry(date, product, deposit, quantity, InventoryEntryType.IN);
        InventoryEntry result = inventoryEntryRepository.save(entry);
        log.info("Inventory entry created: {}", result);

        if (!hasBalanceEntryAfter(date, product, deposit)) {
            updateDepositHasProduct(deposit, product, quantity);
        }

        return result;
    }

    public InventoryEntry remove(LocalDateTime date, Product product, Deposit deposit, int quantity) {

        InventoryEntry entry = createInventoryEntry(date, product, deposit, quantity, InventoryEntryType.OUT);
        InventoryEntry result = inventoryEntryRepository.save(entry);
        log.info("Inventory entry created: {}", result);

        if (!hasBalanceEntryAfter(date, product, deposit)) {
            updateDepositHasProduct(deposit, product, -quantity);
        }

        return result;
    }

    public InventoryEntry balance(LocalDateTime date, Product product, Deposit deposit, int quantity) {
        LocalDateTime balanceDate = date.toLocalDate().atStartOfDay();

        InventoryEntry entry = createInventoryEntry(balanceDate, product, deposit, quantity, InventoryEntryType.BALANCE);
        InventoryEntry result = inventoryEntryRepository.save(entry);

        if (!hasBalanceEntryAfterStrict(balanceDate, product, deposit)) {
            setDepositHasProduct(deposit, product, quantity);
        }

        return result;
    }


    public void transfer(LocalDateTime date, Product product, Deposit sourceDeposit, Deposit destinationDeposit, int quantity) {
        // Create OUT entry for source deposit
        InventoryEntry outEntry = createInventoryEntry(date, product, sourceDeposit, quantity, InventoryEntryType.OUT, destinationDeposit);
        InventoryEntry savedOutEntry = inventoryEntryRepository.save(outEntry);
        log.info("Inventory OUT entry created: {}", savedOutEntry);

        if (!hasBalanceEntryAfter(date, product, sourceDeposit)) {
            updateDepositHasProduct(sourceDeposit, product, -quantity);
        }

        // Create IN entry for destination deposit
        InventoryEntry inEntry = createInventoryEntry(date, product, destinationDeposit, quantity, InventoryEntryType.IN, sourceDeposit);
        InventoryEntry savedInEntry = inventoryEntryRepository.save(inEntry);
        log.info("Inventory IN entry created: {}", savedInEntry);

        if (!hasBalanceEntryAfter(date, product, destinationDeposit)) {
            updateDepositHasProduct(destinationDeposit, product, quantity);
        }
    }

    public void addReserve(LocalDateTime date, Product product, Deposit deposit, int quantity) {

        InventoryReserveEntry reserveEntry = new InventoryReserveEntry();
        reserveEntry.setDate(date);
        reserveEntry.setProduct(product);
        reserveEntry.setDeposit(deposit);
        reserveEntry.setQuantity(quantity);
        reserveEntry.setRegistrationDate(LocalDateTime.now());
        inventoryReserveEntryRepository.save(reserveEntry);

        log.info("Inventory reserve entry created: {}", reserveEntry);

        //update deposit has product
        DepositHasProduct dhp = depositHasProductRepository.findByDepositIdAndProductId(deposit.getId(), product.getId())
                .orElse(createInitialDepositHasProduct(deposit, product));

        dhp.setReserved(dhp.getReserved() + quantity);
        dhp.setAvailable(dhp.getSoh() - dhp.getReserved());

        depositHasProductRepository.save(dhp);

        log.info("DepositHasProduct updated: {}", dhp);
    }

    @Transactional
    public void removeReserve(LocalDateTime localDateTime, Product product, Deposit deposit, int quantity) {

        inventoryReserveEntryRepository.deleteByDateAndProductAndDepositAndQuantity(localDateTime, product, deposit, quantity);

        //update deposit has product
        DepositHasProduct dhp = depositHasProductRepository.findByDepositIdAndProductId(deposit.getId(), product.getId())
                .orElse(createInitialDepositHasProduct(deposit, product));

        dhp.setReserved(dhp.getReserved() - quantity);
        dhp.setAvailable(dhp.getSoh() - dhp.getReserved());

        depositHasProductRepository.save(dhp);

        log.info("DepositHasProduct updated: {}", dhp);

        log.info("Inventory reserve entry removed: {}", localDateTime);
    }

    private boolean hasBalanceEntryAfterStrict(LocalDateTime date, Product product, Deposit deposit) {
        Set<InventoryEntry> balances = inventoryEntryRepository
                .findFirstByProductAndDepositAndDateAfterAndType(product, deposit, date, InventoryEntryType.BALANCE);
        return balances != null && !balances.isEmpty();
    }

    private DepositHasProduct createInitialDepositHasProduct(Deposit deposit, Product product) {
        DepositHasProduct dhp = new DepositHasProduct();
        dhp.setDeposit(deposit);
        dhp.setProduct(product);
        dhp.setSoh(0);
        dhp.setReserved(0);
        dhp.setAvailable(0);
        dhp.setMin(-1);
        return dhp;
    }

    private InventoryEntry createInventoryEntry(LocalDateTime date, Product product,
                                                Deposit deposit, int quantity,
                                                InventoryEntryType type) {
        InventoryEntry entry = new InventoryEntry();
        entry.setType(type);
        entry.setDate(date);
        entry.setProduct(product);
        entry.setDeposit(deposit);
        entry.setQuantity(quantity);
        entry.setRegistrationDate(LocalDateTime.now());
        return entry;
    }

    private InventoryEntry createInventoryEntry(LocalDateTime date, Product product,
                                                Deposit deposit, int quantity,
                                                InventoryEntryType type,
                                                Deposit transferDeposit) {
        InventoryEntry entry = new InventoryEntry();
        entry.setType(type);
        entry.setDate(date);
        entry.setProduct(product);
        entry.setDeposit(deposit);
        entry.setQuantity(quantity);
        entry.setRegistrationDate(LocalDateTime.now());
        entry.setTransferDeposit(transferDeposit);
        return entry;
    }

    private boolean hasBalanceEntryAfter(LocalDateTime date, Product product, Deposit deposit) {
        Set<InventoryEntry> balances = inventoryEntryRepository
                .findFirstByProductAndDepositAndDateAfterEqualAndType(product, deposit, date, InventoryEntryType.BALANCE);
        return balances != null && !balances.isEmpty();
    }

    private void updateDepositHasProduct(Deposit deposit, Product product, int quantity) {
        DepositHasProduct dhp = depositHasProductRepository.findByDepositIdAndProductId(deposit.getId(), product.getId())
                .orElse(createInitialDepositHasProduct(deposit, product));
        dhp.setSoh(dhp.getSoh() + quantity);
        dhp.setAvailable(dhp.getSoh() - dhp.getReserved());
        depositHasProductRepository.save(dhp);
        log.info("DepositHasProduct updated: {}", dhp);
    }

    private void setDepositHasProduct(Deposit deposit, Product product, int quantity) {
        DepositHasProduct dhp = depositHasProductRepository.findByDepositIdAndProductId(deposit.getId(), product.getId())
                .orElse(createInitialDepositHasProduct(deposit, product));
        dhp.setSoh(quantity);
        dhp.setAvailable(quantity);
        depositHasProductRepository.save(dhp);
    }


}

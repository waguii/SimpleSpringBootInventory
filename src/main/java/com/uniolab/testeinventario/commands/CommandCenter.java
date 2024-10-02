package com.uniolab.testeinventario.commands;

import com.uniolab.testeinventario.model.Deposit;
import com.uniolab.testeinventario.model.Product;
import com.uniolab.testeinventario.repository.DepositRepository;
import com.uniolab.testeinventario.repository.ProductRepository;
import com.uniolab.testeinventario.service.DepositService;
import com.uniolab.testeinventario.service.InventoryManager;
import com.uniolab.testeinventario.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@ShellComponent
@RequiredArgsConstructor
public class CommandCenter {

    private final DepositService depositService;
    private final ProductService productService;
    private final DepositRepository depositRepository;
    private final ProductRepository productRepository;
    private final InventoryManager inventoryManager;

    @ShellMethod(key = "init-bd")
    public String initializeDatabase() {

        if (productRepository.count() > 0 || depositRepository.count() > 0) {
            return "Database already initialized";
        }

        //initialize deposit
        Deposit depositOne = new Deposit();
        depositOne.setName("Teste");
        depositService.create(depositOne);

        //initialize deposit
        Deposit depositTwo = new Deposit();
        depositTwo.setName("Teste2");
        depositService.create(depositTwo);

        //initialize products
        Product productOne = new Product();
        productOne.setName("Sabão");
        productOne.setSku("123");

        Product productTwo = new Product();
        productTwo.setName("Detergente");
        productTwo.setSku("456");

        productService.create(productOne);
        productService.create(productTwo);

        return "Database initialized";
    }

    @ShellMethod(key = "add-inventory-entry")
    public void addInventoryEntry(@ShellOption String date,
                                  @ShellOption String sku,
                                  @ShellOption int quantity,
                                  @ShellOption String depositName) {
        //transforms date string into a date object
        //format dd/MM/yyyy HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        Product product = productRepository.findBySku(sku).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Deposit deposit = depositRepository.findByName(depositName).orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        inventoryManager.add(localDateTime, product, deposit, quantity);
    }

    @ShellMethod(key = "remove-inventory-entry")
    public void removeInventoryEntry(@ShellOption String date,
                                     @ShellOption String sku,
                                     @ShellOption int quantity,
                                     @ShellOption String depositName) {
        //transforms date string into a date object
        //format dd/MM/yyyy HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        Product product = productRepository.findBySku(sku).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Deposit deposit = depositRepository.findByName(depositName).orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        inventoryManager.remove(localDateTime, product, deposit, quantity);
    }

    @ShellMethod(key = "rebalance-inventory")
    public void rebalanceInventory(@ShellOption String date,
                                   @ShellOption String sku,
                                   @ShellOption int quantity,
                                   @ShellOption String depositName) {
        //transforms date string into a date object
        //format dd/MM/yyyy HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        Product product = productRepository.findBySku(sku).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Deposit deposit = depositRepository.findByName(depositName).orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        inventoryManager.balance(localDateTime, product, deposit, quantity);
    }

    @ShellMethod(key = "transfer-inventory")
    public void transferBetweenDeposits(@ShellOption String date,
                                        @ShellOption String sku,
                                        @ShellOption int quantity,
                                        @ShellOption String depositNameSource,
                                        @ShellOption String depositNameDestination) {

        //transforms date string into a date object
        //format dd/MM/yyyy HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        Product product = productRepository.findBySku(sku).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Deposit sourceDeposit = depositRepository.findByName(depositNameSource).orElseThrow(() -> new IllegalArgumentException("Source deposit not found"));
        Deposit destinationDeposit = depositRepository.findByName(depositNameDestination).orElseThrow(() -> new IllegalArgumentException("Destination deposit not found"));

        inventoryManager.transfer(localDateTime, product, sourceDeposit, destinationDeposit, quantity);

    }

    @ShellMethod(key = "add-reserve-inventory")
    public void addReserveInventory(@ShellOption String date,
                                 @ShellOption String sku,
                                 @ShellOption int quantity,
                                 @ShellOption String depositName) {
        //transforms date string into a date object
        //format dd/MM/yyyy HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        Product product = productRepository.findBySku(sku).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Deposit deposit = depositRepository.findByName(depositName).orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        inventoryManager.addReserve(localDateTime, product, deposit, quantity);
    }

    @ShellMethod(key = "remove-reserve-inventory")
    public void removeReserveInventory(@ShellOption String date,
                                 @ShellOption String sku,
                                 @ShellOption int quantity,
                                 @ShellOption String depositName) {
        //transforms date string into a date object
        //format dd/MM/yyyy HH:mm:ss
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(date, formatter);

        Product product = productRepository.findBySku(sku).orElseThrow(() -> new IllegalArgumentException("Product not found"));
        Deposit deposit = depositRepository.findByName(depositName).orElseThrow(() -> new IllegalArgumentException("Deposit not found"));

        inventoryManager.removeReserve(localDateTime, product, deposit, quantity);
    }

}

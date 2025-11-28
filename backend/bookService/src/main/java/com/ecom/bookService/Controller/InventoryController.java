package com.ecom.bookService.Controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.bookService.dto.CancelStockRequest;
import com.ecom.bookService.dto.ConfirmStockRequest;
import com.ecom.bookService.dto.CreateInvetoryExistedBookDto;
import com.ecom.bookService.dto.InventaireDto;
import com.ecom.bookService.dto.InventaireResponseDto;
import com.ecom.bookService.dto.InverntoryCreationDto;
import com.ecom.bookService.dto.ReservationResult;
import com.ecom.bookService.dto.ReserveStockRequest;
import com.ecom.bookService.dto.UpdateInventaireDto;
import com.ecom.bookService.model.BookInventory;
import com.ecom.bookService.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Slf4j
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    // TODO receive stock

    // TODO adjust stock

    // TODO get out of stock items

    @PostMapping("/reserve")
    public ResponseEntity<ReservationResult> reserveStock(@RequestBody @Valid ReserveStockRequest request) {
        ReservationResult result = inventoryService.reserveStock(request.orderId(), request.items());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/confirm")
    public ResponseEntity<Void> confirmStock(@RequestBody @Valid ConfirmStockRequest request) {
        inventoryService.confirmReservation(request.orderId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/cancel")
    public ResponseEntity<Void> cancelStock(@RequestBody @Valid CancelStockRequest request) {
        inventoryService.releaseReservation(request.orderId());
        return ResponseEntity.ok().build();
    }

    // Admin inventory management endpoints

    /**
     * GET /api/v1/inventory/admin
     *
     * Returns all inventory items
     *
     * @return A ResponseEntity containing a list of all inventory items
     */
    @GetMapping("/admin")
    public ResponseEntity<List<InventaireResponseDto>> getAllInventory() {
        List<InventaireResponseDto> inventory = inventoryService.getAllInventory();
        return ResponseEntity.ok(inventory);
    }

    /**
     * GET /api/v1/inventory/admin/{id}
     *
     * Returns an inventory item by ID
     *
     * @param id The inventory ID
     * @return A ResponseEntity containing the inventory item
     */
    @GetMapping("/admin/{id}")
    public ResponseEntity<InventaireDto> getInventoryById(@PathVariable Long id) {
        InventaireDto inventory = inventoryService.getInvById(id);
        return ResponseEntity.ok(inventory);
    }

    /**
     * GET /api/v1/inventory/admin/search
     *
     * Returns inventory items by book title
     *
     * @param title The book title to search for
     * @return A ResponseEntity containing a list of matching inventory items
     */
    @GetMapping("/admin/search")
    public ResponseEntity<List<InventaireResponseDto>> searchInventoryByTitle(@RequestParam String title) {
        List<InventaireResponseDto> inventory = inventoryService.findINvertoryByTilte(title);
        return ResponseEntity.ok(inventory);
    }

    /**
     * POST /api/v1/inventory/admin
     *
     * Creates a new inventory with a new book
     *
     * @param request The inventory creation request
     * @return A ResponseEntity containing the created inventory ID
     */
    @PostMapping("/admin")
    public ResponseEntity<Long> createInventory(@RequestBody @Valid InverntoryCreationDto request) {
        Long inventoryId = inventoryService.addInventory(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(inventoryId);
    }

    /**
     * POST /api/v1/inventory/admin/existing-book
     *
     * Creates inventory for an existing book
     *
     * @param request The inventory creation request for existing book
     * @return A ResponseEntity containing the created inventory
     */
    @PostMapping("/admin/existing-book")
    public ResponseEntity<BookInventory> createInventoryForExistingBook(@RequestBody @Valid CreateInvetoryExistedBookDto request) {
        BookInventory inventory = inventoryService.createInventoryForExistingBook(request);
        return ResponseEntity.status(org.springframework.http.HttpStatus.CREATED).body(inventory);
    }

    /**
     * PUT /api/v1/inventory/admin
     *
     * Updates an existing inventory
     *
     * @param request The inventory update request
     * @return A ResponseEntity with no content
     */
    @PutMapping("/admin")
    public ResponseEntity<Void> updateInventory(@RequestBody @Valid UpdateInventaireDto request) {
        inventoryService.updateINventaire(request);
        return ResponseEntity.ok().build();
    }

    /**
     * PUT /api/v1/inventory/admin/{bookId}/add-stock
     *
     * Adds stock to an existing inventory
     *
     * @param bookId The book ID
     * @param quantity The quantity to add
     * @return A ResponseEntity containing the updated inventory
     */
    @PutMapping("/admin/{bookId}/add-stock")
    public ResponseEntity<BookInventory> addStock(
            @PathVariable Long bookId,
            @RequestParam int quantity
    ) {
        BookInventory inventory = inventoryService.addStock(bookId, quantity);
        return ResponseEntity.ok(inventory);
    }

}
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { AdminLayoutComponent } from '../layout/layout.component';
import { InventoryService, InventoryResponseDTO } from '@/app/core/services/inventory.service';
import { catchError, finalize, of } from 'rxjs';

@Component({
    selector: 'admin-inventory',
    standalone: true,
    imports: [AdminLayoutComponent, CommonModule, FormsModule, RouterModule],
    templateUrl: './inventory.page.html'
})
export class AdminInventoryPage implements OnInit {
    private inventoryService = inject(InventoryService);

    inventoryItems: InventoryResponseDTO[] = [];
    loading = false;
    error: string | null = null;
    searchTitle = '';

    // Edit modal state
    editingItem: InventoryResponseDTO | null = null;
    editQuantity: number = 0;

    // Add stock modal state
    addingStockItem: InventoryResponseDTO | null = null;
    stockQuantityToAdd: number = 0;

    ngOnInit() {
        this.loadInventory();
    }

    loadInventory() {
        this.loading = true;
        this.error = null;

        this.inventoryService.getAllInventory()
            .pipe(
                catchError(err => {
                    this.error = 'Failed to load inventory';
                    console.error(err);
                    return of([]);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(items => {
                this.inventoryItems = items;
            });
    }

    searchInventory() {
        if (!this.searchTitle.trim()) {
            this.loadInventory();
            return;
        }

        this.loading = true;
        this.error = null;

        this.inventoryService.searchInventoryByTitle(this.searchTitle)
            .pipe(
                catchError(err => {
                    this.error = 'Failed to search inventory';
                    console.error(err);
                    return of([]);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(items => {
                this.inventoryItems = items;
            });
    }

    getAvailableStock(item: InventoryResponseDTO): number {
        return item.availableQuantity - item.reservedQuantity;
    }

    isLowStock(item: InventoryResponseDTO): boolean {
        return this.getAvailableStock(item) <= item.minimumStockLevel;
    }

    openEditModal(item: InventoryResponseDTO) {
        this.editingItem = item;
        this.editQuantity = item.availableQuantity - item.reservedQuantity;
    }

    closeEditModal() {
        this.editingItem = null;
    }

    saveInventoryChanges() {
        if (!this.editingItem) return;

        this.loading = true;
        this.error = null;

        this.inventoryService.updateQuantity(this.editingItem.bookid, { quantity: this.editQuantity })
            .pipe(
                catchError(err => {
                    this.error = 'Failed to update inventory';
                    console.error(err);
                    return of(null);
                }),
                finalize(() => {
                    this.loading = false;
                    this.closeEditModal();
                })
            )
            .subscribe(() => {
                this.loadInventory();
            });
    }

    openAddStockModal(item: InventoryResponseDTO) {
        this.addingStockItem = item;
        this.stockQuantityToAdd = 0;
    }

    closeAddStockModal() {
        this.addingStockItem = null;
        this.stockQuantityToAdd = 0;
    }

    addStock() {
        if (!this.addingStockItem || this.stockQuantityToAdd <= 0) return;

        this.loading = true;
        this.error = null;

        this.inventoryService.addStock(this.addingStockItem.bookid, this.stockQuantityToAdd)
            .pipe(
                catchError(err => {
                    this.error = 'Failed to add stock';
                    console.error(err);
                    return of(null);
                }),
                finalize(() => {
                    this.loading = false;
                    this.closeAddStockModal();
                })
            )
            .subscribe(() => {
                this.loadInventory();
            });
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString();
    }
}


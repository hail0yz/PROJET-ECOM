import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AdminLayoutComponent } from '../layout/layout.component';
import { InventoryService, InventoryResponseDTO } from '@/app/core/services/inventory.service';
import { catchError, finalize, of } from 'rxjs';
import { Page } from '@/app/core/models/page.model';

@Component({
    selector: 'admin-inventory',
    standalone: true,
    imports: [AdminLayoutComponent, CommonModule, FormsModule, RouterModule],
    templateUrl: './inventory.page.html'
})
export class AdminInventoryPage implements OnInit {
    private inventoryService = inject(InventoryService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);

    pagedInventories = signal<Page<InventoryResponseDTO> | null>(null);
    loading = false;
    error: string | null = null;
    searchTitle = '';

    // Edit modal state
    editingItem: InventoryResponseDTO | null = null;
    editQuantity: number = 0;

    // Add stock modal state
    addingStockItem: InventoryResponseDTO | null = null;
    stockQuantityToAdd: number = 0;

    pages = computed(() => {
        if (!this.pagedInventories()) return [];
        const totalPages = this.pagedInventories()!.totalPages;
        const current = this.pagedInventories()!.number; // 0-based index

        const start = Math.max(0, current - 2);
        const end = Math.min(totalPages - 1, current + 2);

        return Array.from({ length: end - start + 1 }, (_, i) => start + i + 1);
    });

    ngOnInit() {
        this.loadInventory();
    }

    loadInventory() {
        this.loading = true;
        this.error = null;

        this.route.queryParams.subscribe(params => {
            const page = (+params['page'] || 1) - 1;
            const size = +params['size'] || 9;
            this.inventoryService.getAllInventory(this.searchTitle, page, size)
                .pipe(
                    catchError(err => {
                        this.error = 'Failed to load inventory';
                        console.error(err);
                        return of(null);
                    }),
                    finalize(() => this.loading = false)
                )
                .subscribe(page => {
                    this.pagedInventories.set(page);
                });
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

    goToPage(page: number) {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { page, search: this.searchTitle }
        });
    }

    filter() {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { page: (this.pagedInventories()?.number || 0), search: this.searchTitle }
        });
    }

}


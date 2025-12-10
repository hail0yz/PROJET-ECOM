import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { AdminLayoutComponent } from '../layout/layout.component';
import { OrderService } from '@/app/core/services/order.service';
import { catchError, finalize, of } from 'rxjs';
import { OrderResponse } from '@/app/core/models/order.model';
import { Page } from '@/app/core/models/page.model';

@Component({
    selector: 'admin-orders',
    imports: [AdminLayoutComponent, CommonModule, RouterModule],
    templateUrl: './orders.page.html'
})
export class AdminOrdersPage implements OnInit {
    pagedOrders = signal<Page<OrderResponse> | null>(null);
    loading = false;
    error: string | null = null;

    pages = computed(() => {
        if (!this.pagedOrders()) return [];
        const totalPages = this.pagedOrders()!.totalPages;
        const current = this.pagedOrders()!.number; // 0-based index

        const start = Math.max(0, current - 2);
        const end = Math.min(totalPages - 1, current + 2);

        return Array.from({ length: end - start + 1 }, (_, i) => start + i + 1);
    });

    constructor(
        private router: Router,
        private route: ActivatedRoute,
        private orderService: OrderService
    ) { }

    ngOnInit() {
        this.route.queryParams.subscribe(params => {
            const size = +params['size'] || 10;
            const page = +params['page'] || 1;
            this.loadOrders(page, size);
        });
    }

    loadOrders(page: number, size: number) {
        this.loading = true;
        this.error = null;

        this.orderService.getAllOrders(page - 1, size)
            .pipe(
                catchError(err => {
                    this.error = 'Failed to load orders';
                    console.error(err);
                    return of();
                }),
                finalize(() => this.loading = false)
            )
            .subscribe((orders: Page<OrderResponse>) => {
                this.pagedOrders.set(orders);
            });
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString();
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('fr-FR', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    }

    goToPage(page: number) {
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { page }
        });
    }

}


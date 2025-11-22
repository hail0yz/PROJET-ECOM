import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminLayoutComponent } from '../layout/layout.component';
import { OrderService } from '@/app/core/services/order.service';
import { catchError, finalize, of } from 'rxjs';
import { OrderResponse } from '@/app/core/models/order.model';

@Component({
    selector: 'admin-orders',
    imports: [AdminLayoutComponent, CommonModule, RouterModule],
    templateUrl: './orders.page.html'
})
export class AdminOrdersPage implements OnInit {
    private orderService = inject(OrderService);

    orders: OrderResponse[] = [];
    loading = false;
    error: string | null = null;

    ngOnInit() {
        this.loadOrders();
    }

    loadOrders() {
        this.loading = true;
        this.error = null;

        this.orderService.getAllOrders()
            .pipe(
                catchError(err => {
                    this.error = 'Failed to load orders';
                    console.error(err);
                    return of([]);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(orders => {
                this.orders = orders;
            });
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString();
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    }
}


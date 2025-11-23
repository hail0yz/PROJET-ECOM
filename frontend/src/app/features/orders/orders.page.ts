import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import Keycloak from 'keycloak-js';
import { OrderService } from '@/app/core/services/order.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { catchError, finalize, of } from 'rxjs';
import { OrderResponse, OrderStatus } from '@/app/core/models/order.model';

@Component({
    selector: 'app-orders',
    imports: [CommonModule, RouterModule, NavbarComponent, FooterComponent],
    templateUrl: './orders.page.html'
})
export class OrdersPage implements OnInit {
    private keycloak = inject(Keycloak);
    private orderService = inject(OrderService);
    private router = inject(Router);

    orders: OrderResponse[] = [];
    loading = false;
    error: string | null = null;
    customerId: string | null = null;

    ngOnInit() {
        if (!this.keycloak.authenticated) {
            this.router.navigate(['/']);
            return;
        }

        if (this.keycloak.tokenParsed) {
            this.customerId = (this.keycloak.tokenParsed as any).sub || null;
            if (this.customerId) {
                this.loadOrders();
            }
        }
    }

    loadOrders() {
        this.loading = true;
        this.error = null;

        this.orderService.getMyOrders()
            .pipe(
                catchError(err => {
                    this.error = 'Failed to load orders';
                    console.error(err);
                    return of({ content: [], totalElements: 0, totalPages: 0 });
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(page => {
                this.orders = page.content;
            });
    }

    viewOrderDetails(orderId: string) {
        this.router.navigate(['/orders', orderId]);
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString();
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'EUR'
        }).format(amount);
    }

    getStatusClass(status?: string): string {
        if (!status) return 'bg-gray-100 text-gray-800';

        const statusUpper = status.toUpperCase();
        switch (statusUpper) {
            case OrderStatus.PENDING:
            case OrderStatus.PAYMENT_PENDING:
                return 'bg-yellow-100 text-yellow-800';
            case OrderStatus.FAILED:
            case OrderStatus.PAYMENT_FAILED:
                return 'bg-red-100 text-red-800';
            case OrderStatus.CANCELLED:
                return 'bg-gray-100 text-gray-800';
            case OrderStatus.COMPLETED:
                return 'bg-green-100 text-green-800';
            case OrderStatus.PROCESSING:
                return 'bg-blue-100 text-blue-800';
            default:
                return 'bg-gray-100 text-gray-800';
        }
    }

    getStatusLabel(status?: string): string {
        if (!status) return 'Unknown';
        return status.replace(/_/g, ' ').toLowerCase()
            .replace(/\b\w/g, l => l.toUpperCase());
    }

}


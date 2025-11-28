import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, ActivatedRoute, Router } from '@angular/router';
import Keycloak from 'keycloak-js';
import { catchError, finalize, of } from 'rxjs';
import { map, switchMap } from 'rxjs/operators';

import { OrderService } from '@/app/core/services/order.service';
import { CartService } from '@/app/core/services/cart.service';
import { BooksService } from '@/app/core/services/books.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';

import { OrderResponse, OrderStatus } from '@/app/core/models/order.model';
import { Book } from '@/app/core/models/book.model';

interface OrderItem {
    book: Book;
    quantity: number;
    price: number;
}

@Component({
    selector: 'app-order-details',
    standalone: true,
    imports: [
        CommonModule,
        RouterModule,
        NavbarComponent,
        FooterComponent
    ],
    templateUrl: './order-details.page.html'
})
export class OrderDetailsPage implements OnInit {
    private keycloak = inject(Keycloak);
    private orderService = inject(OrderService);
    private cartService = inject(CartService);
    private booksService = inject(BooksService);
    private errorHandler = inject(ErrorHandlerService);
    private route = inject(ActivatedRoute);
    private router = inject(Router);

    order = signal<OrderResponse | null>(null);
    orderItems = signal<OrderItem[]>([]);
    loading = signal(true);
    error = signal<string | null>(null);
    orderId: string | null = null;

    ngOnInit() {
        if (!this.keycloak.authenticated) {
            this.router.navigate(['/']);
            return;
        }

        this.orderId = this.route.snapshot.paramMap.get('id');
        if (!this.orderId) {
            this.error.set('Order ID is required');
            this.loading.set(false);
            return;
        }

        this.loadOrderDetails();
    }

    loadOrderDetails() {
        this.loading.set(true);
        this.error.set(null);

        // Step 1: Fetch order
        this.orderService.getOrderById(this.orderId!)
            .pipe(
                catchError(err => {
                    this.error.set(this.errorHandler.getErrorMessageText(err, 'chargement des d\u00e9tails de la commande'));
                    console.error('Error fetching order:', err);
                    return of(null);
                }),
                switchMap(order => {
                    if (!order) {
                        this.loading.set(false);
                        return of([]);
                    }

                    this.order.set(order);

                    if (order.lines && order.lines.length > 0) {
                        const productIds = order.lines.map(line => line.productId);

                        // Step 3: Fetch products by IDs
                        return this.booksService.getBooksByIds(productIds).pipe(
                            catchError(err => {
                                console.error('Error fetching books:', err);
                                return of([]);
                            }),
                            map(books => {
                                const items: OrderItem[] = [];
                                order.lines!.forEach(line => {
                                    const book = books.find(b => b.id === line.productId);
                                    if (book) {
                                        items.push({
                                            book,
                                            quantity: line.quantity,
                                            price: book.price * line.quantity
                                        });
                                    }
                                });
                                return items;
                            })
                        );
                    }
                    else if (order.cartId) {
                        return this.cartService.getCartById(order.cartId).pipe(
                            catchError(err => {
                                console.error('Error fetching cart:', err);
                                return of(null);
                            }),
                            switchMap(cart => {
                                if (!cart || !cart.items || cart.items.length === 0) {
                                    return of([]);
                                }

                                const productIds = cart.items.map(item => item.productId);

                                // Step 3: Fetch products by IDs
                                return this.booksService.getBooksByIds(productIds).pipe(
                                    catchError(err => {
                                        console.error('Error fetching books:', err);
                                        return of([]);
                                    }),
                                    map(books => {
                                        const items: OrderItem[] = [];
                                        cart.items.forEach(cartItem => {
                                            const book = books.find(b => b.id === cartItem.productId);
                                            if (book) {
                                                items.push({
                                                    book,
                                                    quantity: cartItem.quantity,
                                                    price: (cartItem.price || book.price) * cartItem.quantity
                                                });
                                            }
                                        });
                                        return items;
                                    })
                                );
                            })
                        );
                    }

                    return of([]);
                }),
                finalize(() => this.loading.set(false))
            )
            .subscribe({
                next: (items: OrderItem[]) => {
                    this.orderItems.set(items);
                },
                error: (err) => {
                    console.error('Error loading order details:', err);
                    this.error.set(this.errorHandler.getErrorMessageText(err, 'chargement des d\u00e9tails de la commande'));
                    this.loading.set(false);
                }
            });
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString('en-US', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(amount);
    }

    getTotal(): number {
        return this.orderItems().reduce((sum, item) => sum + item.price, 0);
    }

    goBack() {
        this.router.navigate(['/orders']);
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


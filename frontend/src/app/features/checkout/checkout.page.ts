import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';
import { CartService } from '@/app/core/services/cart.service';
import { OrderService } from '@/app/core/services/order.service';
import { PaymentService } from '@/app/core/services/payment.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { catchError, finalize, of } from 'rxjs';

@Component({
    selector: 'app-checkout',
    imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent, FooterComponent],
    templateUrl: './checkout.page.html'
})
export class CheckoutPage implements OnInit {
    private keycloak = inject(Keycloak);
    private cartService = inject(CartService);
    private orderService = inject(OrderService);
    private paymentService = inject(PaymentService);
    private router = inject(Router);
    private fb = inject(FormBuilder);

    checkoutForm!: FormGroup;
    loading = false;
    error: string | null = null;
    cart: any = null;
    customerId: string | null = null;

    ngOnInit() {
        if (!this.keycloak.authenticated) {
            this.router.navigate(['/cart']);
            return;
        }

        if (this.keycloak.tokenParsed) {
            this.customerId = (this.keycloak.tokenParsed as any).sub || null;
        }

        this.initForm();
        this.loadCart();
    }

    initForm() {
        this.checkoutForm = this.fb.group({
            address: this.fb.group({
                street: ['', [Validators.required]],
                city: ['', [Validators.required]],
                postalCode: ['', [Validators.required]],
                country: ['', [Validators.required]]
            }),
            paymentDetails: this.fb.group({
                paymentMethod: ['CREDIT_CARD', [Validators.required]]
            })
        });
    }

    loadCart() {
        this.cartService.getCartAsObservable().subscribe(cart => {
            this.cart = cart;
            if (!cart || cart.items.length === 0) {
                this.router.navigate(['/cart']);
            }
        });
    }

    getTotal(): number {
        if (!this.cart) return 0;
        return this.cart.items.reduce((sum: number, item: any) => 
            sum + (item.book.price * item.quantity), 0);
    }

    placeOrder() {
        if (!this.checkoutForm.valid || !this.cart || this.cart.id === 0) {
            this.error = 'Please fill in all required fields and ensure your cart is not empty';
            return;
        }

        this.loading = true;
        this.error = null;

        const orderRequest = {
            cartId: this.cart.id,
            address: this.checkoutForm.value.address,
            paymentDetails: this.checkoutForm.value.paymentDetails
        };

        this.orderService.placeOrder(orderRequest)
            .pipe(
                catchError(err => {
                    this.error = err.error?.message || 'Failed to place order. Please try again.';
                    console.error('Order placement error:', err);
                    return of(null);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(response => {
                if (response) {
                    // Clear cart after successful order
                    this.cartService.clearCart();
                    this.router.navigate(['/orders'], {
                        queryParams: { success: true, orderId: response.orderId }
                    });
                }
            });
    }

    get isAuthenticated(): boolean {
        return this.keycloak.authenticated ?? false;
    }
}


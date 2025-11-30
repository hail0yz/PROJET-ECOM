import { Component, OnInit, signal } from '@angular/core';
import { Router, RouterModule } from '@angular/router';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ToastService } from 'ngx-toastr-notifier';
import { loadStripe, Stripe, StripeElements, StripeCardElement } from '@stripe/stripe-js';

import { environment } from '@/app/environment';
import { CartService } from '@/app/core/services/cart.service';
import { OrderService } from '@/app/core/services/order.service';
import { PaymentService } from '@/app/core/services/payment.service';
import { Cart } from '@/app/core/models/cart.model';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';

@Component({
  selector: 'app-checkout-page',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    CurrencyPipe,
    NavbarComponent,
    FooterComponent,
  ],
  templateUrl: './checkout.page.html'
})
export class CheckoutPage implements OnInit {
  cart = signal<Cart | undefined>(undefined);
  checkoutForm!: FormGroup;
  loading = signal(false);
  error = signal<string | null>(null);
  paymentMethods = ['VISA', 'MASTERCARD', 'PAYPAL', 'CASH_ON_DELIVERY'];

  // Stripe-related
  private stripe: Stripe | null = null;
  private elements: StripeElements | null = null;
  private card: StripeCardElement | null = null;
  private clientSecret: string | null = null;
  private orderId: string | null = null;
  private paymentId: number | null = null;
  awaitingPayment = signal(false);

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private paymentService: PaymentService,
    private router: Router,
    private fb: FormBuilder,
    private toastr: ToastService
  ) {
    this.initForm();
  }

  ngOnInit(): void {
    this.cartService.getCartAsObservable().subscribe({
      next: (cart) => {
        this.cart.set(cart);
        if (!cart || cart.items.length === 0) {
          this.router.navigate(['/cart']);
        }
      }
    });
  }

  initForm(): void {
    this.checkoutForm = this.fb.group({
      address: this.fb.group({
        street: ['', [Validators.required, Validators.minLength(3)]],
        city: ['', [Validators.required, Validators.minLength(2)]],
        postalCode: ['', [Validators.required, Validators.pattern(/^\d{5}$/)]],
        country: ['', [Validators.required, Validators.minLength(2)]]
      }),
      paymentDetails: this.fb.group({
        paymentMethod: ['CREDIT_CARD', [Validators.required]]
      })
    });
  }

  getTotal(): number {
    return this.cartService.getTotal();
  }

  placeOrder(): void {
    console.log('Attempting to place order with form data:', this.checkoutForm.value);
    if (this.checkoutForm.invalid) {
      this.checkoutForm.markAllAsTouched();
      this.toastr.error('Please fill in all required fields correctly');
      return;
    }

    const cart = this.cart();
    if (!cart || cart.items.length === 0) {
      this.toastr.error('Your cart is empty');
      return;
    }

    this.loading.set(true);
    this.error.set(null);

    const formValue = this.checkoutForm.value;
    const address = {
      street: formValue.address.street,
      city: formValue.address.city,
      postalCode: formValue.address.postalCode,
      country: formValue.address.country
    };

    const paymentDetails = {
      paymentMethod: formValue.paymentDetails.paymentMethod
    };

    this.orderService.placeOrder(cart.id, address, paymentDetails).subscribe({
      next: async (response) => {
        const clientSecret = response?.paymentDetails?.clientSecret;
        // store order id so we can sync payment result to backend
        this.orderId = response?.orderId ?? null;
        // store server-side payment id returned by the order API
        this.paymentId = response?.paymentDetails?.paymentId ?? null;
        if (clientSecret) {
          this.clientSecret = clientSecret;
          try {
            this.stripe = await loadStripe(environment.stripePublicKey as string);
            if (!this.stripe) throw new Error('Failed to load Stripe');
            this.elements = this.stripe.elements();
            this.card = this.elements.create('card', {
              hidePostalCode: true
            });

            setTimeout(() => {
              const el = document.getElementById('card-element');
              if (el && this.card) {
                this.card.mount('#card-element');
              }
            }, 0);

            // switch UI into awaiting payment mode (show card input + Pay button)
            this.awaitingPayment.set(true);
            this.loading.set(false);
            this.toastr.info('Enter card details to complete payment');
            return;
          } catch (err) {
            console.error('Stripe init failed', err);
            this.error.set('Payment initialization failed. Please try again.');
            this.loading.set(false);
            return;
          }
        }

        // Fallback: no client secret returned -> treat as placed
        this.loading.set(false);
        this.error.set(null);
        this.toastr.success('Order placed successfully!');
        this.cartService.clearCart();
        this.router.navigate(['/orders', response.orderId || 'success']);
      },
      error: (error) => {
        this.loading.set(false);
        console.error('Failed to place order:', error);
        this.error.set('Failed to place order. Please try again.');
        this.toastr.error('Failed to place order. Please try again.');
      }
    });
  }

  async confirmPayment(): Promise<void> {
    if (!this.clientSecret || !this.stripe || !this.card) {
      this.error.set('Payment not initialized correctly');
      return;
    }

    this.loading.set(true);
    try {
      const result = await this.stripe.confirmCardPayment(this.clientSecret, {
        payment_method: {
          card: this.card,
          billing_details: {
            // Optionally include payer details from the form
            name: (this.checkoutForm.value?.address?.street ?? 'Customer') as string
          }
        }
      });

      if (result.error) {
        console.error('Payment failed', result.error);
        this.error.set(result.error.message ?? 'Payment failed');
        this.toastr.error(result.error.message ?? 'Payment failed');
        this.loading.set(false);
        return;
      }

      if (result.paymentIntent && result.paymentIntent.status === 'succeeded') {
        this.toastr.success('Payment succeeded');
        const paymentIntentId = result.paymentIntent.id;
        const paymentStatus = result.paymentIntent.status;
        const transactionId = result.paymentIntent.id;

        if (this.paymentId != null && this.orderId != null) {
          // Prefer calling order endpoint so order state is updated by the orders service
          this.orderService.confirmPayment(this.orderId).subscribe({
            next: () => {
              this.cartService.clearCart();
              this.awaitingPayment.set(false);
              this.loading.set(false);
              this.router.navigate(['/orders', this.orderId || 'success']);
            },
            error: (err) => {
              console.error('Failed to confirm payment with orders service', err);
              this.error.set('Payment processed but confirming order failed. Contact support.');
              this.awaitingPayment.set(false);
              this.loading.set(false);
            }
          });
        } else {
          // Fallback to client-side confirm call when server paymentId is not available
          this.paymentService.confirmPayment({
            orderId: this.orderId,
            paymentIntentId,
            paymentStatus,
            transactionId
          }).subscribe({
            next: () => {
              this.cartService.clearCart();
              this.awaitingPayment.set(false);
              this.loading.set(false);
              this.router.navigate(['/orders', this.orderId || 'success']);
            },
            error: (err) => {
              console.error('Failed to sync payment with backend', err);
              this.error.set('Payment processed but syncing failed. Contact support.');
              this.awaitingPayment.set(false);
              this.loading.set(false);
            }
          });
        }
      } else {
        this.loading.set(false);
        this.error.set('Payment not completed. Please check your payment details.');
      }
    } catch (err) {
      console.error('Confirm payment error', err);
      this.error.set('Payment failed. Please try again.');
      this.loading.set(false);
    }
  }

  goBack(): void {
    this.router.navigate(['/cart']);
  }
}


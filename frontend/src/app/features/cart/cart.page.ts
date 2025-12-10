import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';
import { Router, RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { CartService } from '@/app/core/services/cart.service';
import { Cart } from '@/app/core/models/cart.model';
import { OrderService } from '@/app/core/services/order.service';

@Component({
  selector: 'app-cart-page',
  imports: [NavbarComponent, FooterComponent, CurrencyPipe, RouterModule],
  templateUrl: './cart.page.html',
})
export class CartPage implements OnInit {
  cart = signal<Cart | undefined>(undefined);
  loading = true;
  error = ""
  editableQuantities = signal<Map<number, number>>(new Map());
  editingItem = signal<number | null>(null);

  constructor(
    private cartService: CartService,
    private orderService: OrderService,
    private keycloak: Keycloak,
    private router: Router
  ) { }

  ngOnInit(): void {
    this.cartService.getCartAsObservable().subscribe({
      next: (cart) => {
        this.cart.set(cart);
        // Initialiser les quantités éditables
        const quantities = new Map<number, number>();
        cart.items.forEach(item => {
          quantities.set(item.book.id, item.quantity);
        });
        this.editableQuantities.set(quantities);
      },
      error: err => this.error = err,
      complete: () => {
        this.loading = false
        console.log('complete', this.loading)
      }
    })
  }

  getTotal() {
    return this.cartService.getTotal();
  }

  isLoading() {
    return this.cartService.loading$;
  }

  removeItem(bookId: number) {
    this.cartService.removeItem(bookId).subscribe()
  }

  clearCart() {
    if (confirm('Êtes-vous sûr de vouloir vider votre panier ?')) {
      this.cartService.clearCart();
    }
  }

  startEditing(bookId: number) {
    this.editingItem.set(bookId);
  }

  cancelEditing() {
    this.editingItem.set(null);
    // Réinitialiser les quantités aux valeurs du panier
    const quantities = new Map<number, number>();
    this.cart()?.items.forEach(item => {
      quantities.set(item.book.id, item.quantity);
    });
    this.editableQuantities.set(quantities);
  }

  updateEditableQuantity(bookId: number, newQuantity: number) {
    const quantities = new Map(this.editableQuantities());
    quantities.set(bookId, Math.max(1, newQuantity));
    this.editableQuantities.set(quantities);
  }

  incrementQuantity(bookId: number) {
    const current = this.editableQuantities().get(bookId) || 1;
    this.updateEditableQuantity(bookId, current + 1);
  }

  decrementQuantity(bookId: number) {
    const current = this.editableQuantities().get(bookId) || 1;
    if (current > 1) {
      this.updateEditableQuantity(bookId, current - 1);
    }
  }

  saveQuantity(bookId: number) {
    const newQuantity = this.editableQuantities().get(bookId);
    if (newQuantity && newQuantity > 0) {
      this.cartService.updateQuantity(bookId, newQuantity).subscribe({
        next: () => {
          this.editingItem.set(null);
        },
        error: (err) => {
          console.error('Failed to update quantity:', err);
          // Réinitialiser la quantité en cas d'erreur
          this.cancelEditing();
        }
      });
    }
  }

  placeOrder() {
    const cart = this.cart();
    if (cart != undefined) {
      const address = {
        street: 'string',
        city: 'string',
        postalCode: 'string',
        country: 'string'
      };

      this.orderService.placeOrder(cart.id, address)
        .subscribe({
          next: () => console.log('order placed'),
          error: () => console.error('failed to place order'),
          complete: () => console.log('wiw')
        })
    }
  }

  isAuthenticated() {
    return this.keycloak.authenticated;
  }

  navigateToLogin(): void {
    this.keycloak.login({
      redirectUri: window.location.origin + this.router.url
    });
  }

  navigateToRegister(): void {
    this.router.navigate(['/signup'], {
      queryParams: { returnUrl: this.router.url }
    });
  }

}

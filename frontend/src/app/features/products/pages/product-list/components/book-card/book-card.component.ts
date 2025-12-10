import { Component, EventEmitter, Input } from '@angular/core';
import { CommonModule, CurrencyPipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import Keycloak from 'keycloak-js';

import { Book } from '@/app/core/models/book.model';
import { CartService } from '@/app/core/services/cart.service';
import { ToastService } from 'ngx-toastr-notifier';
import { CartItem } from '@/app/core/models/cart.model';

@Component({
  selector: 'book-card',
  imports: [CommonModule, CurrencyPipe, RouterLink],
  templateUrl: './book-card.component.html',
})
export class BookCardComponent {
  @Input() book!: Book;
  @Input() openAuthModal!: () => void;

  constructor(
    private cartService: CartService,
    private toastr: ToastService,
    private keycloak: Keycloak
  ) { }

  isInCart(): boolean {
    return this.cartService.isItemInCart(this.book.id);
  }

  addToCart(): void {
    if (!this.keycloak.authenticated) {
      this.openAuthModal();
      return;
    }

    const currentBook = this.book;
    if (!currentBook) return;

    // Vérifier si le produit est disponible en stock
    if (currentBook.stock !== undefined && currentBook.stock === 0) {
      this.toastr.warning('Ce produit n\'est pas disponible en stock');
      return;
    }

    // Vérifier si le produit est déjà dans le panier
    if (this.isInCart()) {
      this.toastr.info('Ce produit est déjà dans votre panier');
      return;
    }

    const cartItem: CartItem = {
      book: {
        id: currentBook.id,
        title: currentBook.title,
        price: currentBook.price,
        image: currentBook.thumbnail
      },
      quantity: 1,
    };

    this.cartService.addItem(cartItem).subscribe({
      next: (cart) => {
        this.toastr.info("Book added to cart successfully");
      },
      error: (err) => {
        console.error('Failed to add item to cart:', err);
        this.toastr.error('Failed to add item to cart. Please try again.');
      }
    });
  }

}

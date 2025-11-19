import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { CartService } from '@/app/core/services/cart.service';
import { Cart } from '@/app/core/models/cart.model';

@Component({
  selector: 'app-cart-page',
  imports: [NavbarComponent, FooterComponent, CurrencyPipe],
  templateUrl: './cart.page.html',
})
export class CartPage implements OnInit {
  cart = signal<Cart | undefined>(undefined);
  loading = true;
  error = ""

  constructor(private cartService: CartService) { }

  ngOnInit(): void {
    this.cartService.getCartAsObservable().subscribe({
      next: (cart) => this.cart.set(cart),
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

}

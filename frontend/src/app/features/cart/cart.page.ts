import { Component, OnInit, signal } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { CartService } from '@/app/core/services/cart.service';
import { Cart } from '@/app/core/models/cart.model';
import { OrderService } from '@/app/core/services/order.service';

@Component({
  selector: 'app-cart-page',
  imports: [NavbarComponent, FooterComponent, CurrencyPipe],
  templateUrl: './cart.page.html',
})
export class CartPage implements OnInit {
  cart = signal<Cart | undefined>(undefined);
  loading = true;
  error = ""

  constructor(
    private cartService: CartService,
    private orderService: OrderService
  ) { }

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

  placeOrder() {
    console.log('place order')
    const cart = this.cart();
    if (cart != undefined) {
      const address = {
        street: 'string',
        city: 'string',
        postalCode: 'string',
        country: 'string'
      }
      const paymentDetails = {
        paymentMethod: "VISA"
      }
      console.log('place order 2')
      this.orderService.placeOrder(cart.id, address, paymentDetails)
        .subscribe({
          next: () => console.log('order placed'),
          error: () => console.error('failed to place order'),
          complete: () => console.log('wiw')
        })
    }
  }

}

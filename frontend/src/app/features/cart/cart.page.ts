import { Component, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { CartService } from '@/app/core/services/cart.service';
import { Observable } from 'rxjs';
import { Cart } from '@/app/core/models/cart.model';

@Component({
  selector: 'app-cart-page',
  imports: [NavbarComponent, FooterComponent],
  templateUrl: './cart.page.html',
})
export class CartPage implements OnInit {
  cart = signal<Cart | undefined>(undefined);

  constructor(private cartService: CartService) { }

  ngOnInit(): void {
    this.cartService.getCartAsObservable().subscribe({
      next: (cart) => this.cart.set(cart)
    })
  }

}

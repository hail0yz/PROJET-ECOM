import { Component } from '@angular/core';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { CartService } from '@/app/core/services/cart.service';

@Component({
  selector: 'app-cart-page',
  imports: [NavbarComponent, FooterComponent],
  templateUrl: './cart.page.html',
})
export class CartPage {

  constructor(private cartService: CartService) { }

}

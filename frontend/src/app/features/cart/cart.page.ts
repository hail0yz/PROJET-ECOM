import { Component } from '@angular/core';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';

@Component({
  selector: 'app-cart-page',
  imports: [NavbarComponent, FooterComponent],
  templateUrl: './cart.page.html',
})
export class CartPage {

}

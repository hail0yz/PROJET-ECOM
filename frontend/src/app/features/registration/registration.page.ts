import { Component } from '@angular/core';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';

@Component({
  selector: 'registration-page',
  imports: [NavbarComponent, FooterComponent],
  templateUrl: './registration.page.html',
})
export class RegistrationPage {

}

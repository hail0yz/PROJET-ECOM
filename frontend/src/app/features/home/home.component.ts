import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { CategoriesCarouselComponent } from './components/categories-carousel/categories-carousel.component';
import { BestSellersBooksComponent } from './components/bestsellers-books/bestsellers-books.component';
import { HeroBannerComponent } from './components/hero-banner/hero-banner.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';

@Component({
    selector: 'app-home',
    standalone: true,
    imports: [
        CommonModule,
        NavbarComponent,
        //HeroBannerComponent,
        BestSellersBooksComponent,
        CategoriesCarouselComponent,
        FooterComponent,
    ],
    templateUrl: './home.component.html',
})
export class HomeComponent {

}

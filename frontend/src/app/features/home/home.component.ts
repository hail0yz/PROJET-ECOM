import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

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
        RouterModule,
        NavbarComponent,
        //HeroBannerComponent,
        BestSellersBooksComponent,
        CategoriesCarouselComponent,
        FooterComponent,
    ],
    templateUrl: './home.component.html',
})
export class HomeComponent implements OnInit {
    private keycloak = inject(Keycloak);
    isLoggedIn = false;

    ngOnInit() {
        this.isLoggedIn = this.keycloak.authenticated ?? false;
    }

    register() {
        this.keycloak.register();
    }
}

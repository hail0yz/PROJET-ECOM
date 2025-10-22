import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

interface BannerSlide {
    image: string;
    title?: string;
    subtitle?: string;
    ctaText?: string;
    ctaLink?: string;
}

@Component({
    selector: 'app-hero-banner',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './hero-banner.component.html',
})
export class HeroBannerComponent {
    slides: BannerSlide[] = [
        {
            image: 'assets/banner1.jpg',
            title: 'Discover New Bestsellers',
            subtitle: 'Find your next favorite book today!',
            ctaText: 'Shop Now',
            ctaLink: '/shop',
        },
        {
            image: 'assets/banner2.jpg',
            title: 'Limited Time Offers',
            subtitle: 'Up to 50% off on selected books',
            ctaText: 'See Deals',
            ctaLink: '/promotions',
        },
        {
            image: 'assets/banner3.jpg',
            title: 'Join Our Reading Community',
            subtitle: 'Sign up for personalized recommendations',
            ctaText: 'Sign Up',
            ctaLink: '/signup',
        },
    ];
}

import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

interface Category {
    id: string;
    name: string;
    image: string;
    link?: string;
}

@Component({
    selector: 'app-categories-carousel',
    standalone: true,
    imports: [CommonModule, RouterModule],
    templateUrl: './categories-carousel.component.html',
})
export class CategoriesCarouselComponent {
    categories: Category[] = [
        { id: '1', name: 'Fiction', image: 'assets/categories/fiction.jpg', link: '/categories/fiction' },
        { id: '2', name: 'Non-Fiction', image: 'assets/categories/non-fiction.jpg', link: '/categories/non-fiction' },
        { id: '3', name: 'Science', image: 'assets/categories/science.jpg', link: '/categories/science' },
        { id: '4', name: 'Children', image: 'assets/categories/children.jpg', link: '/categories/children' },
        { id: '5', name: 'History', image: 'assets/categories/history.jpg', link: '/categories/history' },
        { id: '6', name: 'Art', image: 'assets/categories/art.jpg', link: '/categories/art' },
    ];
}

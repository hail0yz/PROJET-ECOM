import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';

import { Book } from '@/app/core/models/book.model';
import { Category } from '@/app/core/models/category.model';
import { BooksService } from '@/app/core/services/books.service';
import { CategoriesService } from '@/app/core/services/categories.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { BookCardComponent } from './components/book-card/book-card.component';

@Component({
    selector: 'product-list-page',
    standalone: true,
    imports: [NavbarComponent, FooterComponent, SidebarComponent, BookCardComponent],
    templateUrl: './products.page.html',
})
export class ProductListPage implements OnInit {
    books: Book[] = [];
    categories: Category[] = [];
    loadingBooks = true;
    loadingCategories = true;
    // TODO: Add pagination, filtering, and sorting properties

    constructor(
        private route: ActivatedRoute,
        private booksService: BooksService,
        private categoriesService: CategoriesService
    ) { }

    ngOnInit(): void {
        this.categories = this.categoriesService.getCategories();
        this.booksService.getBooks().then(books => {
            this.books = books;
            this.loadingBooks = false;
        })
    }
}

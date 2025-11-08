import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { CurrencyPipe } from '@angular/common';

import { Book } from '@/app/core/models/book.model';
import { BooksService } from '@/app/core/services/books.service';
import { Breadcrumb } from './components/breadcrumb/breadcrumb';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { LoadingComponent } from '@/app/core/components/loading/loading.component';

@Component({
    selector: 'product-page',
    standalone: true,
    imports: [CurrencyPipe, Breadcrumb, NavbarComponent, FooterComponent, LoadingComponent],
    templateUrl: './product.page.html',
})
export class ProductDetailPage implements OnInit {
    productId!: string;
    book?: Book;

    constructor(
        private route: ActivatedRoute,
        private booksService: BooksService
    ) { }

    ngOnInit(): void {
        this.productId = this.route.snapshot.paramMap.get('id')!;
        console.log(this.productId);

        this.booksService.getBookById(this.productId).subscribe({
            next: (book) => {
                this.book = book;
                console.log(this.book);
            },
            error: (error) => {
                console.error('Error fetching book details:', error);
            }
        });
    }
}

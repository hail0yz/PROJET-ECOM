import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { LucideAngularModule, HeartIcon } from 'lucide-angular';

import { BooksService } from '@/app/core/services/books.service';
import { Book } from '@/app/core/models/book.model';

@Component({
    selector: 'app-bestsellers-books',
    standalone: true,
    imports: [CommonModule, LucideAngularModule],
    templateUrl: './bestsellers-books.component.html',
})
export class BestSellersBooksComponent implements OnInit {
    readonly HeartIcon = HeartIcon;
    books: Book[] = [];

    constructor(private booksService: BooksService) { }

    ngOnInit() {
        this.books = this.booksService.getBestSellersBooks();
    }
}

import { Component, OnInit } from '@angular/core';

import { AdminLayoutComponent } from '@/app/features/admin/layout/layout.component';
import { BooksService } from '@/app/core/services/books.service';
import { Book } from '@/app/core/models/book.model';

@Component({
  selector: 'admin-product-list',
  imports: [AdminLayoutComponent],
  templateUrl: './product-list.page.html',
})
export class AdminProductListPage implements OnInit {
  books: Book[] = [];

  constructor(
    private booksService: BooksService
  ) { }

  ngOnInit(): void {
    this.booksService.getBooks().then(books => {
      this.books = books;
    })
  }

}

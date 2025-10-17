import { Component } from '@angular/core';
import {IBook} from '../../model/ibook';
import {BookService} from '../../service/book-service';

@Component({
  selector: 'app-book-list',
  imports: [],
  templateUrl: './book-list.html',
  styleUrl: './book-list.css'
})
export class BookList {
  books?: IBook[];

  constructor(private bookService: BookService) { }

  ngOnInit() {
    this.bookService.getAllBooks().subscribe({
      next: (books) => {
        console.log("Books receives :", books);
        this.books = books;
      },
      error: (err) => {
        console.log("Error :", err);
      }
    });
  }
}

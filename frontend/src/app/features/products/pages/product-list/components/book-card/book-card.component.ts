import { Component, Input } from '@angular/core';
import { CurrencyPipe } from '@angular/common';

import { Book } from '@/app/core/models/book.model';

@Component({
  selector: 'book-card',
  imports: [CurrencyPipe],
  templateUrl: './book-card.component.html',
})
export class BookCardComponent {
  @Input() book!: Book;
}

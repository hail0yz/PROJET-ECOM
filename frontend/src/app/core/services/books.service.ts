import { Book } from '@/app/core/models/book.model';
import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, map, Observable, of} from 'rxjs';

export const MOCK_BOOKS: Book[] = [
    /*{
        id: '1',
        title: 'Angular for Beginners',
        author: 'John Doe',
        description: 'A complete guide to get started with Angular framework.',
        price: 29.99,
        image: 'https://placehold.co/600x600?text=Angular+Book',
        category: 'Programming',
        rating: 4.5,
        stock: 10,
        publishedDate: '2022-05-15',
        publisher: 'Tech Books Publishing',
        isbn: '978-1-23456-789-0',
        language: 'English',
        format: 'paperback',
        tags: ['Angular', 'Web Development', 'Frontend']
    },
    {
        id: '2',
        title: 'The Alchemist',
        author: 'Paulo Coelho',
        description: 'A philosophical novel about following your dreams.',
        price: 15.0,
        image: 'https://placehold.co/600x600?text=The+Alchemist',
        category: 'Fiction',
        rating: 4.8,
        stock: 25,
        publishedDate: '1988-01-01',
        publisher: 'HarperCollins',
        isbn: '978-0-06-112241-5',
        language: 'English',
        format: 'hardcover',
        tags: ['Fiction', 'Philosophy', 'Inspirational']
    },
    {
        id: '3',
        title: 'Clean Code',
        author: 'Robert C. Martin',
        description: 'A handbook of agile software craftsmanship.',
        price: 32.5,
        image: 'https://placehold.co/600x600?text=Clean+Code',
        category: 'Programming',
        rating: 4.9,
        stock: 15,
        publishedDate: '2008-08-01',
        publisher: 'Prentice Hall',
        isbn: '978-0-13-235088-4',
        language: 'English',
        format: 'ebook',
        tags: ['Programming', 'Best Practices', 'Software Engineering']
    }*/
];

@Injectable({ providedIn: 'root' })
export class BooksService {

  private bookServiceURL = "http://localhost:8080/api/v1/books";

  constructor(private http: HttpClient) { }

  public getAllBooks(): Observable<Book[]> {

    return this.http.get<Book[]>(this.bookServiceURL);
  }

  /*
  getBestSellersBooks(): Book[] {
    return MOCK_BOOKS;
  }*/

  async getBooks(): Promise<Book[]> {
    return MOCK_BOOKS;
  }

    /*async getBookById(id: string): Promise<Book | undefined> {
        const books = await this.getBooks(); // TODO: Implement fetching book by ID
        return Promise.resolve(books.find(book => book.id === id));
    }*/
}

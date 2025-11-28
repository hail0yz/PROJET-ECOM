import { Book } from '@/app/core/models/book.model';
import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { Page } from '@core/models/page.model';
import { Router } from '@angular/router';
import { environment } from '@/app/environment';

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

  private bookServiceURL = `${environment.apiBaseUrl}/api/v1/books`;

  constructor(private http: HttpClient, private router: Router) { }

  public getAllBooks(options: { page?: number, size?: number, search?: string }): Observable<Page<Book>> {
    console.log('getAllBooks called with options:', options);
    const size = options.size ?? 10;
    const page = options.page ? options.page - 1 : 0; // Backend pages are 0-indexed

    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (options.search) {
      params = params.set('search', options.search);
    }

    console.log('Fetching books with params:', params.toString());

    return this.http.get<Page<Book>>(this.bookServiceURL, { params })
      .pipe(
        catchError((error: HttpErrorResponse) => {
          console.error('Server error:', error);
          return throwError(() => new Error(error.message || 'Server error'));
        })
      );
  }

  /*
  getBestSellersBooks(): Book[] {
    return MOCK_BOOKS;
  }*/

  async getBooks(): Promise<Book[]> {
    return MOCK_BOOKS;
  }

  public getBookById(id: number): Observable<Book> {
    return this.http.get<Book>(this.bookServiceURL + `/${id}`).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 404) {
          console.warn(`Livre avec l'identifiant ${id} introuvable, redirection vers la page 404 page.`);
          this.router.navigate(['/404']);
          return throwError(() => error);
        }
        return throwError(() => error);
      })
    );
  }

}

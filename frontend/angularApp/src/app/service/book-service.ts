import { Injectable } from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {catchError, map, Observable, of} from 'rxjs';
import {IBook} from '../model/ibook';


@Injectable({
  providedIn: 'root'
})
export class BookService {

  private urlBooks = "http://localhost:8080/api/v1/books";

  constructor(private http: HttpClient) { }

  public getAllBooks(): Observable<IBook[]> {
    return this.http.get(this.urlBooks).pipe(
      map((body: any) => {
        //console.log("books = ", body);

        return body;
      }),
      catchError(error => {
        return of([]);
      })
    )
  }
}

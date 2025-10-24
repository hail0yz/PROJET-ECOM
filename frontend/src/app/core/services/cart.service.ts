import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, tap } from 'rxjs';

import { CartItem } from '@core/models/cart-item.model';

@Injectable({
  providedIn: 'root'
})
export class CartService {

  private cartSubject = new BehaviorSubject<CartItem[]>([]);
  cart$ = this.cartSubject.asObservable();

  // TODO: change USER_ID to actual user ID when authentication is implemented
  private API_URL = '/api/cart/user/USER_ID';

  constructor(private http: HttpClient) {
    this.loadCart();
  }

  private loadCart() {
    this.http.get<CartItem[]>(this.API_URL).subscribe(items => {
      this.cartSubject.next(items);
    });
  }

  addItem(item: CartItem) {
    this.http.post<CartItem[]>(`${this.API_URL}/items`, item).pipe(
      tap(c => this.cartSubject.next(c))
    ).subscribe();
  }

  updateQuantity(id: string, quantity: number) {
    this.http.put<CartItem[]>(`${this.API_URL}/items`, { productId: id, quantity }).pipe(
      tap(c => this.cartSubject.next(c))
    ).subscribe();
  }

  removeItem(id: string) {
    this.http.delete<CartItem[]>(`${this.API_URL}/items/${id}`).pipe(
      tap(c => this.cartSubject.next(c))
    ).subscribe();
  }

  clearCart() {
    this.http.delete<CartItem[]>(`${this.API_URL}/clear`).pipe(
      tap(() => this.cartSubject.next([]))
    ).subscribe();
  }

  getTotal(): number {
    return this.cartSubject.value.reduce((sum, item) => sum + item.book.price * item.quantity, 0);
  }

  getCount(): number {
    return this.cartSubject.value.reduce((count, item) => count + item.quantity, 0);
  }

}

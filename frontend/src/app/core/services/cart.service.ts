import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, catchError, map, Observable, of, switchMap, tap, throwError } from 'rxjs';
import Keycloak from 'keycloak-js';

import { Cart, CartEntryAPI, CartItem, CreateCartRequestAPI, CreateCartResponseAPI, GetCartResponseAPI } from '@/app/core/models/cart.model';
import { environment } from '@/app/environment';

@Injectable({
  providedIn: 'root'
})
export class CartService {
  private readonly keycloak = inject(Keycloak);
  private readonly CART_LOCAL_KEY = 'cart_items';
  private API_URL = `${environment.apiBaseUrl}/api/carts`;

  private readonly cartSubject = new BehaviorSubject<Cart>({ id: 0, items: [], local: true, persisted: false });
  private readonly cartIdSubject = new BehaviorSubject<string | null>(null);
  private readonly loadingSubject = new BehaviorSubject<boolean>(false);

  public readonly cart$ = this.cartSubject.asObservable();
  public readonly cartId$ = this.cartIdSubject.asObservable();
  public readonly loading$ = this.loadingSubject.asObservable();

  constructor(private http: HttpClient) {
    this.initCart();
    this.setupAuthListener();
  }

  // ========================================
  // PUBLIC API
  // ========================================

  addItem(item: CartItem): Observable<Cart> {
    this.setLoading(true);
    return this.isAuthenticated()
      ? this.addItemBackend(item)
      : this.addItemLocal(item);
  }

  updateQuantity(bookId: number, quantity: number): Observable<Cart> {
    if (quantity < 1)
      return throwError(() => new Error('Quantity must be at least 1'));

    this.setLoading(true);

    return this.isAuthenticated()
      ? this.updateQuantityBackend(bookId, quantity)
      : this.updateQuantityLocal(bookId, quantity);
  }

  removeItem(bookId: number): Observable<Cart> {
    this.setLoading(true);
    return this.isAuthenticated()
      ? this.removeItemBackend(bookId)
      : this.removeItemLocal(bookId);
  }

  clearCart() {
    this.setLoading(true);
    this.isAuthenticated()
      ? this.clearCartBackend()
      : this.clearCartLocal();
  }

  getTotal(): number {
    return this.getCartTotal(this.cartSubject.value);
  }

  getCartTotal(cart: Cart): number {
    return cart.items
      .reduce((sum, item) => sum + item.book.price * item.quantity, 0);
  }

  getCount(): number {
    return this.cartSubject.value.items.reduce((count, item) => count + item.quantity, 0);
  }

  getCurrentCart(): Cart {
    return this.cartSubject.value;
  }

  getCurrentCartId(): string | null {
    return this.cartIdSubject.value;
  }

  isItemInCart(bookId: number): boolean {
    return this.cartSubject.value.items.some(item => item.book.id === bookId);
  }

  getItemQuantity(bookId: number): number {
    const item = this.cartSubject.value.items.find(i => i.book.id === bookId);
    return item?.quantity || 0;
  }

  getCartAsObservable() {
    return this.cart$;
  }

  getCartItems(): Observable<CartItem[]> {
    return this.cart$.pipe(map(cart => cart.items));
  }

  getCartById(cartId: number): Observable<GetCartResponseAPI> {
    return this.http.get<GetCartResponseAPI>(this.getCartByIdUrl(cartId));
  }

  refreshCart() {
    if (!this.isAuthenticated()) {
      this.loadLocalCart();
      return of(this.cartSubject.value);
    }
    return this.loadBackendCart();
  }

  // ========================================
  // PRIVATE API
  // ========================================

  private initCart(): void {
    if (this.isAuthenticated()) {
      this.loadBackendCart();
    } else {
      this.loadLocalCart();
    }
  }

  private setLoading(loading: boolean): void {
    this.loadingSubject.next(loading);
  }

  private isAuthenticated(): boolean {
    return this.keycloak.authenticated ?? false;
  }

  private getCartByIdUrl(cartId: number): string {
    return `${this.API_URL}/${cartId}`;
  }

  private loadLocalCart(): void {
    console.log('load local cart')
    try {
      const data = localStorage.getItem(this.CART_LOCAL_KEY);
      if (data) {
        const items: CartItem[] = data ? JSON.parse(data) : [];
        this.cartSubject.next({ id: 0, items, local: true });
      } else {
        this.cartSubject.next({ id: 0, items: [], local: true });
      }
    } catch (error) {
      console.error('Failed to load local cart:', error);
      this.cartSubject.next({ id: 0, items: [], local: true });
    } finally {
      this.setLoading(false);
    }
  }

  private saveLocalCart(cart: Cart): void {
    try {
      localStorage.setItem(this.CART_LOCAL_KEY, JSON.stringify(cart.items));
      this.cartSubject.next(cart);
    } catch (error) {
      console.error('Failed to save local cart:', error);
    }
  }

  private addItemLocal(item: CartItem) {
    const currentCart = this.cartSubject.value;
    const items = [...currentCart.items];
    const existingIndex = items.findIndex(i => i.book.id === item.book.id);

    if (existingIndex !== -1) {
      items[existingIndex].quantity += item.quantity;
    } else {
      items.push({ ...item });
    }

    const updatedCart: Cart = { id: 0, items, local: true };
    this.saveLocalCart(updatedCart);
    this.setLoading(false);
    return of(updatedCart);
  }

  private updateQuantityLocal(bookId: number, quantity: number) {
    const currentCart = this.cartSubject.value;
    const items = [...currentCart.items];
    const item = items.find(i => i.book.id === bookId);

    if (item) {
      item.quantity = quantity;
    }

    const updatedCart: Cart = { id: 0, items, local: true };
    this.saveLocalCart(updatedCart);
    this.setLoading(false);

    return of(updatedCart)
  }

  private removeItemLocal(bookId: number): Observable<Cart> {
    const currentCart = this.cartSubject.value;
    const items = currentCart.items.filter(i => i.book.id !== bookId);
    const updatedCart: Cart = { id: 0, items, local: true };

    this.saveLocalCart(updatedCart);
    this.setLoading(false);
    return of(updatedCart);
  }

  private clearCartLocal() {
    localStorage.removeItem(this.CART_LOCAL_KEY);
    this.cartSubject.next({ id: 0, items: [], local: true });
    this.setLoading(false);
  }

  // ========================================
  // BACKEND OPERATIONS
  // ========================================
  private loadBackendCart() {
    this.setLoading(true);

    this.http.get<GetCartResponseAPI>(`${this.API_URL}/current`)
      .pipe(
        map(response => {
          const cart: Cart = {
            id: response.id,
            items: response.items.map(item => ({
              book: {
                id: item.productId,
                price: item.price,
                title: item.title,
                image: item.image
              },
              quantity: item.quantity
            })),
            local: false,
            persisted: true
          };

          this.setLoading(false);
          return cart;
        }),
        catchError(err => {
          if (err.status === 404) {
            const emptyCart: Cart = {
              id: 0,
              items: [],
              local: false,
              persisted: false
            };
            this.cartSubject.next(emptyCart);
            this.setLoading(false);
            return of(emptyCart);
          }

          const emptyCart: Cart = {
            id: 0,
            items: [],
            local: false,
            persisted: false
          };
          this.cartSubject.next(emptyCart);
          this.setLoading(false);
          return this.handleBackendError('load cart', err)
        })
      )
      .subscribe({
        next: cart => this.cartSubject.next(cart),
      })
  }

  private addItemBackend(item: CartItem): Observable<Cart> {
    const currentCart = this.cartSubject.value;
    const currentCartId = currentCart.id;

    // Cart exists - add item to existing cart
    console.log('add item to backend cart', currentCart);
    if (currentCart.persisted) {
      const entry: CartEntryAPI = {
        productId: item.book.id,
        quantity: item.quantity
      }
      return this.http.post<void>(`${this.getCartByIdUrl(currentCartId)}/items`, entry)
        .pipe(
          map(_ => {
            const updatedItems = [...currentCart.items, item]
            const updatedCart = { ...currentCart, items: updatedItems, };
            this.setLoading(false);
            this.cartSubject.next(updatedCart)
            return updatedCart;
          }),
          catchError(err => {
            // Si le panier n'existe plus (404), créer un nouveau panier
            if (err.status === 404) {
              console.warn('Cart not found (deleted after order), creating new cart');
              return this.createCartWithItem(item);
            }
            this.setLoading(false);
            return this.handleBackendError('add item', err);
          })
        )

    }

    // Cart doesn't exist - create cart with items
    return this.createCartWithItem(item);
  }

  private createCartWithItem(item: CartItem): Observable<Cart> {
    const request: CreateCartRequestAPI = {
      items: [
        {
          price: item.book.price,
          productId: item.book.id,
          quantity: item.quantity
        }
      ]
    };

    return this.http.post<CreateCartResponseAPI>(this.API_URL, request)
      .pipe(
        map(response => {
          const cart: Cart = {
            id: response.cartId,
            items: [item],
            local: false,
            persisted: true
          }
          this.cartSubject.next(cart);
          this.setLoading(false);
          return cart;
        }),
        catchError(err => {
          if (err.status === 404) {
            const emptyCart: Cart = {
              id: 0,
              items: [],
              local: false,
              persisted: false
            };
            this.setLoading(false);
            this.cartSubject.next(emptyCart)
            return of(emptyCart);
          }

          const emptyCart: Cart = {
            id: 0,
            items: [],
            local: false,
            persisted: false
          };
          this.cartSubject.next(emptyCart)
          this.setLoading(false);
          return this.handleBackendError('create cart with item', err);
        })
      );
  }

  private updateQuantityBackend(bookId: number, quantity: number): Observable<Cart> {
    const currentCart = this.cartSubject.value;
    const currentCartId = currentCart.id;

    if (!currentCartId || currentCartId === 0) {
      this.setLoading(false);
      return throwError(() => new Error('Cart does not exist. Cannot update item.'));
    }

    this.setLoading(true);

    const entry: CartEntryAPI = {
      productId: bookId,
      quantity: quantity
    };

    return this.http.put<void>(`${this.getCartByIdUrl(currentCartId)}/items`, entry)
      .pipe(
        map(_ => {
          const updatedItems = currentCart.items.map(item =>
            item.book.id === bookId
              ? { ...item, quantity }
              : item
          );

          const updatedCart: Cart = {
            ...currentCart,
            items: updatedItems
          };

          this.setLoading(false);
          this.cartSubject.next(updatedCart);
          return updatedCart;
        }),
        catchError(err => {
          // Si le panier n'existe plus (404), réinitialiser le panier
          if (err.status === 404) {
            console.warn('Cart not found (deleted after order), resetting cart');
            const emptyCart: Cart = { id: 0, items: [], local: false, persisted: false };
            this.cartSubject.next(emptyCart);
            this.setLoading(false);
            return of(emptyCart);
          }
          this.setLoading(false);
          return this.handleBackendError('update quantity', err);
        })
      );
  }

  private removeItemBackend(bookId: number): Observable<Cart> {
    this.setLoading(true);
    const currentCart = this.cartSubject.value;
    const currentCartId = currentCart.id;

    if (!currentCartId || currentCartId === 0) {
      this.setLoading(false);
      return throwError(() => new Error('Cart does not exist. Cannot remove item.'));
    }

    return this.http.delete<void>(`${this.getCartByIdUrl(currentCartId)}/items/${bookId}`)
      .pipe(
        map(_ => {
          const updatedItems = currentCart.items.filter(entry => entry.book.id !== bookId);
          const updatedCart: Cart = { ...currentCart, items: updatedItems };
          this.setLoading(false);
          this.cartSubject.next(updatedCart)
          return updatedCart;
        }),
        catchError(err => {
          // Si le panier n'existe plus (404), réinitialiser le panier
          if (err.status === 404) {
            console.warn('Cart not found (deleted after order), resetting cart');
            const emptyCart: Cart = { id: 0, items: [], local: false, persisted: false };
            this.cartSubject.next(emptyCart);
            this.setLoading(false);
            return of(emptyCart);
          }
          this.setLoading(false);
          return this.handleBackendError('remove item', err)
        })
      );
  }

  private clearCartBackend() {
    this.setLoading(true);
    const currentCart = this.cartSubject.value;
    const currentCartId = currentCart.id;

    if (!currentCartId || currentCartId === 0) {
      this.setLoading(false);
      this.handleBackendError('Clear Cart', new Error('Cart does not exist. Cannot remove item.'))
    }

    this.http.post<void>(`${this.API_URL}/${currentCartId}/clear`, {})
      .pipe(
        tap(() => {
          const updatedCart: Cart = { ...currentCart, items: [] };
          this.setLoading(false);
          this.cartSubject.next(updatedCart);
          return updatedCart;
        }),
        catchError(err => this.handleBackendError('clear cart', err))
      )
      .subscribe({
        next: _ => console.log('Cart clear successfully'),
        error: (err) => console.error('Failed to clear Cart', err)
      });
  }

  private handleBackendError(operation: string, error: any): Observable<never> {
    console.error(`Failed to ${operation}:`, error);

    // Optionally show user-friendly error message
    // this.notificationService.showError(`Failed to ${operation}`);

    return throwError(() => new Error(`Cart operation failed: ${operation}`));
  }

  // ========================================
  // SYNC METHODS for migration
  // ========================================
  migrateLocalToBackend() {
    // const localItems = this.cartSubject.value;

    // if (localItems.length === 0) {
    //   return of([]);
    // }

    // this.http.post<CartItem[]>(`${this.API_URL}/migrate`, {
    //   items: localItems
    // }).pipe(
    //   tap(items => {
    //     this.cartSubject.next(items);
    //     localStorage.removeItem(this.CART_LOCAL_KEY);
    //   }),
    //   catchError(err => {
    //     console.error('Failed to migrate cart:', err);
    //     return of(localItems);
    //   })
    // );
  }

  // saveToLocalOnLogout(): void {
  //   const currentCart = this.cartSubject.value;
  //   if (currentCart.items.length > 0) {
  //     const localCart: Cart = {
  //       id: 0,
  //       items: currentCart.items,
  //       local: true,
  //       persisted: false
  //     };
  //     this.saveLocalCart(localCart);
  //   } else {
  //     this.cartSubject.next({
  //       id: 0,
  //       items: [],
  //       local: true,
  //       persisted: false
  //     });
  //   }
  // }

  private setupAuthListener(): void {
    console.log('set auth listener');
    this.keycloak.onAuthSuccess = () => {
      this.handleLogin();
    };

    this.keycloak.onAuthLogout = () => {
      this.handleLogout();
    };

    // this.keycloak.onTokenExpired = () => {
    //   this.keycloak.updateToken(30);
    // };
  }

  private handleLogin(): void {
    const localCart = this.cartSubject.value;

    // If user has items in local cart, migrate them
    // if (localCart.local && localCart.items.length > 0) {
    //   this.loadBackendCart().subscribe({
    //     next: () => console.log('Cart (loaded from backend | migrated) successfully'),
    //     error: (err) => console.error('Failed to (load from backend | migrate cart):', err)
    //   });
    // } else {
    //   // Just load backend cart
    //   this.loadBackendCart().subscribe({
    //     error: (err) => console.error('Failed to load backend cart:', err)
    //   });
    // }

    this.loadBackendCart();
  }

  private handleLogout(): void {
    //this.saveToLocalOnLogout();
  }

  markCartAsCompleted() {
    // empty the cart upon order completion (not persisted !)
    const emptyCart: Cart = { id: 0, items: [], local: true, persisted: false }
    this.cartSubject.next(emptyCart);
  }

}

import { Component, OnInit, signal } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { CurrencyPipe } from '@angular/common';
import { ToastService } from 'ngx-toastr-notifier';

import { Book } from '@/app/core/models/book.model';
import { BooksService } from '@/app/core/services/books.service';
import { Breadcrumb } from './components/breadcrumb/breadcrumb';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { LoadingComponent } from '@/app/core/components/loading/loading.component';
import { CartService } from '@/app/core/services/cart.service';
import { CartItem } from '@/app/core/models/cart.model';

@Component({
    selector: 'product-page',
    standalone: true,
    imports: [CurrencyPipe, Breadcrumb, NavbarComponent, FooterComponent, LoadingComponent],
    templateUrl: './product.page.html',
})
export class ProductDetailPage implements OnInit {
    bookId!: number;
    book?: Book;
    addingToCart = signal(false);
    quantity = signal(1);
    showSuccessMessage = signal(false);
    errorMessage = signal<string | null>(null);

    constructor(
        private route: ActivatedRoute,
        private router: Router,
        private booksService: BooksService,
        private cartService: CartService,
        private toastr: ToastService
    ) { }

    ngOnInit(): void {
        this.bookId = +this.route.snapshot.paramMap.get('id')!;
        this.loadBook(this.bookId);
    }

    loadBook(bookId: number) {
        this.booksService.getBookById(bookId).subscribe({
            next: (book) => {
                this.book = book;
                console.log(this.book);
            },
            error: (error) => {
                console.error('Error fetching book details:', error);
            }
        });
    }

    incrementQuantity(): void {
        const currentBook = this.book;
        if (!currentBook) return;

        if (this.canIncrementQuantity()) {
            this.quantity.update(q => q + 1);
        }
    }

    canIncrementQuantity(): boolean {
        return !!this.book && (!this.book.stock || this.quantity() < this.book.stock);
    }

    decrementQuantity(): void {
        if (this.quantity() > 1) {
            this.quantity.update(q => q - 1);
        }
    }

    onQuantityInput(event: Event): void {
        const input = event.target as HTMLInputElement;
        let value = parseInt(input.value, 10);

        const currentBook = this.book;
        if (!currentBook) return;

        if (isNaN(value) || value < 1) {
            value = 1;
        } else if (currentBook.stock && value > currentBook.stock) {
            value = currentBook.stock;
        }

        this.quantity.set(value);
        input.value = value.toString();
    }

    addToCart(): void {
        const currentBook = this.book;
        console.log('add to cart')
        if (!currentBook || (currentBook.stock && currentBook.stock === 0)) return;

        console.log('add to cart')
        this.addingToCart.set(true);
        this.errorMessage.set(null);
        this.showSuccessMessage.set(false);

        const cartItem: CartItem = {
            book: {
                id: currentBook.id,
                title: currentBook.title,
                price: currentBook.price,
                image: currentBook.thumbnail
            },
            quantity: this.quantity(),
        };

        this.cartService.addItem(cartItem).subscribe({
            next: (cart) => {
                console.log('Item added to cart:', cart);
                this.addingToCart.set(false);
                this.showSuccessMessage.set(true);

                this.toastr.info("Book added to cart successfully");

                setTimeout(() => {
                    this.showSuccessMessage.set(false);
                }, 3000);

                this.quantity.set(1);
            },
            error: (err) => {
                console.error('Failed to add item to cart:', err);
                this.addingToCart.set(false);
                this.errorMessage.set('Failed to add item to cart. Please try again.');

                setTimeout(() => {
                    this.errorMessage.set(null);
                }, 5000);
            }
        });
    }

    isInCart(): boolean {
        const currentBook = this.book;
        if (!currentBook) return false;
        return this.cartService.isItemInCart(currentBook.id);
    }

    goBack(): void {
        this.router.navigate(['/products']);
    }

    goToCart(): void {
        this.router.navigate(['/cart']);
    }

}

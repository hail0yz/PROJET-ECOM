import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { ToastService } from 'ngx-toastr-notifier';

import { AdminLayoutComponent } from '@/app/features/admin/layout/layout.component';
import { BooksService } from '@/app/core/services/books.service';
import { Category } from '@/app/core/models/category.model';
import { CategoriesService } from '@/app/core/services/categories.service';
import { Book } from '@/app/core/models/book.model';

@Component({
    selector: 'admin-product-edit',
    standalone: true,
    imports: [AdminLayoutComponent, CommonModule, ReactiveFormsModule],
    templateUrl: './product-edit.page.html',
})
export class AdminProductEditPage implements OnInit {
    bookForm!: FormGroup;
    categories = signal<Category[]>([]);
    loading = signal(false);
    loadingBook = signal(false);
    loadingCategories = signal(false);
    imageUrl = signal<string | null>(null);
    selectedFile: File | null = null;
    bookId!: number;
    book?: Book;
    errorMessage = signal<string | null>(null);
    showSuccessMessage = signal(false);

    constructor(
        private fb: FormBuilder,
        private booksService: BooksService,
        private categoryService: CategoriesService,
        private route: ActivatedRoute,
        private router: Router,
        private toastr: ToastService
    ) {
        this.initForm();
    }

    ngOnInit(): void {
        this.bookId = +this.route.snapshot.paramMap.get('id')!;
        this.loadCategories();
        this.fetchBook(this.bookId);
    }

    initForm(): void {
        this.bookForm = this.fb.group({
            isbn10: ['', [Validators.pattern(/^\d{10}$/)]],
            isbn13: ['', [Validators.pattern(/^\d{13}$/)]],
            title: ['', [Validators.required, Validators.minLength(3)]],
            author: ['', [Validators.required, Validators.minLength(2)]],
            category: [null, [Validators.required]],
            price: ['', [Validators.required, Validators.min(0.01)]],
            summary: ['']
        });
    }

    fetchBook(bookId: number): void {
        this.loadingBook.set(true);

        this.booksService.getBookById(bookId).subscribe({
            next: (book) => {
                this.book = book;
                this.loadingBook.set(false);
                this.bookForm.patchValue({
                    isbn10: book.isbn10,
                    isbn13: book.isbn13,
                    title: book.title,
                    author: book.author,
                    category: book.category?.id,
                    price: book.price,
                    summary: book.summary || ''
                });
                if (book.thumbnail) {
                    this.imageUrl.set(book.thumbnail);
                }
            },
            error: () => {
                this.errorMessage.set('Échec du chargement du produit');
                this.loadingBook.set(false);
            }
        });
    }

    loadCategories(): void {
        this.loadingCategories.set(true);
        this.categoryService.getCategories().subscribe({
            next: (categories) => {
                this.categories.set(categories);
                this.loadingCategories.set(false);
            },
            error: (err) => {
                this.toastr.error('Échec du chargement des catégories');
                this.loadingCategories.set(false);
            }
        });
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files[0]) {
            this.selectedFile = input.files[0];

            const reader = new FileReader();
            reader.onload = (e: ProgressEvent<FileReader>) => {
                this.imageUrl.set(e.target?.result as string);
            };
            reader.readAsDataURL(this.selectedFile);
        }
    }

    async onSubmit(): Promise<void> {
        if (this.bookForm.invalid) {
            this.bookForm.markAllAsTouched();
            this.toastr.error('Veuillez remplir tous les champs obligatoires');
            return;
        }

        this.loading.set(true);

        try {
            const formData = new FormData();

            const bookRequest = {
                title: this.bookForm.value.title,
                description: this.bookForm.value.summary || '',
                author: this.bookForm.value.author,
                categoryId: this.bookForm.value.category,
                price: this.bookForm.value.price,
                summary: this.bookForm.value.summary || null,
            };

            if (this.selectedFile) {
                formData.append('image', this.selectedFile);
            }

            formData.append('request', new Blob([JSON.stringify(bookRequest)], { type: 'application/json' }));

            this.booksService.updateBook(this.bookId, formData).subscribe({
                next: (response) => {
                    this.loading.set(false);
                    this.toastr.success('Livre ajouté avec succès');
                },
                error: (err) => {
                    console.error('Error creating book:', err);
                    this.toastr.error('Échec de la création du livre');
                    this.loading.set(false);
                }
            });
        } catch (error) {
            console.error('Error in submit:', error);
            this.loading.set(false);
        }
    }

    cancel(): void {
        this.router.navigate(['/admin/products']);
    }
}

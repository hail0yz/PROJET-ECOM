import { Component, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { ToastService } from 'ngx-toastr-notifier';

import { AdminLayoutComponent } from '@/app/features/admin/layout/layout.component';
import { BooksService } from '@/app/core/services/books.service';
import { Category } from '@/app/core/models/category.model';
import { CategoriesService } from '@/app/core/services/categories.service';

@Component({
    selector: 'admin-product-create',
    standalone: true,
    imports: [AdminLayoutComponent, CommonModule, ReactiveFormsModule],
    templateUrl: './product-create.page.html',
})
export class AdminProductCreatePage implements OnInit {
    bookForm!: FormGroup;
    categories = signal<Category[]>([]);
    loading = signal(false);
    imageUrl = signal<string | null>(null);
    selectedFile: File | null = null;

    constructor(
        private fb: FormBuilder,
        private booksService: BooksService,
        private categoryService: CategoriesService,
        private router: Router,
        private toastr: ToastService
    ) {
        this.initForm();
    }

    ngOnInit(): void {
        this.loadCategories();
    }

    initForm(): void {
        this.bookForm = this.fb.group({
            isbn10: ['', [Validators.required, Validators.pattern(/^\d{10}$/)]],
            isbn13: ['', [Validators.pattern(/^\d{13}$/)]],
            title: ['', [Validators.required, Validators.minLength(3)]],
            author: ['', [Validators.required, Validators.minLength(2)]],
            category: [null, [Validators.required]],
            stock: [0, [Validators.required, Validators.min(0)]],
            price: ['', [Validators.required, Validators.min(0.01)]],
            summary: ['']
        });
    }

    loadCategories(): void {
        this.categoryService.getCategories().subscribe({
            next: (categories) => {
                this.categories.set(categories);
            },
            error: (err) => {
                console.error('Error loading categories:', err);
                this.toastr.error('Échec du chargement des catégories');
            }
        });
    }

    onFileSelected(event: Event): void {
        const input = event.target as HTMLInputElement;
        if (input.files && input.files[0]) {
            this.selectedFile = input.files[0];

            // Preview local
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

        if (!this.selectedFile) {
            this.toastr.error('Veuillez sélectionner une image');
            return;
        }

        this.loading.set(true);

        try {
            const formData = new FormData();

            // Créer l'objet request avec les données du livre
            const bookRequest = {
                isbn10: this.bookForm.value.isbn10 || '',
                isbn13: this.bookForm.value.isbn13 || '',
                title: this.bookForm.value.title,
                description: this.bookForm.value.summary || '',
                author: this.bookForm.value.author,
                categoryId: this.bookForm.value.category,
                initialStock: this.bookForm.value.stock,
                price: this.bookForm.value.price
            };

            // Ajouter l'objet request comme JSON
            formData.append('request', new Blob([JSON.stringify(bookRequest)], { type: 'application/json' }));

            // Ajouter l'image
            formData.append('image', this.selectedFile);

            this.booksService.createBook(formData).subscribe({
                next: (response) => {
                    this.toastr.success('Livre ajouté avec succès');
                    this.router.navigate(['/admin/products']);
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

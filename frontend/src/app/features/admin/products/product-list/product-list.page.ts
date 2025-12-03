import { Component, computed, OnInit, signal } from '@angular/core';
import { AdminLayoutComponent } from '@/app/features/admin/layout/layout.component';
import { BooksService } from '@/app/core/services/books.service';
import { Book, BookFilters } from '@/app/core/models/book.model';
import { Category } from '@/app/core/models/category.model';
import { CategoriesService } from '@/app/core/services/categories.service';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Page } from '@/app/core/models/page.model';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';

@Component({
  selector: 'admin-product-list',
  standalone: true,
  imports: [AdminLayoutComponent, CommonModule, FormsModule, RouterLink],
  templateUrl: './product-list.page.html',
})
export class AdminProductListPage implements OnInit {
  pagedBooks = signal<Page<Book> | null>(null);
  categories = signal<Category[]>([]);
  loading = signal<boolean>(true);

  filters = signal<BookFilters>({
    page: 1,
    size: 10
  });

  searchText: string = '';

  pages = computed(() => {
    if (!this.pagedBooks()) return [];
    const totalPages = this.pagedBooks()!.totalPages;
    const current = this.pagedBooks()!.number; // 0-based index

    const start = Math.max(0, current - 2);
    const end = Math.min(totalPages - 1, current + 2);

    return Array.from({ length: end - start + 1 }, (_, i) => start + i + 1);
  });

  error = signal<string | null>(null);

  constructor(
    private booksService: BooksService,
    private categoriesService: CategoriesService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
    this.loadCategories();

    this.route.queryParams.subscribe(params => {
      const categoryParam = params['category'];

      console.log('Query params:', params);

      this.filters.set({
        search: params['search'] || undefined,
        page: +params['page'] || 1,
        size: +params['size'] || 10,
        minPrice: params['minPrice'] ? +params['minPrice'] : undefined,
        maxPrice: params['maxPrice'] ? +params['maxPrice'] : undefined,
        category: categoryParam ? +categoryParam : undefined
      });

      this.searchText = this.filters().search || '';

      console.log('Filters applied:', this.filters());
      this.loadBooks(this.filters());
    });
  }

  loadBooks(filter: BookFilters): void {
    this.loading.set(true);
    this.error.set(null);

    this.booksService.getAllBooks(filter).subscribe({
      next: (response) => {
        this.pagedBooks.set(response);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading books:', err);
        this.error.set(err.message || 'Échec du chargement des produits');
        this.loading.set(false);
        this.pagedBooks.set(null);
      }
    });
  }

  goToPage(page: number) {
    this.updateFilters({ page });
  }

  updateFilters(newFilters: Partial<BookFilters>) {
    console.log('filter changes', newFilters)
    const updated = { ...this.filters(), ...newFilters };
    this.filters.set(updated);

    const queryParams: any = {
      page: updated.page,
      size: updated.size,
    };

    if (updated.search) {
      queryParams.search = updated.search;
    }
    if (updated.minPrice !== undefined) {
      queryParams.minPrice = updated.minPrice;
    }
    if (updated.maxPrice !== undefined) {
      queryParams.maxPrice = updated.maxPrice;
    }
    if (updated.category) {
      queryParams.category = updated.category;
    }

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: queryParams
    });
  }

  onSearch(searchText: string) {
    this.updateFilters({ search: searchText, page: 1 });
  }

  loadCategories(): void {
    this.categoriesService.getCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
      },
      error: (err) => {
        console.error('Error loading categories:', err);
      }
    });
  }

  onCategoryChange(categoryId: string): void {
    const category = categoryId ? +categoryId : undefined;
    this.updateFilters({ category, page: 1 });
  }

}

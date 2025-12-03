import { Component, computed, signal, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { Book, BookFilters } from '@/app/core/models/book.model';
import { Category } from '@/app/core/models/category.model';
import { BooksService } from '@/app/core/services/books.service';
import { CategoriesService } from '@/app/core/services/categories.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { SidebarComponent } from './components/sidebar/sidebar.component';
import { BookCardComponent } from './components/book-card/book-card.component';
import { Page } from '@/app/core/models/page.model';

@Component({
  selector: 'product-list-page',
  standalone: true,
  imports: [NavbarComponent, FooterComponent, SidebarComponent, BookCardComponent],
  templateUrl: './products.page.html',
})
export class ProductListPage implements OnInit {
  pagedBooks = signal<Page<Book> | null>(null);
  categories = signal<Category[]>([]);
  loadingBooks = signal<boolean>(true);
  loadingCategories = signal<boolean>(true);
  error = signal<string | null>(null);

  searchText: string = '';

  pages = computed(() => {
    if (!this.pagedBooks()) return [];
    const totalPages = this.pagedBooks()!.totalPages;
    const current = this.pagedBooks()!.number; // 0-based index

    const start = Math.max(0, current - 2);
    const end = Math.min(totalPages - 1, current + 2);

    return Array.from({ length: end - start + 1 }, (_, i) => start + i + 1);
  });

  filters = signal<BookFilters>({
    page: 1,
    size: 9
  });

  readonly Math = Math;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private booksService: BooksService,
    private categoriesService: CategoriesService
  ) { }

  ngOnInit(): void {
    this.loadCategories();

    this.route.queryParams.subscribe(params => {
      this.filters.set({
        search: params['search'] || undefined,
        page: +params['page'] || 1,
        size: +params['size'] || 9,
        minPrice: params['minPrice'] ? +params['minPrice'] : undefined,
        maxPrice: params['maxPrice'] ? +params['maxPrice'] : undefined,
        category: +params['category'] || undefined
      });

      this.searchText = this.filters().search || '';

      this.loadCategories();

      this.loadBooks(this.filters());
      // effect(() => {
      // });
    });
  }

  loadCategories(): void {
    this.loadingCategories.set(true);

    this.categoriesService.getCategories().subscribe({
      next: (categories) => {
        this.categories.set(categories);
        this.loadingCategories.set(false);
      },
      error: (err) => {
        console.error('Error loading categories:', err);
        this.categories.set([]);
        this.error.set(err.message || 'Échec du chargement des catégories');
        this.loadingCategories.set(false);
      }
    });
  }

  loadBooks(filter: BookFilters): void {
    this.loadingBooks.set(true);
    this.error.set(null);

    this.booksService.getAllBooks(filter).subscribe({
      next: (response) => {
        this.pagedBooks.set(response);
        this.loadingBooks.set(false);
      },
      error: (err) => {
        console.error('Error loading books:', err);
        this.error.set(err.message || 'Échec du chargement des livres');
        this.loadingBooks.set(false);
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

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: {
        search: updated.search || null,
        page: updated.page,
        size: updated.size,
        minPrice: updated.minPrice ?? null,
        maxPrice: updated.maxPrice ?? null,
        category: updated.category ?? null
      },
      queryParamsHandling: 'merge'
    });
  }

  onSearchClick(searchText: string) {
    this.updateFilters({ search: searchText.trim(), page: 1 });
  }

}
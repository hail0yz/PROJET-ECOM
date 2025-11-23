import { Component, computed, OnInit, signal } from '@angular/core';
import { AdminLayoutComponent } from '@/app/features/admin/layout/layout.component';
import { BooksService } from '@/app/core/services/books.service';
import { Book, BookFilters } from '@/app/core/models/book.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Page } from '@/app/core/models/page.model';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'admin-product-list',
  standalone: true,
  imports: [AdminLayoutComponent, CommonModule, FormsModule],
  templateUrl: './product-list.page.html',
})
export class AdminProductListPage implements OnInit {
  pagedBooks = signal<Page<Book> | null>(null);
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

  errorMessage = '';

  constructor(
    private booksService: BooksService,
    private router: Router,
    private route: ActivatedRoute
  ) { }

  ngOnInit(): void {
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

      this.loadBooks(this.filters());
    });
  }

  loadBooks(filter: BookFilters): void {
    this.booksService.getAllBooks(filter).subscribe({
      next: (response) => {
        this.pagedBooks.set(response);
        this.loading.set(false);
      },
      error: (err) => {
        this.errorMessage = err.message || 'Failed to load books';
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
      },
      queryParamsHandling: 'merge'
    });
  }

  onSearch(searchText: string) {
    this.updateFilters({ search: searchText, page: 1 });
  }

}

import { Component, OnInit, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterModule, ActivatedRoute } from '@angular/router';

import { Category } from '@/app/core/models/category.model';
import { Page } from '@/app/core/models/page.model';
import { CategoriesService } from '@/app/core/services/categories.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';

@Component({
    selector: 'categories-page',
    standalone: true,
    imports: [CommonModule, RouterModule, NavbarComponent, FooterComponent],
    templateUrl: './categories.page.html',
})
export class CategoriesPage implements OnInit {
    readonly Math = Math;

    pagedCategories = signal<Page<Category> | null>(null);
    loading = signal<boolean>(true);
    errorMessage = signal<string>('');
    currentPage = signal<number>(0);
    pageSize = signal<number>(12);

    categories = computed(() => this.pagedCategories()?.content || []);
    totalPages = computed(() => this.pagedCategories()?.totalPages || 0);
    totalElements = computed(() => this.pagedCategories()?.totalElements || 0);

    pages = computed(() => {
        if (!this.pagedCategories()) return [];
        const total = this.totalPages();
        const current = this.currentPage();

        const start = Math.max(0, current - 2);
        const end = Math.min(total - 1, current + 2);

        return Array.from({ length: end - start + 1 }, (_, i) => start + i);
    });

    constructor(
        private categoriesService: CategoriesService,
        private errorHandler: ErrorHandlerService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit(): void {
        this.route.queryParams.subscribe(params => {
            const page = params['page'] ? +params['page'] - 1 : 0;
            this.loadCategories(page);
        });
    }

    loadCategories(page: number = 0): void {
        this.loading.set(true);
        this.errorMessage.set('');
        this.currentPage.set(page);

        this.categoriesService.getCategoriesPaged(page, this.pageSize()).subscribe({
            next: (pagedCategories) => {
                this.pagedCategories.set(pagedCategories);
                this.loading.set(false);
            },
            error: (err) => {
                const errorMsg = this.errorHandler.getErrorMessage(err, 'chargement des catégories');
                this.errorMessage.set(`${errorMsg.title}: ${errorMsg.message}`);
                this.loading.set(false);
            }
        });
    }

    goToPage(page: number): void {
        if (page < 0 || page >= this.totalPages()) return;

        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { page: page + 1 },
            queryParamsHandling: 'merge'
        });
    }

    navigateToCategory(categoryId: number): void {
        this.router.navigate(['/products'], { queryParams: { category: categoryId } });
    }
}

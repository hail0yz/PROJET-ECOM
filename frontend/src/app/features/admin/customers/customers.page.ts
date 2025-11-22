import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminLayoutComponent } from '../layout/layout.component';
import { CustomerService, CustomerDTO } from '@/app/core/services/customer.service';
import { Page } from '@/app/core/models/page.model';
import { catchError, finalize, of } from 'rxjs';

@Component({
    selector: 'admin-customers',
    imports: [AdminLayoutComponent, CommonModule, RouterModule],
    templateUrl: './customers.page.html'
})
export class AdminCustomersPage implements OnInit {
    private customerService = inject(CustomerService);

    customers: CustomerDTO[] = [];
    loading = false;
    error: string | null = null;
    currentPage = 0;
    pageSize = 20;
    totalElements = 0;
    totalPages = 0;

    ngOnInit() {
        this.loadCustomers();
    }

    loadCustomers(page: number = 0) {
        this.loading = true;
        this.error = null;
        this.currentPage = page;

        this.customerService.listCustomers(page, this.pageSize)
            .pipe(
                catchError(err => {
                    this.error = 'Failed to load customers';
                    console.error(err);
                    return of({
                        content: [],
                        totalElements: 0,
                        totalPages: 0,
                        number: 0,
                        size: this.pageSize,
                        pageable: {
                            pageNumber: 0,
                            pageSize: this.pageSize,
                            sort: { empty: true, sorted: false, unsorted: true },
                            offset: 0,
                            paged: true,
                            unpaged: false
                        },
                        sort: { empty: true, sorted: false, unsorted: true },
                        numberOfElements: 0,
                        first: true,
                        last: true
                    } as Page<CustomerDTO>);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(page => {
                this.customers = page.content;
                this.totalElements = page.totalElements;
                this.totalPages = page.totalPages;
            });
    }

    deleteCustomer(customerId: string) {
        if (!confirm('Are you sure you want to delete this customer?')) {
            return;
        }

        this.customerService.deleteCustomer(customerId)
            .pipe(
                catchError(err => {
                    this.error = 'Failed to delete customer';
                    console.error(err);
                    return of(null);
                })
            )
            .subscribe(() => {
                this.loadCustomers(this.currentPage);
            });
    }

    formatDate(dateString?: string): string {
        if (!dateString) return 'N/A';
        return new Date(dateString).toLocaleDateString();
    }

    goToPage(page: number) {
        if (page >= 0 && page < this.totalPages) {
            this.loadCustomers(page);
        }
    }
}


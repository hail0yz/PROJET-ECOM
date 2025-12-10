import { Component, OnInit, signal } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';

import { AdminLayoutComponent } from '../layout/layout.component';
import { CustomerService } from '@/app/core/services/customer.service';
import { OrderService } from '@/app/core/services/order.service';
import { BooksService } from '@/app/core/services/books.service';
import { StatsService } from '@/app/core/services/stats.service';

@Component({
  selector: 'admin-dashboard',
  imports: [CommonModule, AdminLayoutComponent, RouterModule],
  templateUrl: './dashboard.page.html',
})
export class AdminDashboardPage implements OnInit {
  stats = {
    totalRevenue: 45780,
    totalOrders: 324,
    totalCustomers: 1289,
    totalProducts: 156,
    revenueGrowth: 12.5,
    ordersGrowth: 8.2,
    customersGrowth: 15.3,
    productsGrowth: -2.4
  };
  aggregatedStats = signal<any>(null);
  loading = signal<boolean>(true);
  errorMessage = signal<string | null>(null);

  constructor(private statsService: StatsService) { }

  ngOnInit(): void {
    this.loading.set(true);
    this.statsService.aggregateStats().subscribe({
      next: (data) => {
        this.aggregatedStats.set(data);
        this.loading.set(false);
        console.log(data)
      },
      error: () => {
        this.errorMessage.set('Échec du chargement des statistiques.');
        this.loading.set(false);
      }
    })
  }

}

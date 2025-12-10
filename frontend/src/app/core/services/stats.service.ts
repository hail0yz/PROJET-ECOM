import { Injectable } from "@angular/core";
import { forkJoin, map } from "rxjs";
import { CustomerService } from "./customer.service";
import { OrderService } from "./order.service";
import { BooksService } from "./books.service";
import { OrderStatsAPI } from "../models/order.model";
import { BookStatsAPI } from "../models/book.model";
import { CustomerStatsAPI } from "../models/customer.model";

type AggregatedStats = OrderStatsAPI & BookStatsAPI & CustomerStatsAPI;

@Injectable({ providedIn: 'root' })
export class StatsService {

    constructor(
        private customerService: CustomerService,
        private orderService: OrderService,
        private bookService: BooksService
    ) { }

    aggregateStats() {
        return forkJoin([
            this.customerService.getStats(),
            this.orderService.getStats(),
            this.bookService.getStats()
        ]).pipe(
            map(([customerStats, orderStats, bookStats]) => {
                return {
                    totalCustomers: customerStats.totalCustomers,
                    totalOrders: orderStats.totalOrders,
                    totalRevenue: orderStats.totalRevenue,
                    totalBooks: bookStats.totalBooks,
                    totalCategories: bookStats.totalCategories
                } as AggregatedStats;
            })
        );
    }


}
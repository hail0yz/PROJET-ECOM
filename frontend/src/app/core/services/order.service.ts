import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlaceOrderRequestAPI, PlaceOrderResponseAPI, OrderResponse, OrderStatsAPI } from '@/app/core/models/order.model';
import { Page } from '../models/page.model';

interface OrderStats {
    totalOrders: number;
    totalRevenue: number;
}

@Injectable({ providedIn: 'root' })
export class OrderService {

    private readonly orderServiceURL = `${environment.apiBaseUrl}/api/orders`;

    constructor(private http: HttpClient) { }

    placeOrder(
        cartId: number,
        address: {
            street: string;
            city: string;
            postalCode: string;
            country: string;
        }
    ): Observable<PlaceOrderResponseAPI> {
        const request: PlaceOrderRequestAPI = { cartId, address }
        return this.http.post<PlaceOrderResponseAPI>(this.orderServiceURL, request);
    }

    getOrders(page: number = 0, size: number = 10): Observable<{ content: OrderResponse[], totalElements: number, totalPages: number }> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<{ content: OrderResponse[], totalElements: number, totalPages: number }>(this.orderServiceURL, { params });
    }

    getOrderById(orderId: string): Observable<OrderResponse> {
        return this.http.get<OrderResponse>(`${this.orderServiceURL}/${orderId}`);
    }

    getMyOrders(page: number = 0, size: number = 10): Observable<{ content: OrderResponse[], totalElements: number, totalPages: number }> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<{ content: OrderResponse[], totalElements: number, totalPages: number }>(`${this.orderServiceURL}/me`, { params });
    }

    getAllOrders(page: number = 0, size: number = 10): Observable<Page<OrderResponse>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<OrderResponse>>(this.orderServiceURL, { params });
    }

    confirmPayment(orderId: string): Observable<any> {
        return this.http.post<any>(`${this.orderServiceURL}/${orderId}/confirm-payment`, {});
    }

    cancelOrder(orderId: string): Observable<void> {
        return this.http.post<void>(`${this.orderServiceURL}/${orderId}/cancel`, {});
    }

    getStats() {
        return this.http.get<OrderStatsAPI>(`${this.orderServiceURL}/stats`);
    }

}

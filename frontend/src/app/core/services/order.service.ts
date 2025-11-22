import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { PlaceOrderRequestAPI, PlaceOrderResponseAPI, OrderResponse } from '@/app/core/models/order.model';
import { environment } from '@/app/environment';



@Injectable({ providedIn: 'root' })
export class OrderService {

    private orderServiceURL = `${environment.apiBaseUrl}/api/orders`;

    constructor(private http: HttpClient) { }

    placeOrder(request: PlaceOrderRequestAPI): Observable<PlaceOrderResponseAPI> {
        return this.http.post<PlaceOrderResponseAPI>(this.orderServiceURL, request);
    }

    getAllOrders(): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(this.orderServiceURL);
    }

    getOrderById(orderId: string): Observable<OrderResponse> {
        return this.http.get<OrderResponse>(`${this.orderServiceURL}/${orderId}`);
    }

    getOrdersByCustomerId(customerId: string, page: number = 0, size: number = 20): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(`${this.orderServiceURL}/customer/${customerId}`, {
            params: { page: page.toString(), size: size.toString() }
        });
    }

    getMyOrders(page: number = 0, size: number = 20): Observable<OrderResponse[]> {
        return this.http.get<OrderResponse[]>(`${this.orderServiceURL}/my-orders`, {
            params: { page: page.toString(), size: size.toString() }
        });
    }

}

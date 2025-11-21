import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { PlaceOrderRequestAPI } from '@/app/core/models/order.model';

@Injectable({ providedIn: 'root' })
export class OrderService {

    private orderServiceURL = "http://localhost:8080/api/orders";

    constructor(private http: HttpClient) { }

    placeOrder(
        cartId: number,
        address: {
            street: string;
            city: string;
            postalCode: string;
            country: string;
        },
        paymentDetails: {
            paymentMethod: string
        }
    ) {
        const request: PlaceOrderRequestAPI = { cartId, address, paymentDetails }
        return this.http.post<void>(this.orderServiceURL, request);
    }

}

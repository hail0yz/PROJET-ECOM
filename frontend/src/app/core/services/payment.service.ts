import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/app/environment';

export interface CreatePaymentRequest {
    orderId: string;
    amount: number;
    currency: string;
    paymentMethod: string;
    customerEmail: string;
}

export interface ProcessPaymentRequest {
    paymentId: number;
    paymentMethod: string;
    paymentIntentId?: string;
}

export interface RefundRequest {
    paymentId: number;
    amount?: number;
    reason?: string;
}

export interface PaymentResponse {
    paymentId: number;
    orderId: string;
    amount: number;
    currency: string;
    status: string;
    paymentMethod: string;
    transactionId?: string;
    message?: string;
}

export interface PaymentDTO {
    id: number;
    orderId: string;
    amount: number;
    currency: string;
    status: string;
    paymentMethod: string;
    transactionId?: string;
    customerEmail: string;
    createdAt?: string;
    updatedAt?: string;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
    private readonly apiUrl = `${environment.apiBaseUrl}/api/payments`;

    constructor(private http: HttpClient) { }

    createPayment(request: CreatePaymentRequest): Observable<PaymentResponse> {
        return this.http.post<PaymentResponse>(this.apiUrl, request);
    }

    processPayment(request: ProcessPaymentRequest): Observable<PaymentResponse> {
        return this.http.post<PaymentResponse>(`${this.apiUrl}/process`, request);
    }

    refundPayment(request: RefundRequest): Observable<PaymentResponse> {
        return this.http.post<PaymentResponse>(`${this.apiUrl}/refund`, request);
    }

    cancelPayment(paymentId: number): Observable<{ message: string }> {
        return this.http.put<{ message: string }>(`${this.apiUrl}/${paymentId}/cancel`, {});
    }

    getPaymentById(paymentId: number): Observable<PaymentDTO> {
        return this.http.get<PaymentDTO>(`${this.apiUrl}/${paymentId}`);
    }

    getPaymentByOrderId(orderId: string): Observable<PaymentDTO> {
        return this.http.get<PaymentDTO>(`${this.apiUrl}/order/${orderId}`);
    }

    getAllPaymentsByOrderId(orderId: string): Observable<PaymentDTO[]> {
        return this.http.get<PaymentDTO[]>(`${this.apiUrl}/order/${orderId}/all`);
    }

    syncPaymentWithStripe(paymentId: number): Observable<PaymentResponse> {
        return this.http.get<PaymentResponse>(`${this.apiUrl}/${paymentId}/sync`);
    }
}


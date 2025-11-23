import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/app/environment';

export interface ConfirmPaymentRequest {
    orderId?: string | null;
    paymentIntentId: string;
    paymentStatus: string;
    transactionId?: string | null;
}

@Injectable({ providedIn: 'root' })
export class PaymentService {
    private readonly api = `${environment.apiBaseUrl}/api/payments`;

    constructor(private http: HttpClient) { }

    confirmPayment(payload: ConfirmPaymentRequest): Observable<any> {
        return this.http.post<any>(`${this.api}/confirm`, payload);
    }

    syncPayment(paymentId: number): Observable<any> {
        return this.http.get<any>(`${this.api}/${paymentId}/sync`);
    }

}

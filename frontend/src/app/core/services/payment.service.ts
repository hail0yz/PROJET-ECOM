import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  Payment, 
  CreatePaymentRequest, 
  PaymentResponse 
} from '../models/payment.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class PaymentService {
  private apiUrl = `${environment.apiUrl}/payments`;

  constructor(private http: HttpClient) {}

  /**
   * Créer un nouveau paiement
   */
  createPayment(request: CreatePaymentRequest): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(this.apiUrl, request);
  }

  /**
   * Récupérer tous les paiements
   */
  getAllPayments(): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(this.apiUrl);
  }

  /**
   * Récupérer un paiement par ID
   */
  getPaymentById(id: number): Observable<PaymentResponse> {
    return this.http.get<PaymentResponse>(`${this.apiUrl}/${id}`);
  }

  /**
   * Récupérer les paiements par commande
   */
  getPaymentsByOrder(orderId: number): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(`${this.apiUrl}/order/${orderId}`);
  }

  /**
   * Récupérer les paiements par email
   */
  getPaymentsByEmail(email: string): Observable<PaymentResponse[]> {
    return this.http.get<PaymentResponse[]>(`${this.apiUrl}/customer/${email}`);
  }

  /**
   * Traiter un paiement (confirmer)
   */
  processPayment(paymentId: number): Observable<PaymentResponse> {
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/${paymentId}/process`, 
      {}
    );
  }

  /**
   * Annuler un paiement
   */
  cancelPayment(paymentId: number): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/${paymentId}/cancel`, {});
  }

  /**
   * Rembourser un paiement
   */
  refundPayment(paymentId: number, amount?: number): Observable<PaymentResponse> {
    const body = amount ? { amount } : {};
    return this.http.post<PaymentResponse>(
      `${this.apiUrl}/${paymentId}/refund`, 
      body
    );
  }
}
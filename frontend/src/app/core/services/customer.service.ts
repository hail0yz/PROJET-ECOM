import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/app/environment';
import { Page } from '@/app/core/models/page.model';

export interface CustomerProfileDTO {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    createdAt?: string;
}

export interface CustomerDetailsDTO {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    createdAt?: string;
    updatedAt?: string;
}

export interface CustomerPreferencesDTO {
    language?: string;
    currency?: string;
    emailNotifications?: boolean;
    smsNotifications?: boolean;
    preferredCategories?: string[];
}

export interface UpdatePreferencesRequest {
    language?: string;
    currency?: string;
    emailNotifications?: boolean;
    smsNotifications?: boolean;
    preferredCategories?: string[];
}

export interface CustomerDTO {
    id: string;
    email: string;
    firstName: string;
    lastName: string;
    phone?: string;
    createdAt?: string;
}

@Injectable({ providedIn: 'root' })
export class CustomerService {
    private readonly apiUrl = `${environment.apiBaseUrl}/api/customers`;

    constructor(private http: HttpClient) { }

    getCustomerProfile(customerId: string): Observable<CustomerProfileDTO> {
        return this.http.get<CustomerProfileDTO>(`${this.apiUrl}/${customerId}/profile`);
    }

    getCustomerDetails(customerId: string): Observable<CustomerDetailsDTO> {
        return this.http.get<CustomerDetailsDTO>(`${this.apiUrl}/${customerId}/details`);
    }

    getCustomerPreferences(customerId: string): Observable<CustomerPreferencesDTO> {
        return this.http.get<CustomerPreferencesDTO>(`${this.apiUrl}/${customerId}/preferences`);
    }

    updateCustomerPreferences(customerId: string, preferences: UpdatePreferencesRequest): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${customerId}/preferences`, preferences);
    }

    listCustomers(page: number = 0, size: number = 20): Observable<Page<CustomerDTO>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<CustomerDTO>>(this.apiUrl, { params });
    }

    deleteCustomer(customerId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${customerId}`);
    }
}


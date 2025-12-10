import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { environment } from '@/app/environment';
import { Page } from '@/app/core/models/page.model';
import { CustomerAPI, CustomerDetailsAPI, CustomerPreferencesAPI, CustomerProfileAPI, UpdatePreferencesRequest, UpdateProfileRequest } from '@/app/core/models/customer.model';

@Injectable({ providedIn: 'root' })
export class CustomerService {
    private readonly apiUrl = `${environment.apiBaseUrl}/api/customers`;

    constructor(private http: HttpClient) { }

    getCustomerProfile(customerId: string): Observable<CustomerProfileAPI> {
        return this.http.get<CustomerProfileAPI>(`${this.apiUrl}/${customerId}/profile`);
    }

    getCustomerDetails(customerId: string): Observable<CustomerDetailsAPI> {
        return this.http.get<CustomerDetailsAPI>(`${this.apiUrl}/${customerId}/details`);
    }

    getCustomerPreferences(customerId: string): Observable<CustomerPreferencesAPI> {
        return this.http.get<CustomerPreferencesAPI>(`${this.apiUrl}/${customerId}/preferences`);
    }

    updateCustomerProfile(customerId: string, profile: UpdateProfileRequest): Observable<CustomerProfileAPI> {
        return this.http.put<CustomerProfileAPI>(`${this.apiUrl}/${customerId}/profile`, profile);
    }

    updateCustomerPreferences(customerId: string, preferences: UpdatePreferencesRequest): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${customerId}/preferences`, preferences);
    }

    listCustomers(page: number = 0, size: number = 20): Observable<Page<CustomerAPI>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<CustomerAPI>>(this.apiUrl, { params });
    }

    deleteCustomer(customerId: string): Observable<void> {
        return this.http.delete<void>(`${this.apiUrl}/${customerId}`);
    }

    getStats(): Observable<any> {
        return this.http.get<any>(`${this.apiUrl}/stats`);
    }

}


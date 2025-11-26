import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from '@/app/environment';

export interface CustomerRegistrationRequest {
    firstname: string;
    lastname: string;
    email: string;
    password: string;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
    private registrationApiUrl = `${environment.apiBaseUrl}/register`;

    constructor(private http: HttpClient) { }

    registerCustomer(request: CustomerRegistrationRequest): Observable<any> {
        return this.http.post<any>(this.registrationApiUrl, request);
    }

}

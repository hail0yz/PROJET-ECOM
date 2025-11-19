import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

@Injectable({ providedIn: 'root' })
export class OrderService {

    private orderServiceURL = "http://localhost:8080/api/categories";

    constructor(private http: HttpClient) { }

}

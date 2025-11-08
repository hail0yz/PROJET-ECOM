import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Category } from '@/app/core/models/category.model';
import { Observable } from 'rxjs';

@Injectable({ providedIn: 'root' })
export class CategoriesService {

    private categoryServiceURL = "http://localhost:8080/api/v1/categories";

    constructor(private http: HttpClient) { }

    getCategories(): Observable<Category[]> {
        return this.http.get<Category[]>(this.categoryServiceURL);
    }

}

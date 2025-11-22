import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';

import { Category } from '@/app/core/models/category.model';
import { Observable } from 'rxjs';
import { environment } from '@/app/environment';

@Injectable({ providedIn: 'root' })
export class CategoriesService {

    private categoryServiceURL = `${environment.apiBaseUrl}/api/categories`;

    constructor(private http: HttpClient) { }

    getCategories(): Observable<Category[]> {
        return this.http.get<Category[]>(this.categoryServiceURL);
    }

}

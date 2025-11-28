import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';

import { Category } from '@/app/core/models/category.model';
import { Page } from '@/app/core/models/page.model';
import { Observable } from 'rxjs';
import { environment } from '@/app/environment';

@Injectable({ providedIn: 'root' })
export class CategoriesService {

    private categoryServiceURL = `${environment.apiBaseUrl}/api/categories`;

    constructor(private http: HttpClient) { }

    getCategories(): Observable<Category[]> {
        return this.http.get<Category[]>(this.categoryServiceURL);
    }

    getCategoriesPaged(page: number = 0, size: number = 12): Observable<Page<Category>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<Category>>(`${this.categoryServiceURL}/paged`, { params });
    }

}

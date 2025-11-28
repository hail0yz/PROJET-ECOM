import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '@/app/environment';

export interface InventoryResponseDTO {
    id: number;
    bookid: number;
    availableQuantity: number;
    reservedQuantity: number;
    minimumStockLevel: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface InventoryDTO {
    id: number;
    book: any;
    availableQuantity: number;
    reservedQuantity: number;
    minimumStockLevel: number;
    version?: number;
    createdAt?: string;
    updatedAt?: string;
}

export interface UpdateQuantityRequest {
    quantity: number;
}

export interface CreateInventoryForExistingBookRequest {
    bookId: number;
    availableQuantity: number;
    minimumStockLevel: number;
}

@Injectable({ providedIn: 'root' })
export class InventoryService {
    private readonly apiUrl = `${environment.apiBaseUrl}/api/inventory`;

    constructor(private http: HttpClient) { }

    getAllInventory(): Observable<InventoryResponseDTO[]> {
        return this.http.get<InventoryResponseDTO[]>(`${this.apiUrl}/admin`);
    }

    getInventoryById(id: number): Observable<InventoryDTO> {
        return this.http.get<InventoryDTO>(`${this.apiUrl}/${id}`);
    }

    searchInventoryByTitle(title: string): Observable<InventoryResponseDTO[]> {
        const params = new HttpParams().set('title', title);
        return this.http.get<InventoryResponseDTO[]>(`${this.apiUrl}/search`, { params });
    }

    updateQuantity(bookId: number, request: UpdateQuantityRequest): Observable<void> {
        return this.http.put<void>(`${this.apiUrl}/${bookId}`, request);
    }

    addStock(bookId: number, quantity: number): Observable<any> {
        const params = new HttpParams().set('quantity', quantity.toString());
        return this.http.put<any>(`${this.apiUrl}/${bookId}/add-stock`, {}, { params });
    }

    createInventoryForExistingBook(request: CreateInventoryForExistingBookRequest): Observable<any> {
        return this.http.post<any>(`${this.apiUrl}/existing-book`, request);
    }
}


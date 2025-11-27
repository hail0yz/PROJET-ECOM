import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { Page } from '@/app/core/models/page.model';
import { TicketAPI, TicketStatsAPI } from '@/app/core/models/ticket.model';

@Injectable({ providedIn: 'root' })
export class TicketService {
    private apiUrl = 'http://localhost:8080/api/tickets';

    constructor(private http: HttpClient) { }

    getTicketById(ticketId: number): Observable<TicketAPI> {
        return this.http.get<TicketAPI>(`${this.apiUrl}/${ticketId}`);
    }

    listMyTickets(page = 0, size = 20): Observable<Page<TicketAPI>> {
        const params = new HttpParams()
            .set('page', page.toString())
            .set('size', size.toString());
        return this.http.get<Page<TicketAPI>>(`${this.apiUrl}/me`, { params });
    }

    createTicketForCustomer(payload: any): Observable<any> {
        return this.http.post(this.apiUrl, payload);
    }

    addMessage(ticketId: number, payload: any): Observable<any> {
        return this.http.post(`${this.apiUrl}/${ticketId}/messages`, payload);
    }

    listAllTickets(filters: any): Observable<any> {
        let params = new HttpParams();
        if (filters.page != null) params = params.set('page', String(Math.max(0, filters.page - 1)));
        if (filters.size != null) params = params.set('size', String(filters.size));
        if (filters.customerId) params = params.set('customerId', filters.customerId);
        if (filters.status) params = params.set('status', filters.status);
        if (filters.priority) params = params.set('priority', filters.priority);
        return this.http.get(`${this.apiUrl}`, { params });
    }

    getStats(): Observable<TicketStatsAPI> {
        return this.http.get<TicketStatsAPI>(`${this.apiUrl}/stats`);
    }

}

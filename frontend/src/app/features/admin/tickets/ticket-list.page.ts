import { Component, computed, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';

import { Page } from '@/app/core/models/page.model';
import { TicketAPI, TicketStatus } from '@/app/core/models/ticket.model';
import { TicketService } from '@/app/core/services/ticket.service';
import { AdminLayoutComponent } from '@/app/features/admin/layout/layout.component';

@Component({
    selector: 'admin-ticket-list',
    standalone: true,
    imports: [AdminLayoutComponent, CommonModule, FormsModule],
    templateUrl: './ticket-list.page.html',
})
export class AdminTicketListPage implements OnInit {
    readonly TicketStatus = TicketStatus;
    pagedTickets = signal<Page<any> | null>(null);
    loading = signal<boolean>(true);
    errorMessage = signal<string>('');
    stats = signal<any>(null);

    pages = computed(() => {
        if (!this.pagedTickets()) return [];
        const totalPages = this.pagedTickets()!.totalPages;
        const current = this.pagedTickets()!.number; // 0-based index

        const start = Math.max(0, current - 2);
        const end = Math.min(totalPages - 1, current + 2);

        return Array.from({ length: end - start + 1 }, (_, i) => start + i + 1);
    });

    constructor(
        private ticketService: TicketService,
        private router: Router,
        private route: ActivatedRoute
    ) { }

    ngOnInit(): void {
        this.loadStats();

        this.route.queryParams.subscribe(params => {
            this.loadTickets((+params['page'] || 1) - 1, +params['size'] || 9);
        });
    }

    loadStats() {
        this.ticketService.getStats().subscribe({
            next: (stats) => {
                this.stats.set(stats);
            },
            error: (err) => {
                console.error('Error fetching ticket stats:', err);
            }
        });
    }

    loadTickets(page: number, size: number) {
        this.loading.set(true);
        this.errorMessage.set('');

        this.ticketService.listAllTickets({}).subscribe({
            next: (res: any) => {
                this.pagedTickets.set(res);
                this.loading.set(false);
            },
            error: (err) => {
                this.errorMessage.set(err?.error?.message || err?.message || 'Erreur lors du chargement des tickets');
                this.loading.set(false);
            }
        });
    }

    applyFilters() {
        // TODO: implement logic to apply filters
    }


    goToPage(page: number) {
        if (!this.pagedTickets()) return;

        if (page < 1 || page > this.pagedTickets()?.totalPages!) return;
        this.router.navigate([], {
            relativeTo: this.route,
            queryParams: { page: page },
            queryParamsHandling: 'merge'
        });
    }

    resetFilters() {
        // TODO: implement logic to reset filters
    }

    hasActiveFilters(): boolean {
        // TODO: implement logic to check if any filters are active
        return false;
    }

    getTicketsByStatus(status: TicketStatus): TicketAPI[] {
        if (!this.pagedTickets()?.content) return [];
        return this.pagedTickets()!.content.filter((t: any) => t.status === status);
    }

    getUrgentTickets(): TicketAPI[] {
        if (!this.pagedTickets()?.content) return [];
        return this.pagedTickets()!.content.filter((t: any) => t.priority === 'URGENT');
    }

    getCountByPriority(priority: string): number {
        return this.stats()?.countsByPriority?.[priority] || 0;
    }

    getCountByType(type: string): number {
        return this.stats()?.countsByType?.[type] || 0;
    }

    getStatusLabel(status: string | TicketStatus): string {
        const statusKey = typeof status === 'string' ? status : TicketStatus[status];
        const labels: { [key: string]: string } = {
            'IN_PROGRESS': 'En cours',
            'WAITING_CUSTOMER': 'Attente client',
            'WAITING_VENDOR': 'Attente fournisseur',
            'RESOLVED': 'Résolu',
            'CLOSED': 'Fermé',
            'REOPENED': 'Réouvert',
            'ESCALATED': 'Escaladé'
        };
        return labels[statusKey] || statusKey;
    }

    getStatusClass(status: TicketStatus): string {
        const classes: { [key in TicketStatus]: string } = {
            [TicketStatus.IN_PROGRESS]: 'bg-blue-100 text-blue-800',
            [TicketStatus.WAITING_CUSTOMER]: 'bg-yellow-100 text-yellow-800',
            [TicketStatus.WAITING_VENDOR]: 'bg-purple-100 text-purple-800',
            [TicketStatus.RESOLVED]: 'bg-green-100 text-green-800',
            [TicketStatus.CLOSED]: 'bg-gray-100 text-gray-800',
            [TicketStatus.REOPENED]: 'bg-orange-100 text-orange-800',
            [TicketStatus.ESCALATED]: 'bg-red-100 text-red-800'
        };
        return classes[status];
    }

    getPriorityLabel(priority?: string): string {
        const labels: { [key: string]: string } = {
            'low': 'Basse',
            'medium': 'Moyenne',
            'high': 'Haute',
            'urgent': 'Urgente'
        };
        return priority ? labels[priority] : '-';
    }

    getPriorityClass(priority?: string): string {
        const classes: { [key: string]: string } = {
            'low': 'bg-gray-100 text-gray-800',
            'medium': 'bg-blue-100 text-blue-800',
            'high': 'bg-orange-100 text-orange-800',
            'urgent': 'bg-red-100 text-red-800'
        };
        return priority ? classes[priority] : 'bg-gray-100 text-gray-800';
    }

    getCategoryLabel(category?: string): string {
        const labels: { [key: string]: string } = {
            'technical': 'Technique',
            'billing': 'Facturation',
            'feature': 'Fonctionnalité',
            'bug': 'Bug',
            'question': 'Question'
        };
        return category ? labels[category] : '-';
    }

    formatDate(dateString: string): string {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

}

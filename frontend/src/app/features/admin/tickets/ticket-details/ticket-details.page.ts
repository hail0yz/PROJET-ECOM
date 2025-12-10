import { Component, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

import { TicketAPI, TicketStatus } from '@/app/core/models/ticket.model';
import { TicketService } from '@/app/core/services/ticket.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
import { AdminLayoutComponent } from '@/app/features/admin/layout/layout.component';

@Component({
    selector: 'admin-ticket-details',
    standalone: true,
    imports: [AdminLayoutComponent, CommonModule, FormsModule, RouterModule],
    templateUrl: './ticket-details.page.html',
})
export class AdminTicketDetailsPage implements OnInit {
    readonly TicketStatus = TicketStatus;
    private keycloak = inject(Keycloak);
    ticket = signal<TicketAPI | null>(null);
    loading = signal<boolean>(true);
    errorMessage = signal<string>('');
    newMessageContent = signal<string>('');
    sendingMessage = signal<boolean>(false);

    constructor(
        private ticketService: TicketService,
        private router: Router,
        private route: ActivatedRoute,
        private errorHandler: ErrorHandlerService
    ) { }

    ngOnInit(): void {
        this.route.params.subscribe(params => {
            const ticketId = +params['id'];
            if (ticketId) {
                this.loadTicket(ticketId);
            }
        });
    }

    loadTicket(ticketId: number) {
        this.loading.set(true);
        this.errorMessage.set('');

        this.ticketService.getTicketById(ticketId).subscribe({
            next: (ticket) => {
                this.ticket.set(ticket);
                this.loading.set(false);
            },
            error: (err) => {
                const errorMsg = this.errorHandler.getErrorMessage(err, 'chargement du ticket');
                this.errorMessage.set(`${errorMsg.title}: ${errorMsg.message}`);
                this.loading.set(false);
            }
        });
    }

    sendMessage() {
        if (!this.newMessageContent().trim() || !this.ticket()) return;

        this.sendingMessage.set(true);
        const payload = {
            content: this.newMessageContent().trim()
        };

        this.ticketService.addMessage(this.ticket()!.id, payload).subscribe({
            next: () => {
                this.newMessageContent.set('');
                this.sendingMessage.set(false);
                this.loadTicket(this.ticket()!.id);
            },
            error: (err) => {
                const errorMsg = this.errorHandler.getErrorMessage(err, 'envoi du message');
                this.errorMessage.set(`${errorMsg.title}: ${errorMsg.message}`);
            }
        });
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

    formatDate(dateString?: string): string {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'long',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    formatDateShort(dateString?: string): string {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        });
    }

    closeTicket() {
        if (!this.ticket()) return;

        if (!confirm('Êtes-vous sûr de vouloir fermer ce ticket ? Cette action est irréversible.')) {
            return;
        }

        this.loading.set(true);
        this.errorMessage.set('');

        this.ticketService.closeTicket(this.ticket()!.id).subscribe({
            next: (updatedTicket) => {
                this.ticket.set(updatedTicket);
                this.loading.set(false);
                alert('Ticket fermé avec succès');
            },
            error: (err) => {
                const errorMsg = this.errorHandler.getErrorMessage(err, 'fermeture du ticket');
                this.errorMessage.set(`${errorMsg.title}: ${errorMsg.message}`);
                this.loading.set(false);
            }
        });
    }

    changeStatus(newStatus: string) {
        if (!this.ticket()) return;

        this.loading.set(true);
        this.errorMessage.set('');

        this.ticketService.changeTicketStatus(this.ticket()!.id, newStatus).subscribe({
            next: (updatedTicket) => {
                this.loadTicket(this.ticket()!.id);
            },
            error: (err) => {
                const errorMsg = this.errorHandler.getErrorMessage(err, 'changement de statut');
                this.errorMessage.set(`${errorMsg.title}: ${errorMsg.message}`);
                this.loading.set(false);
            }
        });
    }

    isTicketClosed(): boolean {
        const status = this.ticket()?.status;
        return status === TicketStatus.CLOSED || (status as any) === 'CLOSED';
    }

    isTicketEscalatedOrClosed(): boolean {
        const status = this.ticket()?.status;
        return status === TicketStatus.ESCALATED || (status as any) === 'ESCALATED' ||
            status === TicketStatus.CLOSED || (status as any) === 'CLOSED';
    }

    goBack() {
        this.router.navigate(['/admin/tickets']);
    }

    getMessageAuthorLabel(message: { role: string; authorId: string }): string {
        if (this.keycloak.tokenParsed?.sub === message.authorId) {
            return 'Moi';
        }

        return (message.role === 'ROLE_SUPPORT' || message.role === 'ROLE_ADMIN') ? 'Support' : 'Client';
    }

    priorities = [
        { value: 'LOW', label: 'Basse', color: 'bg-gray-400' },
        { value: 'MEDIUM', label: 'Moyenne', color: 'bg-blue-300' },
        { value: 'HIGH', label: 'Haute', color: 'bg-orange-500' },
        { value: 'URGENT', label: 'Urgente', color: 'bg-red-500' }
    ];

    types = [
        { value: 'ORDER_ISSUE', label: 'Problème de commande' },
        { value: 'PAYMENT_ISSUE', label: 'Problème de paiement' },
        { value: 'PRODUCT_QUERY', label: 'Problème de produit' },
        { value: 'ACCOUNT_ISSUE', label: 'Problème de compte' },
        { value: 'FEEDBACK', label: 'Retour d\'information' },
        { value: 'OTHER', label: 'Autre' },
    ];

    getPriorityLabel(priority: string = 'MEDIUM'): { label: string, color: string } {
        return this.priorities.find(p => p.value === priority) || {
            label: priority,
            color: 'bg-blue-300'
        };
    }

    getTypeLabel(type: string): { label: string, color?: string } {
        return this.types.find(t => t.value === type) || { label: type };
    }

}

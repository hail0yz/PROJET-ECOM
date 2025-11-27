import { Component, OnInit, signal } from '@angular/core';
import Keycloak from 'keycloak-js';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TicketService } from '@/app/core/services/ticket.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { TicketAPI, TicketStatus } from '@/app/core/models/ticket.model';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'ticket-details-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './ticket.page.html'
})
export class TicketDetailsUserPage implements OnInit {
  ticket: TicketAPI | null = null;
  loading = signal<boolean>(true);
  errorMessage: string = '';

  constructor(
    private ticketService: TicketService,
    private route: ActivatedRoute,
    private router: Router
  ) { }

  ngOnInit(): void {
    const ticketId = +this.route.snapshot.paramMap.get('id')!;
    this.loadTicketDetails(ticketId);
  }

  loadTicketDetails(ticketId: number) {
    this.ticketService.getTicketById(ticketId).subscribe({
      next: (res: TicketAPI) => {
        this.ticket = res;
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error loading ticket details:', err);
        this.errorMessage = 'Erreur lors du chargement des informations du ticket.';
        this.loading.set(false);
      }
    });
  }

  getStatusLabel(status: TicketStatus): string {
    const labels: { [key in TicketStatus]: string } = {
      [TicketStatus.IN_PROGRESS]: 'En cours',
      [TicketStatus.WAITING_CUSTOMER]: 'Attente client',
      [TicketStatus.WAITING_VENDOR]: 'Attente fournisseur',
      [TicketStatus.RESOLVED]: 'Résolu',
      [TicketStatus.CLOSED]: 'Fermé',
      [TicketStatus.REOPENED]: 'Réouvert',
      [TicketStatus.ESCALATED]: 'Escaladé'
    };
    return labels[status];
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

  formatDate(dateString: string): string {
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  goBack() {
    this.router.navigate(['/tickets']);
  }

  isAdminOrSupport(role: string): boolean {
    return role === 'ADMIN' || role === 'SUPPORT';
  }

}

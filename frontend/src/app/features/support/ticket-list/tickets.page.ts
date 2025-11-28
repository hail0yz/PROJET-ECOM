import { Component, computed, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { TicketService } from '@/app/core/services/ticket.service';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { TicketAPI, TicketStatus } from '@/app/core/models/ticket.model';
import { Page } from '@/app/core/models/page.model';
import { ActivatedRoute, Router } from '@angular/router';

@Component({
  selector: 'ticket-list-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './tickets.page.html'
})
export class TicketListUserPage implements OnInit {
  readonly TicketStatus = TicketStatus;
  pagedTickets = signal<Page<TicketAPI> | null>(null)
  loading = signal<boolean>(false);
  errorMessage = signal<string>('');

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
    this.loadTickets();
  }

  loadTickets() {
    this.loading.set(true);
    this.errorMessage.set('');

    this.route.queryParams.subscribe(params => {
      this.ticketService.listMyTickets((+params['page'] || 1) - 1, +params['size'] || 9).subscribe({
        next: (res: any) => {
          this.pagedTickets.set(res);
          this.loading.set(false);
          console.log(this.pages(), this.pagedTickets()!.number);
        },
        error: (err) => {
          this.errorMessage.set(err?.error?.message || err?.message || 'Erreur lors du chargement des tickets');
          this.loading.set(false);
        }
      });
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

  getTicketsByStatus(status: TicketStatus): TicketAPI[] {
    return this.pagedTickets()?.content.filter(ticket => String(ticket.status) === String(status)) || [];
  }

  formatDate(value: string | number | Date | undefined): string {
    if (!value) return '-';
    try {
      const d = typeof value === 'string' || typeof value === 'number' ? new Date(value) : value as Date;
      return d.toLocaleString();
    } catch (e) {
      return String(value);
    }
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

}

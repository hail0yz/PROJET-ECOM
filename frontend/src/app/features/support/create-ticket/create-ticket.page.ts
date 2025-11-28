import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { Router } from '@angular/router';
import { TicketService } from '@/app/core/services/ticket.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';

@Component({
  selector: 'create-ticket-page',
  standalone: true,
  imports: [CommonModule, FormsModule, NavbarComponent],
  templateUrl: './create-ticket.page.html'
})
export class CreateTicketPage {

  formData = {
    subject: '',
    description: '',
    priority: 'medium',
    category: '',
    customerId: ''
  };

  priorities = [
    { value: 'low', label: 'Basse', color: 'bg-gray-400' },
    { value: 'medium', label: 'Moyenne', color: 'bg-blue-500' },
    { value: 'high', label: 'Haute', color: 'bg-orange-500' },
    { value: 'urgent', label: 'Urgente', color: 'bg-red-500' }
  ];

  isSubmitting = false;
  showSuccess = false;
  errorMessage: string | null = null;

  constructor(
    private router: Router,
    private ticketService: TicketService,
    private errorHandler: ErrorHandlerService
  ) { }

  onSubmit() {
    if (this.isSubmitting) return;

    this.isSubmitting = true;

    const payload = {
      subject: this.formData.subject,
      description: this.formData.description,
      priority: this.formData.priority,
      category: this.formData.category,
      customerId: this.formData.customerId
    };

    this.ticketService.createTicketForCustomer(payload).subscribe({
      next: () => {
        this.showSuccess = true;
        this.isSubmitting = false;
      },
      error: (err) => {
        console.error('Error creating ticket:', err);
        const errorMsg = this.errorHandler.getErrorMessage(err, 'envoi du message');
        this.errorMessage = `${errorMsg.title}: ${errorMsg.message}`;
        this.isSubmitting = false;
      }
    });
  }

  onCancel() {
    if (confirm('Êtes-vous sûr de vouloir annuler ? Les données saisies seront perdues.')) {
      this.router.navigate(['/tickets']);
    }
  }

  goBack() {
    this.router.navigate(['/tickets']);
  }

}

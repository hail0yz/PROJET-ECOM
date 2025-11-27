import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { Router } from '@angular/router';

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

  constructor(private router: Router) { }

  onSubmit() {
    if (this.isSubmitting) return;

    this.isSubmitting = true;

    setTimeout(() => {
      console.log('Ticket créé:', this.formData);

      this.showSuccess = true;
      this.isSubmitting = false;

      setTimeout(() => {
        this.router.navigate(['/tickets']);
      }, 2000);
    }, 1500);
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

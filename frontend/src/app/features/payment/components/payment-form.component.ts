import { Component, Input, OnInit } from '@angular/core';
import { loadStripe, Stripe, StripeElements, StripeCardElement } from '@stripe/stripe-js';
import { environment } from '../../../../../environments/environment';

@Component({
  selector: 'app-payment-form',
  templateUrl: './payment-form.component.html',
  styleUrls: ['./payment-form.component.css']
})
export class PaymentFormComponent implements OnInit {
  @Input() amount: number = 0;

  stripe: Stripe | null = null;
  elements: StripeElements | null = null;
  cardElement: StripeCardElement | null = null;

  async ngOnInit() {
    // Charger Stripe
    this.stripe = await loadStripe(environment.stripePublishableKey);
    
    if (this.stripe) {
      this.elements = this.stripe.elements();
      this.cardElement = this.elements.create('card');
      this.cardElement.mount('#card-element');
    }
  }

  async getPaymentMethod() {
    if (!this.stripe || !this.cardElement) {
      return null;
    }

    const { paymentMethod, error } = await this.stripe.createPaymentMethod({
      type: 'card',
      card: this.cardElement
    });

    if (error) {
      console.error('Erreur Stripe:', error);
      return null;
    }

    return paymentMethod;
  }
}
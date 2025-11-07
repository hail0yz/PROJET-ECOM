import { Component, OnInit } from '@angular/core';
import { Router, ActivatedRoute } from '@angular/router';
import { PaymentService } from '../../core/services/payment.service';
import { CartService } from '../../core/services/cart.service';
import { 
  CreatePaymentRequest, 
  PaymentMethod, 
  PaymentResponse 
} from '../../core/models/payment.model';

@Component({
  selector: 'app-payment',
  templateUrl: './payment.page.html',
  styleUrls: ['./payment.page.css']
})
export class PaymentPage implements OnInit {
  orderId: number = 0;
  totalAmount: number = 0;
  customerEmail: string = '';
  selectedPaymentMethod: PaymentMethod = PaymentMethod.CARD;
  isProcessing: boolean = false;
  error: string = '';

  PaymentMethod = PaymentMethod; // Pour utiliser dans le template

  constructor(
    private paymentService: PaymentService,
    private cartService: CartService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    // Récupérer les infos depuis le panier ou la route
    this.route.queryParams.subscribe(params => {
      this.orderId = params['orderId'] || 0;
      this.totalAmount = params['amount'] || 0;
      this.customerEmail = params['email'] || '';
    });

    // Ou depuis le service de panier
    this.totalAmount = this.cartService.getTotal();
  }

  onPaymentMethodChange(method: PaymentMethod) {
    this.selectedPaymentMethod = method;
  }

  processPayment() {
    this.isProcessing = true;
    this.error = '';

    const paymentRequest: CreatePaymentRequest = {
      orderId: this.orderId,
      amount: this.totalAmount,
      customerEmail: this.customerEmail,
      paymentMethod: this.selectedPaymentMethod
    };

    this.paymentService.createPayment(paymentRequest).subscribe({
      next: (response: PaymentResponse) => {
        console.log('Paiement créé:', response);
        this.isProcessing = false;
        
        // Rediriger vers la page de confirmation
        this.router.navigate(['/payment/confirmation'], {
          queryParams: { paymentId: response.paymentId }
        });
      },
      error: (err) => {
        console.error('Erreur paiement:', err);
        this.error = err.error?.message || 'Erreur lors du paiement';
        this.isProcessing = false;
      }
    });
  }
}
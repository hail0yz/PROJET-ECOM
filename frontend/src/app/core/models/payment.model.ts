export interface Payment {
    paymentId?: number;
    orderId: number;
    amount: number;
    customerEmail: string;
    paymentMethod: PaymentMethod;
    status?: PaymentStatus;
    dateCreation?: Date;
    stripePaymentIntentId?: string;
    transactionId?: string;
    failureReason?: string;
  }
  
  export enum PaymentMethod {
    CARD = 'CARD',
    BANK_TRANSFER = 'BANK_TRANSFER',
    PAYPAL = 'PAYPAL'
  }
  
  export enum PaymentStatus {
    PENDING = 'PENDING',
    PROCESSING = 'PROCESSING',
    COMPLETED = 'COMPLETED',
    FAILED = 'FAILED',
    CANCELLED = 'CANCELLED',
    REFUNDED = 'REFUNDED',
    REQUIRES_ACTION = 'REQUIRES_ACTION'
  }
  
  export interface CreatePaymentRequest {
    orderId: number;
    amount: number;
    customerEmail: string;
    paymentMethod: PaymentMethod;
  }
  
  export interface PaymentResponse {
    paymentId: number;
    amount: number;
    status: PaymentStatus;
    dateCreation: Date;
    customerEmail: string;
    paymentMethod: PaymentMethod;
    stripePaymentIntentId?: string;
    transactionId?: string;
  }
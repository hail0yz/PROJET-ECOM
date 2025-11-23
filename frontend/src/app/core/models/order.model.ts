export enum OrderStatus {
    PENDING = 'PENDING',
    FAILED = 'FAILED',
    PAYMENT_FAILED = 'PAYMENT_FAILED',
    PAYMENT_PENDING = 'PAYMENT_PENDING',
    CANCELLED = 'CANCELLED',
    COMPLETED = 'COMPLETED',
    PROCESSING = 'PROCESSING'
}

export interface PlaceOrderRequestAPI {
    cartId: number;
    reference?: string;
    address: {
        street: string;
        city: string;
        postalCode: string;
        country: string;
    },
    paymentDetails: {
        paymentMethod: string;
    }
}

export interface PlaceOrderResponseAPI {
    orderId: string;
    orderStatus: OrderStatus;
    paymentDetails: {
        paymentId: number;
        paymentStatus: string;
        transactionId: string;
        paymentMethod: string;
        stripePaymentIntentId: string;
        clientSecret: string;
    }
}

export interface OrderLineResponse {
    id: string;
    productId: number;
    quantity: number;
}

export interface OrderResponse {
    orderId: string;
    reference?: string;
    customerId: string;
    cartId?: number;
    payment_method: string;
    amount: number;
    status?: string;
    createdAt?: string;
    updatedAt?: string;
    lines?: OrderLineResponse[];
}
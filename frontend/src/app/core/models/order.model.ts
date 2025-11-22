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
    paymentId: number;
}

export interface OrderResponse {
    orderId: string;
    reference?: string;
    customerId: string;
    payment_method: string;
    amount: number;
    status?: string;
    createdAt?: string;
    updatedAt?: string;
}
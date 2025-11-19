export interface PlaceOrderRequestAPI {
    cartId: number;
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
export interface CartItem {
    book: {
        id: string;
        title: string;
        price: number;
    }
    quantity: number;
    image?: string;
}

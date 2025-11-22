export interface Cart {
    id: number;
    items: CartItem[],
    local: boolean,
    persisted?: boolean
}

export interface CartItem {
    book: {
        id: number;
        title: string;
        price: number;
        image?: string;
    }
    quantity: number;
}

export interface CartEntryAPI {
    productId: number;
    quantity: number
}


export interface GetCartResponseAPI {
    id: number;
    userId: string;
    items: { productId: number, quantity: number, title: string, image?: string, price: number }[],
    createdAt: string;
    updatedAt: string;
    totalPrice: number;
}

export interface CreateCartRequestAPI {
    items: {
        productId: number;
        quantity: number;
        price: number;
    }[]
}

export interface CreateCartResponseAPI {
    cartId: number;
}
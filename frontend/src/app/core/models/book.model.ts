import {Category} from '@core/models/category.model';

export interface Book {
    id: number;
    title: string;
    author: string;
    summary: string;
    price: number;
    image?: string;
    category?: string;
    rating?: number;
    stock?: number;
    publishedDate?: string;
    publisher?: string;
    isbn?: string;
    language?: string;
    format?: 'paperback' | 'hardcover' | 'ebook';
    tags?: string[];
}

import { Category } from '@core/models/category.model';

export interface Book {
    id: number;
    title: string;
    author: string;
    summary: string;
    price: number;
    thumbnail?: string;
    category?: Category;
    rating?: number;
    stock?: number;
    publishedYear?: string;
    publisher?: string;
    isbn10?: string;
    isbn13?: string;
    language?: string;
    numPages?: number;
    tags?: string[];
}

export interface BookFilters {
    search?: string;
    page: number;
    size: number;
    minPrice?: number;
    maxPrice?: number;
    category?: number;
}

export interface BookStatsAPI {
    totalBooks: number;
    totalCategories: number;
}
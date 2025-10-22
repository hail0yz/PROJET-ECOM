import { Routes } from '@angular/router';
import { HomeComponent } from './features/home/home.component';
import { ProductDetailPage } from './features/products/pages/product-details/product.page';
import { ProductListPage } from './features/products/pages/product-list/products.page';
import { CartPage } from './features/cart/cart.page';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'products', component: ProductListPage },
    { path: 'products/:id', component: ProductDetailPage },
    { path: 'cart', component: CartPage },
];

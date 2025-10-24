import { Routes } from '@angular/router';

import { HomeComponent } from './features/home/home.component';
import { ProductDetailPage } from './features/products/pages/product-details/product.page';
import { ProductListPage } from './features/products/pages/product-list/products.page';
import { CartPage } from './features/cart/cart.page';
import { AdminDashboardPage } from './features/admin/dashboard/dashboard.page';
import { AdminCategoriesPage } from './features/admin/categories/categories.page';
import { RegistrationPage } from './features/registration/registration.page';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'products', component: ProductListPage },
    { path: 'products/:id', component: ProductDetailPage },
    { path: 'cart', component: CartPage },
    { path: 'signup', component: RegistrationPage },
    { path: 'admin', component: AdminDashboardPage },
    { path: 'admin/categories', component: AdminCategoriesPage },
];

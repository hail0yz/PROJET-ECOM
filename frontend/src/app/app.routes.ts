import { Routes } from '@angular/router';

import { HomeComponent } from '@/app/features/home/home.component';
import { ProductDetailPage } from '@/app/features/products/pages/product-details/product.page';
import { ProductListPage } from '@/app/features/products/pages/product-list/products.page';
import { CartPage } from '@/app/features/cart/cart.page';
import { AdminDashboardPage } from '@/app/features/admin/dashboard/dashboard.page';
import { AdminCategoriesPage } from '@/app/features/admin/categories/categories.page';
import { RegistrationPage } from '@/app/features/registration/registration.page';
import { AdminProductListPage } from '@/app/features/admin/products/product-list/product-list.page';

import { canActivateAuthRole } from '@/app/core/guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        component: HomeComponent,
        data: { public: true },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'products',
        component: ProductListPage,
        data: { public: true }
    },
    {
        path: 'products/:id',
        component: ProductDetailPage,
        data: { public: true }
    },
    {
        path: 'cart',
        component: CartPage,
        data: { public: true }
    },
    {
        path: 'signup',
        component: RegistrationPage,
        data: { public: true }
    },
    {
        path: 'admin',
        data: {
            role: ['admin']
        },
        component: AdminDashboardPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/categories',
        data: {
            role: ['admin']
        },
        component: AdminCategoriesPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/products',
        data: {
            role: ['admin']
        },
        component: AdminProductListPage,
        canActivate: [canActivateAuthRole]
    },
];

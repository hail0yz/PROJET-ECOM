import { Routes } from '@angular/router';

import { HomeComponent } from '@/app/features/home/home.component';
import { ProductDetailPage } from '@/app/features/products/pages/product-details/product.page';
import { ProductListPage } from '@/app/features/products/pages/product-list/products.page';
import { CartPage } from '@/app/features/cart/cart.page';
import { AdminDashboardPage } from '@/app/features/admin/dashboard/dashboard.page';
import { AdminCategoriesPage } from '@/app/features/admin/categories/categories.page';
import { RegistrationPage } from '@/app/features/registration/registration.page';
import { AdminProductListPage } from '@/app/features/admin/products/product-list/product-list.page';
import { PaymentPage } from './features/payment/payment.page';
import { PaymentConfirmationComponent } from './features/payment/components/payment-confirmation/payment-confirmation.component';
import { PaymentHistoryComponent } from './features/payment/components/payment-history/payment-history.component';

export const routes: Routes = [
    { path: '', component: HomeComponent },
    { path: 'products', component: ProductListPage },
    { path: 'products/:id', component: ProductDetailPage },
    { path: 'cart', component: CartPage },
    { path: 'signup', component: RegistrationPage },
    { path: 'admin', component: AdminDashboardPage },
    { path: 'admin/categories', component: AdminCategoriesPage },
    { path: 'admin/products', component: AdminProductListPage },
    { path: 'payment',component: PaymentPage},
    {path: 'payment/confirmation',component: PaymentConfirmationComponent},
    {path: 'payment/history', component: PaymentHistoryComponent
    },
];

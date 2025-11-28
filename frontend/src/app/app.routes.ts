import { Routes } from '@angular/router';

import { HomeComponent } from '@/app/features/home/home.component';
import { ProductDetailPage } from '@/app/features/products/pages/product-details/product.page';
import { ProductListPage } from '@/app/features/products/pages/product-list/products.page';
import { CartPage } from '@/app/features/cart/cart.page';
import { CheckoutPage } from '@/app/features/checkout/checkout.page';
import { ProfilePage } from '@/app/features/profile/profile.page';
import { AdminDashboardPage } from '@/app/features/admin/dashboard/dashboard.page';
import { AdminCategoriesPage } from '@/app/features/admin/categories/categories.page';
import { RegistrationPage } from '@/app/features/registration/registration.page';
import { AdminProductListPage } from '@/app/features/admin/products/product-list/product-list.page';
import { AdminOrdersPage } from '@/app/features/admin/orders/orders.page';
import { AdminCustomersPage } from '@/app/features/admin/customers/customers.page';
import { AdminTicketListPage } from '@/app/features/admin/tickets/ticket-list.page';
import { AdminTicketDetailsPage } from '@/app/features/admin/tickets/ticket-details/ticket-details.page';
import { TicketListUserPage } from '@/app/features/support/ticket-list/tickets.page';
import { TicketDetailsUserPage } from '@/app/features/support/ticket-details/ticket.page';
import { AdminInventoryPage } from '@/app/features/admin/inventory/inventory.page';
import { OrdersPage } from '@/app/features/orders/orders.page';
import { OrderDetailsPage } from '@/app/features/orders/order-details.page';
import { CreateTicketPage } from './features/support/create-ticket/create-ticket.page';
import { CategoriesPage } from '@/app/features/categories/categories.page';

import { canActivateAuthRole } from '@/app/core/guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        component: HomeComponent,
        data: { public: true },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'categories',
        component: CategoriesPage,
        data: { public: true }
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
        path: 'checkout',
        component: CheckoutPage,
        data: { public: true },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'orders',
        component: OrdersPage,
        data: {
            role: ['USER']
        },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'orders/:id',
        component: OrderDetailsPage,
        data: {
            role: ['USER']
        },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'profile',
        component: ProfilePage,
        data: {
            role: ['USER']
        },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'signup',
        component: RegistrationPage,
        data: { public: true }
    },
    {
        path: 'admin',
        data: {
            role: ['ADMIN']
        },
        component: AdminDashboardPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/categories',
        data: {
            role: ['ADMIN']
        },
        component: AdminCategoriesPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/products',
        data: {
            role: ['ADMIN']
        },
        component: AdminProductListPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/tickets',
        data: { role: ['ADMIN', 'SUPPORT'] },
        component: AdminTicketListPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/tickets/:id',
        data: { role: ['ADMIN', 'SUPPORT'] },
        component: AdminTicketDetailsPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/customers',
        data: {
            role: ['ADMIN']
        },
        component: AdminCustomersPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'tickets',
        component: TicketListUserPage,
        data: { role: ['USER'] },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'tickets/create',
        component: CreateTicketPage,
        data: { role: ['USER'] },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'tickets/:id',
        component: TicketDetailsUserPage,
        data: { role: ['USER'] },
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/orders',
        data: {
            role: ['ADMIN']
        },
        component: AdminOrdersPage,
        canActivate: [canActivateAuthRole]
    },
    {
        path: 'admin/inventory',
        data: {
            role: ['ADMIN']
        },
        component: AdminInventoryPage,
        canActivate: [canActivateAuthRole]
    },
];

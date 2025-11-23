import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { catchError, finalize, of } from 'rxjs';
import { CustomerService } from '@/app/core/services/customer.service';
import { CustomerPreferencesAPI, CustomerProfileAPI, UpdatePreferencesRequest } from '@/app/core/models/customer.model';

@Component({
    selector: 'app-profile',
    imports: [CommonModule, ReactiveFormsModule, RouterModule, NavbarComponent, FooterComponent],
    templateUrl: './profile.page.html'
})
export class ProfilePage implements OnInit {
    private keycloak = inject(Keycloak);
    private customerService = inject(CustomerService);
    private fb = inject(FormBuilder);

    profile: CustomerProfileAPI | null = null;
    preferences: CustomerPreferencesAPI | null = null;
    preferencesForm!: FormGroup;
    loading = false;
    error: string | null = null;
    customerId: string | null = null;

    ngOnInit() {
        if (this.keycloak.authenticated && this.keycloak.tokenParsed) {
            this.customerId = (this.keycloak.tokenParsed as any).sub || null;
            if (this.customerId) {
                this.loadProfile();
                this.loadPreferences();
                this.initPreferencesForm();
            }
        }
    }

    initPreferencesForm() {
        this.preferencesForm = this.fb.group({
            language: ['en'],
            currency: ['USD'],
            emailNotifications: [true],
            smsNotifications: [false],
            preferredCategories: [[]]
        });
    }

    loadProfile() {
        if (!this.customerId) return;
        this.loading = true;
        this.error = null;

        this.customerService.getCustomerProfile(this.customerId)
            .pipe(
                catchError(err => {
                    this.error = 'Failed to load profile';
                    console.error(err);
                    return of(null);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(profile => {
                this.profile = profile;
            });
    }

    loadPreferences() {
        if (!this.customerId) return;
        this.loading = true;

        this.customerService.getCustomerPreferences(this.customerId)
            .pipe(
                catchError(err => {
                    console.error('Failed to load preferences:', err);
                    return of(null);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(preferences => {
                this.preferences = preferences;
                if (preferences) {
                    this.preferencesForm.patchValue({
                        language: preferences.language || 'en',
                        currency: preferences.currency || 'USD',
                        emailNotifications: preferences.emailNotifications ?? true,
                        smsNotifications: preferences.smsNotifications ?? false,
                        preferredCategories: preferences.preferredCategories || []
                    });
                }
            });
    }

    savePreferences() {
        if (!this.customerId || !this.preferencesForm.valid) return;

        const preferences: UpdatePreferencesRequest = this.preferencesForm.value;
        this.loading = true;
        this.error = null;

        this.customerService.updateCustomerPreferences(this.customerId, preferences)
            .pipe(
                catchError(err => {
                    this.error = 'Failed to update preferences';
                    console.error(err);
                    return of(null);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(() => {
                this.loadPreferences();
                alert('Preferences updated successfully!');
            });
    }

    get isAuthenticated(): boolean {
        return this.keycloak.authenticated ?? false;
    }
}


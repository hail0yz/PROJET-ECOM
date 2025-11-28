import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { catchError, finalize, of } from 'rxjs';
import { CustomerService } from '@/app/core/services/customer.service';
import { ErrorHandlerService } from '@/app/core/services/error-handler.service';
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
    private errorHandler = inject(ErrorHandlerService);

    profile: CustomerProfileAPI | null = null;
    preferences: CustomerPreferencesAPI | null = null;
    profileForm!: FormGroup;
    preferencesForm!: FormGroup;
    loading = false;
    profileError: string | null = null;
    preferencesError: string | null = null;
    successMessage: string | null = null;
    customerId: string | null = null;
    editingProfile = false;

    ngOnInit() {
        if (this.keycloak.authenticated && this.keycloak.tokenParsed) {
            this.customerId = (this.keycloak.tokenParsed as any).sub || null;
            if (this.customerId) {
                this.loadProfile();
                this.loadPreferences();
                this.initForms();
            }
        }
    }

    initForms() {
        this.profileForm = this.fb.group({
            firstname: ['', [Validators.required, Validators.minLength(2)]],
            lastname: ['', [Validators.required, Validators.minLength(2)]],
            email: ['', [Validators.required, Validators.email]],
            phone: ['']
        });

        this.preferencesForm = this.fb.group({
            emailNotificationsEnabled: [true],
            smsNotificationsEnabled: [false]
        });
    }

    toggleEditProfile() {
        this.editingProfile = !this.editingProfile;
        if (this.editingProfile && this.profile) {
            this.profileForm.patchValue({
                firstname: this.profile.firstname,
                lastname: this.profile.lastname,
                email: this.profile.email,
                phone: this.profile.phoneNumber || ''
            });
        }
        this.profileError = null;
        this.successMessage = null;
    }

    cancelEditProfile() {
        this.editingProfile = false;
        this.profileError = null;
        this.successMessage = null;
    }

    loadProfile() {
        if (!this.customerId) return;
        this.loading = true;
        this.profileError = null;

        this.customerService.getCustomerProfile(this.customerId)
            .pipe(
                catchError(err => {
                    this.profileError = this.errorHandler.getErrorMessageText(err, 'chargement du profil');
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
        this.preferencesError = null;

        this.customerService.getCustomerPreferences(this.customerId)
            .pipe(
                catchError(err => {
                    this.preferencesError = this.errorHandler.getErrorMessageText(err, 'chargement des préférences');
                    console.error(err);
                    return of(null);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(preferences => {
                this.preferences = preferences;
                if (preferences) {
                    this.preferencesForm.patchValue({
                        emailNotificationsEnabled: preferences.emailNotificationsEnabled ?? true,
                        smsNotificationsEnabled: preferences.smsNotificationsEnabled ?? false
                    });
                }
            });
    }

    saveProfile() {
        if (!this.customerId || !this.profileForm.valid) return;

        const profileData = this.profileForm.value;
        this.loading = true;
        this.profileError = null;
        this.successMessage = null;

        this.customerService.updateCustomerProfile(this.customerId, profileData)
            .pipe(
                catchError(err => {
                    this.profileError = this.errorHandler.getErrorMessageText(err, 'mise à jour du profil');
                    console.error(err);
                    return of(null);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(result => {
                if (result !== null) {
                    this.successMessage = 'Profil mis à jour avec succès!';
                    this.editingProfile = false;
                    this.loadProfile();
                    setTimeout(() => this.successMessage = null, 3000);
                }
            });
    }

    savePreferences() {
        if (!this.customerId || !this.preferencesForm.valid) return;

        const preferences: UpdatePreferencesRequest = this.preferencesForm.value;
        this.loading = true;
        this.preferencesError = null;
        this.successMessage = null;

        this.customerService.updateCustomerPreferences(this.customerId, preferences)
            .pipe(
                catchError(err => {
                    this.preferencesError = this.errorHandler.getErrorMessageText(err, 'mise à jour des préférences');
                    console.error(err);
                    return of(null);
                }),
                finalize(() => this.loading = false)
            )
            .subscribe(result => {
                if (result !== null) {
                    this.successMessage = 'Préférences mises à jour avec succès!';
                    this.loadPreferences();
                    setTimeout(() => this.successMessage = null, 3000);
                }
            });
    }

    get isAuthenticated(): boolean {
        return this.keycloak.authenticated ?? false;
    }
}


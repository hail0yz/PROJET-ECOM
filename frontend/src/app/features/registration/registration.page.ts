import { Component, inject, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import Keycloak from 'keycloak-js';

import { FooterComponent } from '@/app/core/components/footer/footer.component';
import { NavbarComponent } from '@/app/core/components/navbar/navbar.component';
import { AuthService, CustomerRegistrationRequest } from '@/app/core/services/auth.service';
import { Router } from '@angular/router';

@Component({
  selector: 'registration-page',
  standalone: true,
  imports: [ReactiveFormsModule, NavbarComponent, FooterComponent],
  templateUrl: './registration.page.html',
})
export class RegistrationPage implements OnInit {
  private keycloak = inject(Keycloak);
  registrationForm: FormGroup;
  submitted = false;
  errorMessage = '';
  successMessage = '';

  constructor(private router: Router, private fb: FormBuilder, private authService: AuthService) {
    this.registrationForm = this.fb.group({
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(6)]],
    });
  }

  ngOnInit(): void {
    if (this.keycloak.authenticated) {
      this.router.navigate(['/']);
    }
  }

  onSubmit(): void {
    this.submitted = true;

    console.log(this.registrationForm.get('password'));


    if (this.registrationForm.invalid) {
      this.errorMessage = 'Veuillez corriger les erreurs dans le formulaire.';
      console.error("Input validation failed", this.registrationForm);
      return;
    }

    this.errorMessage = '';

    const customer: CustomerRegistrationRequest = {
      firstname: this.registrationForm.value.firstname,
      lastname: this.registrationForm.value.lastname,
      email: this.registrationForm.value.email,
      password: this.registrationForm.value.password,
    };

    this.authService.registerCustomer(customer).subscribe({
      next: () => {
        this.successMessage = 'Vous êtes inscrit avec succès!';
        this.registrationForm.reset();
        this.submitted = false;
      },
      error: err => {
        this.errorMessage = err.error.error;
      }
    });
  }

}

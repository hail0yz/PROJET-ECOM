import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

@Component({
    selector: 'app-navbar',
    imports: [CommonModule, RouterModule],
    templateUrl: './navbar.component.html',
})
export class NavbarComponent implements OnInit {
    private keycloak = inject(Keycloak);
    isMenuOpen = false;
    isLoggedIn = false;

    ngOnInit() {
        this.isLoggedIn = this.keycloak.authenticated;
    }

    login() {
        this.keycloak.login();
    }

    register() {
        this.keycloak.register();
    }

    logout() {
        this.keycloak.logout();
    }

    toggleMenu() {
        this.isMenuOpen = !this.isMenuOpen;
    }

    isUser(): boolean {
        return this.keycloak.hasRealmRole('USER');
    }

    isAdmin(): boolean {
        return this.keycloak.hasRealmRole('ADMIN');
    }

}

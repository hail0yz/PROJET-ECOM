import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

@Component({
    selector: 'app-footer',
    imports: [CommonModule, RouterModule],
    templateUrl: './footer.component.html',
})
export class FooterComponent {
    private keycloak = inject(Keycloak);

    isCustomer(): boolean {
        return this.keycloak.hasRealmRole('USER');
    }
}

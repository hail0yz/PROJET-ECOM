import { Component, inject, OnInit } from "@angular/core";
import Keycloak from "keycloak-js";

@Component({
    selector: 'app-logout',
    standalone: true,
    template: '<p>Logging out...</p>'
})
export class LogoutPage implements OnInit {
    private keycloak = inject(Keycloak)

    ngOnInit(): void {
        this.keycloak.logout({
            redirectUri: window.location.origin
        });
    }
}

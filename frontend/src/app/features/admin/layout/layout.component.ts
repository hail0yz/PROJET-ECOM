import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import Keycloak from 'keycloak-js';

@Component({
  selector: 'admin-layout',
  imports: [CommonModule, RouterModule],
  templateUrl: './layout.component.html',
})
export class AdminLayoutComponent implements OnInit {
  private keycloak = inject(Keycloak);

  name: string | null = null;
  email: string | null = null;

  ngOnInit(): void {
    if (this.keycloak?.authenticated && this.keycloak?.tokenParsed) {
      const parsed = this.keycloak.tokenParsed as any;
      this.name = parsed.name || parsed.preferred_username || null;
      this.email = parsed.email || null;
    }
  }

  get initials(): string {
    if (!this.name) return '';
    const parts = this.name.split(/\s+/).filter(Boolean);
    if (parts.length === 0) return '';
    if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
    return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
  }

}

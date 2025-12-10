import { Component, Input } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'product-details-breadcrumb',
  imports: [],
  templateUrl: './breadcrumb.html',
  host: { class: 'contents' }
})
export class Breadcrumb {
  @Input() category!: { name: string, id: number } | undefined;
  @Input() productName!: string;

  constructor(private router: Router) { }

  navigateToCategory(): void {
    if (!this.category) return;

    this.router.navigate(['/products'], { queryParams: { category: this.category?.id } });
  }
}

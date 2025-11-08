import { Component, Input } from '@angular/core';

@Component({
  selector: 'product-details-breadcrumb',
  imports: [],
  templateUrl: './breadcrumb.html',
  host: { class: 'contents' }
})
export class Breadcrumb {
  @Input() category!: { name: string, id: number } | undefined;
  @Input() productName!: string;
}

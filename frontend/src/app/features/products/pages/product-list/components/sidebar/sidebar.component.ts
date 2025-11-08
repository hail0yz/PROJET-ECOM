import { Category } from '@/app/core/models/category.model';
import { Component, Input } from '@angular/core';

@Component({
  selector: 'product-list-sidebar',
  imports: [],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  @Input() categories: Category[] = [];

  onCategoryChange(categoryId: number): void {
    console.log('Selected category ID:', categoryId);
  }

}

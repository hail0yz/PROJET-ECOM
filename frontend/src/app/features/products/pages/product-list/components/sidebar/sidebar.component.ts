import { Category } from '@/app/core/models/category.model';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'product-list-sidebar',
  imports: [],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  @Input() categories: Category[] = [];
  @Input() selectedCategory?: number | undefined;

  @Output() categoryChange = new EventEmitter<number | undefined>();

  onCategorySelect(categoryId: number | undefined) {
    console.log('Selected category ID:', categoryId);
    this.categoryChange.emit(categoryId);
  }

}

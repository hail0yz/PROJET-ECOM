import { Category } from '@/app/core/models/category.model';
import { Component, EventEmitter, Input, Output, computed, signal } from '@angular/core';

@Component({
  selector: 'product-list-sidebar',
  imports: [],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent {
  @Input() categories: Category[] = [];
  @Input() selectedCategory?: number | undefined;

  @Output() categoryChange = new EventEmitter<number | undefined>();

  searchTerm = signal<string>('');

  filteredCategories = computed(() => {
    const term = this.searchTerm().toLowerCase().trim();
    if (!term) {
      return this.categories;
    }
    return this.categories.filter(category =>
      category.name.toLowerCase().includes(term)
    );
  });

  onCategorySelect(categoryId: number | undefined) {
    console.log('Selected category ID:', categoryId);
    this.categoryChange.emit(categoryId);
  }

  onSearchChange(event: Event) {
    const input = event.target as HTMLInputElement;
    this.searchTerm.set(input.value);
  }

}

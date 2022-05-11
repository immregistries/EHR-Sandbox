import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectCodebaseComponent } from './_components/select-codebase/select-codebase.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';



@NgModule({
  declarations: [
    SelectCodebaseComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    MatInputModule,
    MatAutocompleteModule,
    MatBadgeModule,
    MatTooltipModule,
    MatIconModule,
  ],
  exports: [
    ReactiveFormsModule,
    FormsModule,
    MatInputModule,
    MatAutocompleteModule,
    MatBadgeModule,
    MatTooltipModule,
    MatIconModule,

    SelectCodebaseComponent,
  ],
})
export class SharedModule { }

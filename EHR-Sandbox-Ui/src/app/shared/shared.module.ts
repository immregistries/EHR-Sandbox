import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectCodebaseComponent } from './_components/select-codebase/select-codebase.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatBadgeModule } from '@angular/material/badge';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatIconModule } from '@angular/material/icon';
import { FacilityListComponent } from './_components/_lists/_card-lists/facility-list/facility-list.component';
import { TenantListComponent } from './_components/_lists/_card-lists/tenant-list/tenant-list.component';
import { TenantCreationComponent } from './_components/_dialogs/tenant-creation/tenant-creation.component';
import { FacilityCreationComponent } from './_components/_dialogs/facility-creation/facility-creation.component';
import { TenantMenuComponent } from './_components/_lists/_menus/tenant-menu/tenant-menu.component';
import { FacilityMenuComponent } from './_components/_lists/_menus/facility-menu/facility-menu.component';
import { SettingsDialogComponent } from './_components/_dialogs/settings-dialog/settings-dialog.component';
import { FeedbackTableComponent } from './feedback-table/feedback-table.component';
import { FeedbackDialogComponent } from './feedback-table/feedback-dialog/feedback-dialog.component';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTableModule } from '@angular/material/table';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { MatDialogActions, MatDialogModule } from '@angular/material/dialog';
import { MatCardModule } from '@angular/material/card';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatButtonModule } from '@angular/material/button';


@NgModule({
  declarations: [
    SelectCodebaseComponent,
    TenantListComponent,
    FacilityListComponent,
    TenantCreationComponent,
    FacilityCreationComponent,
    TenantMenuComponent,
    FacilityMenuComponent,
    SettingsDialogComponent,
    FeedbackTableComponent,
    FeedbackDialogComponent,
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
    MatProgressBarModule,
    MatTableModule,
    MatMenuModule,
    MatDividerModule,
    MatListModule,
    MatDialogModule,
    MatCardModule,
    MatGridListModule,
    MatButtonModule
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
    TenantListComponent,
    FacilityListComponent,
    TenantCreationComponent,
    FacilityCreationComponent,
    TenantMenuComponent,
    FacilityMenuComponent,
    SettingsDialogComponent,
    FeedbackTableComponent,
    FeedbackDialogComponent,
  ],
})
export class SharedModule { }

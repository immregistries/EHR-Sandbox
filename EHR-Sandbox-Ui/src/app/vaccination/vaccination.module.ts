import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { VaccinationFormComponent } from './vaccination-form/vaccination-form.component';
import { VaccinationCreationComponent } from './vaccination-form/vaccination-creation/vaccination-creation.component';
import { VaccinationTableComponent } from './vaccination-table/vaccination-table.component';
import { VaccinationFreeFormComponent } from './vaccination-form/vaccination-free-form/vaccination-free-form.component';
import { SharedModule } from '../shared/shared.module';
import { FhirModule } from '../fhir/fhir.module';


import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatNativeDateModule } from '@angular/material/core';
import { MatDialogModule } from '@angular/material/dialog';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatSelectFilterModule } from 'mat-select-filter';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { LayoutModule } from '@angular/cdk/layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { VaccinationDashboardComponent } from './vaccination-dashboard/vaccination-dashboard.component';
import { VaccinationDashboardDialogComponent } from './vaccination-dashboard/vaccination-dashboard-dialog/vaccination-dashboard-dialog.component';
import { VaccinationDetailsComponent } from './vaccination-details/vaccination-details.component';
import { VaccinationHistoryComponent } from './vaccination-history/vaccination-history.component';
import { RecommendationTableComponent } from './recommandation-table/recommendation-table.component';

@NgModule({
  declarations: [
    VaccinationFormComponent,
    VaccinationCreationComponent,
    VaccinationTableComponent,
    VaccinationFreeFormComponent,
    VaccinationDashboardComponent,
    VaccinationDashboardDialogComponent,
    VaccinationDetailsComponent,
    VaccinationHistoryComponent,
    RecommendationTableComponent,
  ],
  imports: [
    CommonModule,
    LayoutModule,
    ReactiveFormsModule,
    FormsModule,

    SharedModule,
    FhirModule,
    // CoreModule,

    MatToolbarModule,
    MatButtonModule,
    MatSidenavModule,
    MatIconModule,
    MatListModule,
    MatInputModule,
    MatDatepickerModule,
    MatGridListModule,
    MatCardModule,
    MatMenuModule,
    MatTabsModule,
    MatSnackBarModule,
    MatNativeDateModule,
    MatDialogModule,
    MatTableModule,
    MatSelectModule,
    MatSelectFilterModule,
    MatAutocompleteModule,
    MatTooltipModule,
    MatProgressBarModule,
    MatBadgeModule,
    MatButtonToggleModule,
  ],
  exports: [
    VaccinationFormComponent,
    VaccinationCreationComponent,
    VaccinationTableComponent,
    VaccinationFreeFormComponent,
    RecommendationTableComponent,
  ]
})
export class VaccinationModule { }

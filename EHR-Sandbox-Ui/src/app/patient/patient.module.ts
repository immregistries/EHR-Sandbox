import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PatientFormComponent } from './patient-form/patient-form.component';
import { PatientListComponent } from './patient-list/patient-list.component';
import { PatientFreeFormComponent } from './patient-free-form/patient-free-form.component';
import { PatientTableComponent } from './patient-table/patient-table.component';
import { PatientDetailsComponent } from './patient-details/patient-details.component';
import { SharedModule } from '../shared/shared.module';
import { FhirModule } from '../fhir/fhir.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';


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
import { CoreModule } from '../core/core.module';
import { PatientFormDialogComponent } from './patient-form/patient-form-dialog/patient-form-dialog.component';
import { PatientDashboardComponent } from './patient-dashboard/patient-dashboard.component';
import { LayoutModule } from '@angular/cdk/layout';
import { VaccinationModule } from '../vaccination/vaccination.module';
import { PatientDashboardDialogComponent } from './patient-dashboard/patient-dashboard-dialog/patient-dashboard-dialog.component';
import { PatientMenuComponent } from './patient-menu/patient-menu.component';
import { PatientHistoryComponent } from './patient-history/patient-history.component';

@NgModule({
  declarations: [
    PatientFormComponent,
    PatientListComponent,
    PatientFreeFormComponent,
    PatientTableComponent,
    PatientDetailsComponent,
    PatientFormDialogComponent,
    PatientDashboardComponent,
    PatientDashboardDialogComponent,
    PatientMenuComponent,
    PatientHistoryComponent
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,

    // CoreModule,
    FhirModule,
    VaccinationModule,
    SharedModule,

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
    LayoutModule,

  ],
  exports: [
    PatientFormComponent,
    PatientListComponent,
    PatientFreeFormComponent,
    PatientFormDialogComponent,
    PatientTableComponent,
    PatientDetailsComponent,
    PatientDashboardComponent,
    PatientMenuComponent,
    PatientHistoryComponent
  ],
})
export class PatientModule { }

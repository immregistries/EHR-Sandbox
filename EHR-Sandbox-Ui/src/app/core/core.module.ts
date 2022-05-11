import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthenticationModule } from './authentication/authentication.module';
import { SettingsService } from './_services/settings.service';
import { PatientService } from './_services/patient.service';
import { TenantService } from './_services/tenant.service';
import { FacilityService } from './_services/facility.service';
import { CodeMapsService } from './_services/code-maps.service';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { LayoutModule } from '@angular/cdk/layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NavigationComponent } from './_components/navigation/navigation.component';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { PatientFormComponent } from './_components/_forms/patient-form/patient-form.component';
import { TenantListComponent } from './_components/_lists/_card-lists/tenant-list/tenant-list.component';
import { FacilityListComponent } from './_components/_lists/_card-lists/facility-list/facility-list.component';
import { TenantCreationComponent } from './_components/_dialogs/tenant-creation/tenant-creation.component';
import { FacilityCreationComponent } from './_components/_dialogs/facility-creation/facility-creation.component';
import { PatientListComponent } from './_components/_lists/_card-lists/patient-list/patient-list.component';
import { PatientCreationComponent } from './_components/_forms/patient-form/patient-creation/patient-creation.component';
import { VaccinationFormComponent } from './_components/_forms/vaccination-form/vaccination-form.component';
import { PatientFreeFormComponent } from './_components/_forms/patient-form/patient-free-form/patient-free-form.component';
import { VaccinationCreationComponent } from './_components/_forms/vaccination-form/vaccination-creation/vaccination-creation.component';
import { VaccinationTableComponent } from './_components/_lists/_tables/vaccination-table/vaccination-table.component';
import { PatientTableComponent } from './_components/_lists/_tables/patient-table/patient-table.component';
import { TenantMenuComponent } from './_components/_lists/_menus/tenant-menu/tenant-menu.component';
import { FacilityMenuComponent } from './_components/_lists/_menus/facility-menu/facility-menu.component';
import { SettingsDialogComponent } from './_components/_dialogs/settings-dialog/settings-dialog.component';
import { HomeComponent } from './_components/home/home.component';
import { VaccinationFreeFormComponent } from './_components/_forms/vaccination-form/vaccination-free-form/vaccination-free-form.component';
import { FeedbackTableComponent } from './_components/_lists/_tables/feedback-table/feedback-table.component';
import { FeedbackDialogComponent } from './_components/_lists/_tables/feedback-table/feedback-dialog/feedback-dialog.component';
import { AppRoutingModule } from '../app-routing.module';

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
import { SharedModule } from '../shared/shared.module';
import { FhirModule } from '../fhir/fhir.module';



@NgModule({
  declarations: [
    NavigationComponent,
    HomeComponent,
    DashboardComponent,
    PatientFormComponent,
    TenantListComponent,
    FacilityListComponent,
    TenantCreationComponent,
    FacilityCreationComponent,
    PatientListComponent,
    PatientCreationComponent,
    VaccinationFormComponent,
    PatientFreeFormComponent,
    VaccinationCreationComponent,
    VaccinationTableComponent,
    PatientTableComponent,
    TenantMenuComponent,
    FacilityMenuComponent,
    SettingsDialogComponent,
    VaccinationFreeFormComponent,
    FeedbackTableComponent,
    FeedbackDialogComponent
  ],
  imports: [
    CommonModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    LayoutModule,
    ReactiveFormsModule,
    FormsModule,

    AuthenticationModule,
    FhirModule,

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
  ],
  exports: [
    AuthenticationModule,
    NavigationComponent,
  ],
  providers: [
    SettingsService,
    PatientService,
    TenantService,
    FacilityService,
    CodeMapsService,
  ]
})
export class CoreModule { }

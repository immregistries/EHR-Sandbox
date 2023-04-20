import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthenticationModule } from './authentication/authentication.module';
import { SharedModule } from '../shared/shared.module';
import { FhirModule } from '../fhir/fhir.module';

import { SettingsService } from './_services/settings.service';
import { PatientService } from './_services/patient.service';
import { TenantService } from './_services/tenant.service';
import { FacilityService } from './_services/facility.service';
import { CodeMapsService } from './_services/code-maps.service';
import { FeedbackService } from './_services/feedback.service';

import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { LayoutModule } from '@angular/cdk/layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NavigationComponent } from './_components/navigation/navigation.component';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { HomeComponent } from './_components/home/home.component';
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
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatBadgeModule } from '@angular/material/badge';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { SettingsComponent } from './_components/settings/settings.component';
import { RefreshNotificationComponent } from './_components/refresh-notification/refresh-notification.component';
import { VaccinationComparePipe } from '../shared/_pipes/vaccination-compare.pipe';

@NgModule({
  declarations: [
    NavigationComponent,
    HomeComponent,
    DashboardComponent,
    SettingsComponent,
    RefreshNotificationComponent,
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
    MatAutocompleteModule,
    MatTooltipModule,
    MatProgressBarModule,
    MatBadgeModule,
    MatButtonToggleModule,
  ],
  exports: [
    AuthenticationModule,
    // FhirModule,
    // PatientModule,
    // VaccinationModule,

    NavigationComponent,
    HomeComponent,
    DashboardComponent,
    SettingsComponent,
  ],
  providers: [
    VaccinationComparePipe,
    SettingsService,
    PatientService,
    FeedbackService,
    TenantService,
    FacilityService,
    CodeMapsService,
  ]
})
export class CoreModule { }

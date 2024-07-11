import { APP_INITIALIZER, NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthenticationModule } from './authentication/authentication.module';
import { SharedModule } from '../shared/shared.module';

import { SettingsService } from './_services/settings.service';
import { PatientService } from './_services/patient.service';
import { TenantService } from './_services/tenant.service';
import { FacilityService } from './_services/facility.service';
import { CodeMapsService } from './_services/code-maps.service';
import { FeedbackService } from './_services/feedback.service';
import { ClinicianService } from './_services/clinician.service';


import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClient, HttpClientModule } from '@angular/common/http';
import { LayoutModule } from '@angular/cdk/layout';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { NavigationComponent } from './_components/navigation/navigation.component';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { HomeComponent } from './_components/home/home.component';
import { AppRoutingModule } from '../app-routing.module';

import { RefreshNotificationComponent } from './_components/refresh-notification/refresh-notification.component';
import { VaccinationComparePipe } from '../shared/_pipes/vaccination-compare.pipe';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { RemoteGroupDashboardComponent } from './_components/remote-group-dashboard/remote-group-dashboard.component';
import { JsonDialogService } from './_services/json-dialog.service';
import { StepsComponent } from './_components/steps/steps.component';
import { FhirBulkService } from './_services/_fhir/fhir-bulk.service';
import { FhirClientService } from './_services/_fhir/fhir-client.service';
import { FhirResourceService } from './_services/_fhir/fhir-resource.service';
import { Hl7Service } from './_services/_fhir/hl7.service';
import { SubscriptionService } from './_services/_fhir/subscription.service';
import { GroupStepsComponent } from './_components/group-steps/group-steps.component';
import { firstValueFrom } from 'rxjs';



const initAppFn = (settingsService: SettingsService, codeMapsService: CodeMapsService) => {
  return () => settingsService.loadEnvConfig('/runtime-config.json').then(() => firstValueFrom(codeMapsService.load()));
};


@NgModule({
  declarations: [
    NavigationComponent,
    HomeComponent,
    DashboardComponent,
    RefreshNotificationComponent,
    RemoteGroupDashboardComponent,
    StepsComponent,
    GroupStepsComponent,
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
    SharedModule,

    // MatToolbarModule,
    // MatButtonModule,
    // MatSidenavModule,
    // MatIconModule,
    // MatListModule,
    // MatInputModule,
    // MatDatepickerModule,
    // MatGridListModule,
    // MatCardModule,
    // MatMenuModule,
    // MatTabsModule,
    // MatSnackBarModule,
    // MatNativeDateModule,
    // MatDialogModule,
    // MatTableModule,
    // MatSelectModule,
    // MatAutocompleteModule,
    // MatTooltipModule,
    // MatProgressBarModule,
    // MatBadgeModule,
    // MatButtonToggleModule,
    // MatStepperModule,
  ],
  exports: [
    AuthenticationModule,
    NavigationComponent,
    HomeComponent,
    DashboardComponent,
  ],
  providers: [
    VaccinationComparePipe,
    SettingsService,
    PatientService,
    FeedbackService,
    TenantService,
    FacilityService,
    CodeMapsService,
    ClinicianService,
    JsonDialogService,
    FhirClientService,
    FhirResourceService,
    FhirBulkService,
    Hl7Service,
    SubscriptionService,
    {
      provide: MatDialogRef,
      useValue: {}
    },
    {
      provide: MAT_DIALOG_DATA,
      useValue: {}
    },
    {
      provide: APP_INITIALIZER,
      useFactory: initAppFn,
      multi: true,
      deps: [SettingsService, CodeMapsService],
    }
  ]
})
export class CoreModule { }

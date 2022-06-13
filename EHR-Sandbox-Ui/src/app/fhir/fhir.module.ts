import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FhirService } from './_services/fhir.service';
import { Hl7Service } from './_services/hl7.service';
import { FhirDialogComponent } from './_components/fhir-dialog/fhir-dialog.component';
import { FhirGetComponent } from './_components/fhir-get/fhir-get.component';
import { FhirMessagingComponent } from './_components/fhir-messaging/fhir-messaging.component';
import { Hl7MessagingComponent } from './_components/hl7-messaging/hl7-messaging.component';
import { HttpClientModule } from '@angular/common/http';
import { MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatInputModule } from '@angular/material/input';
import { SharedModule } from '../shared/shared.module';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatTabsModule } from '@angular/material/tabs';
import { SubscriptionDashboardComponent } from './_components/subscription-dashboard/subscription-dashboard.component';

@NgModule({
  declarations: [
    Hl7MessagingComponent,
    FhirMessagingComponent,
    FhirGetComponent,
    FhirDialogComponent,
    SubscriptionDashboardComponent,
  ],
  imports: [
    CommonModule,
    HttpClientModule,
    MatDialogModule,
    MatButtonModule,
    ReactiveFormsModule,
    FormsModule,
    MatInputModule,
    MatProgressBarModule,
    MatTabsModule,

    SharedModule,
  ],
  providers: [
    FhirService,
    Hl7Service,
  ],
  exports: [
    Hl7MessagingComponent,
    FhirMessagingComponent,
    FhirGetComponent,
    FhirDialogComponent,
    SubscriptionDashboardComponent,
  ]
})
export class FhirModule { }

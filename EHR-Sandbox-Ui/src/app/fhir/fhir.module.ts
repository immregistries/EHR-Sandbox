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
import { SubscriptionTableComponent } from './_components/subscription-table/subscription-table.component';
import { MatTableModule } from '@angular/material/table';
import { MatSelectModule } from '@angular/material/select';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatCardModule } from '@angular/material/card';
import {MatCheckboxModule} from '@angular/material/checkbox';

import { SubscriptionService } from './_services/subscription.service';

@NgModule({
  declarations: [
    Hl7MessagingComponent,
    FhirMessagingComponent,
    FhirGetComponent,
    FhirDialogComponent,
    SubscriptionDashboardComponent,
    SubscriptionTableComponent,
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
    MatTableModule,
    MatSelectModule,
    MatGridListModule,
    MatCardModule,
    MatCheckboxModule,

    SharedModule,
  ],
  providers: [
    FhirService,
    Hl7Service,
    SubscriptionService,
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

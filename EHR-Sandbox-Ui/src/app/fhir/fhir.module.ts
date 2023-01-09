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
import { FhirPostComponent } from './_components/fhir-post/fhir-post.component';
import { MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import { FhirBulkComponent } from '../fhir/_components/fhir-bulk/fhir-bulk.component';

@NgModule({
  declarations: [
    Hl7MessagingComponent,
    FhirMessagingComponent,
    FhirGetComponent,
    FhirDialogComponent,
    SubscriptionDashboardComponent,
    SubscriptionTableComponent,
    FhirPostComponent,
    FhirBulkComponent,
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
    MatProgressSpinnerModule,

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
    FhirPostComponent,
    FhirBulkComponent,
  ]
})
export class FhirModule { }

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FhirService } from './_services/fhir.service';
import { Hl7Service } from './_services/hl7.service';
import { FhirGetComponent } from './_components/fhir-messaging/fhir-get/fhir-get.component';
import { FhirMessagingComponent } from './_components/fhir-messaging/fhir-messaging.component';
import { Hl7MessagingComponent } from './_components/hl7-messaging/hl7-messaging.component';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { SharedModule } from '../shared/shared.module';
import { SubscriptionDashboardComponent } from './_components/subscription-dashboard/subscription-dashboard.component';
import { SubscriptionTableComponent } from './_components/subscription-table/subscription-table.component';
import { MatGridListModule } from '@angular/material/grid-list';

import { SubscriptionService } from './_services/subscription.service';
import { FhirPostComponent } from './_components/fhir-messaging/fhir-post/fhir-post.component';
import { FhirBulkComponent } from '../fhir/_components/fhir-bulk/fhir-bulk.component';
import { MatToolbarModule } from '@angular/material/toolbar';
import { FhirBulkDashboardComponent } from '../fhir/_components/fhir-bulk-dashboard/fhir-bulk-dashboard.component';
import { FhirBulkOperationComponent } from './_components/fhir-bulk/fhir-bulk-operation/fhir-bulk-operation.component';
import { FhirBulkStatusCheckComponent } from './_components/fhir-bulk/fhir-bulk-status-check/fhir-bulk-status-check.component';
import { FhirBulkNdjsonImportComponent } from './_components/fhir-bulk/fhir-bulk-ndjson-import/fhir-bulk-ndjson-import.component';
import { FhirOperationComponent } from './_components/fhir-operation/fhir-operation.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import {MatCardModule} from '@angular/material/card';

@NgModule({
  declarations: [
    Hl7MessagingComponent,
    FhirMessagingComponent,
    FhirGetComponent,
    SubscriptionDashboardComponent,
    SubscriptionTableComponent,
    FhirPostComponent,
    FhirBulkComponent,
    FhirBulkDashboardComponent,
    FhirBulkOperationComponent,
    FhirBulkStatusCheckComponent,
    FhirBulkNdjsonImportComponent,
    FhirOperationComponent,
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
    MatToolbarModule,
    MatAutocompleteModule,
    MatDatepickerModule,

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
    SubscriptionDashboardComponent,
    FhirPostComponent,
    FhirBulkComponent,
    FhirBulkDashboardComponent,
    FhirBulkOperationComponent,
    FhirBulkStatusCheckComponent,
    FhirBulkNdjsonImportComponent,
    FhirOperationComponent,
  ]
})
export class FhirModule { }

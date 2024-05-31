import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectCodebaseComponent } from './_components/select-codebase/select-codebase.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { TenantFormComponent } from './_tenant/tenant-form/tenant-form.component';
import { FacilityFormComponent } from './_facility/facility-form/facility-form.component';
import { TenantMenuComponent } from './_tenant/tenant-menu/tenant-menu.component';
import { FacilityMenuComponent } from './_facility/facility-menu/facility-menu.component';
import { ImmunizationRegistryFormComponent } from './_immunization-registry/immunization-registry-form/immunization-registry-form.component';
import { FeedbackTableComponent } from './_data-quality-issues/feedback-table/feedback-table.component';
import { MatDividerModule } from '@angular/material/divider';
import { MatGridListModule } from '@angular/material/grid-list';
import { HoverMenuComponent } from './_components/hover-menu/hover-menu.component';
import { EmptyListMessageComponent } from './_components/empty-list-message/empty-list-message.component';
import { SelectImmunizationRegistryComponent } from './_components/select-immunization-registry/select-immunization-registry.component';
import { CodeMapsPipe } from './_pipes/code-maps.pipe';
import { TextShortenPipe } from './_pipes/text-shorten.pipe';
import { ImmunizationRegistryMenuComponent } from './_immunization-registry/immunization-registry-menu/immunization-registry-menu.component';
import { LocalCopyDialogComponent } from './_components/local-copy-dialog/local-copy-dialog.component';
import { KeysPipe } from './_pipes/keys.pipe';
import { FeedbackIconComponent } from './_data-quality-issues/feedback-icon/feedback-icon.component';
import { VaccinationComparePipe } from './_pipes/vaccination-compare.pipe';
import { VaccinationFormComponent } from './_vaccination/vaccination-form/vaccination-form.component';
import { FetchAndLoadComponent } from './_vaccination/fetch-and-load/fetch-and-load.component';
import { RecommendationComponentTableComponent } from './_vaccination/recommendation-table/recommendation-component-table/recommendation-component-table.component';
import { RecommendationTableComponent } from './_vaccination/recommendation-table/recommendation-table.component';
import { VaccinationDashboardComponent } from './_vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { VaccinationToolsComponent } from './_vaccination/vaccination-tools/vaccination-tools.component';
import { VaccinationFreeFormComponent } from './_vaccination/vaccination-form/vaccination-free-form/vaccination-free-form.component';
import { VaccinationHistoryComponent } from './_vaccination/vaccination-history/vaccination-history.component';
import { VaccinationTableComponent } from './_vaccination/vaccination-table/vaccination-table.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { PatientDashboardComponent } from './_patient/patient-dashboard/patient-dashboard.component';
import { PatientToolsComponent } from './_patient/patient-tools/patient-tools.component';
import { PatientFormComponent } from './_patient/patient-form/patient-form.component';
import { PatientFreeFormComponent } from './_patient/patient-form/patient-free-form/patient-free-form.component';
import { PatientHistoryComponent } from './_patient/patient-history/patient-history.component';
import { PatientListComponent } from './_patient/patient-list/patient-list.component';
import { PatientMenuComponent } from './_patient/patient-menu/patient-menu.component';
import { PatientTableComponent } from './_patient/patient-table/patient-table.component';
import { VaccinationReceivedTableComponent } from './_vaccination/vaccination-received-table/vaccination-received-table.component';
import { CardFormComponent } from './_components/card-form/card-form.component';
import { MatAutocompleteModule } from '@angular/material/autocomplete';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatDialogModule } from '@angular/material/dialog';
import { MatInputModule } from '@angular/material/input';
import { MatListModule } from '@angular/material/list';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatSelectModule } from '@angular/material/select';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { RegistryNamePipe } from './_pipes/registry-name.pipe';
import { MatToolbarModule } from '@angular/material/toolbar';
import { ClinicianSelectComponent } from './_clinician/clinician-select/clinician-select.component';
import { ClinicianFormComponent } from './_clinician/clinician-form/clinician-form.component';
import { RemoteGroupTableComponent } from './_components/remote-group-table/remote-group-table.component';
import { PatientMatchComponent } from './_patient/patient-match/patient-match.component';
import { PatientReceivedTableComponent } from './_patient/patient-received-table/patient-received-table.component';
import { JsonDialogComponent } from './_components/json-dialog/json-dialog.component';
import { JsonDialogButtonComponent } from './_components/json-dialog-button/json-dialog-button.component';
import { ClipboardModule } from '@angular/cdk/clipboard';
import { GroupFormComponent } from './_group/group-form/group-form.component';
import { GroupDashboardComponent } from './_group/group-dashboard/group-dashboard.component';
import { MatChipsModule } from '@angular/material/chips';
import { PatientGroupListComponent } from './_patient/patient-group-list/patient-group-list.component';
import { TabWithMenuComponent } from './_components/tab-with-menu/tab-with-menu.component';
import { AbstractDataTableComponent } from './_components/abstract-data-table/abstract-data-table.component';
import { GroupTableComponent } from './_group/group-table/group-table.component';
import { GroupAllDashboardComponent } from './_group/group-all-dashboard/group-all-dashboard.component';
import { GroupToolsComponent } from './_group/group-tools/group-tools.component';
import { CardDisplayComponent } from './_components/card-display/card-display.component';
import { PatientDisplayComponent } from './_patient/patient-display/patient-display.component';
import { VaccinationDisplayComponent } from './_vaccination/vaccination-display/vaccination-display.component';
import { FacilityDashboardComponent } from './_facility/facility-dashboard/facility-dashboard.component';
import { FacilityToolsComponent } from './_facility/facility-tools/facility-tools.component';
import { FacilityTableComponent } from './_facility/facility-table/facility-table.component';
import { ClinicianTableComponent } from './_clinician/clinician-table/clinician-table.component';
import { ClinicianToolsComponent } from './_clinician/clinician-tools/clinician-tools.component';
import { GroupBulkCardComponent } from './_group/group-bulk-card/group-bulk-card.component';
import { GroupBulkCompareComponent } from './_group/group-bulk-compare/group-bulk-compare.component';
import { VaccinationCompareComponent } from './_vaccination/vaccination-compare/vaccination-compare.component';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { RecommendationDownloadComponent } from './_vaccination/recommendation-download/recommendation-download.component';
import { MatStepperModule } from '@angular/material/stepper';
import { MatButtonToggleModule } from '@angular/material/button-toggle';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { ImmunizationRegistryDashboardComponent } from './_immunization-registry/immunization-registry-dashboard/immunization-registry-dashboard.component';
import { FhirBulkDashboardComponent } from './_fhir/fhir-bulk-dashboard/fhir-bulk-dashboard.component';
import { FhirBulkNdjsonImportComponent } from './_fhir/fhir-bulk/fhir-bulk-ndjson-import/fhir-bulk-ndjson-import.component';
import { FhirBulkNdjsonManualComponent } from './_fhir/fhir-bulk/fhir-bulk-ndjson-manual/fhir-bulk-ndjson-manual.component';
import { FhirBulkOperationComponent } from './_fhir/fhir-bulk/fhir-bulk-operation/fhir-bulk-operation.component';
import { FhirBulkStatusCheckComponent } from './_fhir/fhir-bulk/fhir-bulk-status-check/fhir-bulk-status-check.component';
import { FhirBulkComponent } from './_fhir/fhir-bulk/fhir-bulk.component';
import { FhirGetComponent } from './_fhir/fhir-messaging/fhir-get/fhir-get.component';
import { FhirMessagingComponent } from './_fhir/fhir-messaging/fhir-messaging.component';
import { FhirPostComponent } from './_fhir/fhir-messaging/fhir-post/fhir-post.component';
import { FhirOperationComponent } from './_fhir/fhir-operation/fhir-operation.component';
import { Hl7MessagingComponent } from './_fhir/hl7-messaging/hl7-messaging.component';
import { Hl7PostComponent } from './_fhir/hl7-post/hl7-post.component';
import { SubscriptionDashboardComponent } from './_fhir/subscription-dashboard/subscription-dashboard.component';
import { SubscriptionTableComponent } from './_fhir/subscription-table/subscription-table.component';
import { IdentifierListFormComponent } from './_components/card-form/generic-list-form/identifier-list-form/identifier-list-form.component';
import { VxuStepsComponent } from './_components/vxu-steps/vxu-steps.component';
import { FirstStepsComponent } from './_components/first-steps/first-steps.component';
import { MatTreeModule } from '@angular/material/tree';
import { PhoneListFormComponent } from './_components/card-form/generic-list-form/phone-list-form/phone-list-form.component';
import { NextOfKinListFormComponent } from './_components/card-form/generic-list-form/next-of-kin-list-form/next-of-kin-list-form.component';
import { GenericListFormComponent } from './_components/card-form/generic-list-form/generic-list-form.component';
import { RaceListFormComponent } from './_components/card-form/generic-list-form/race-list-form/race-list-form.component';
import { AddressListFormComponent } from './_components/card-form/generic-list-form/address-list-form/address-list-form.component';

@NgModule({
  declarations: [
    SelectCodebaseComponent,
    TenantFormComponent,
    FacilityFormComponent,
    TenantMenuComponent,
    FacilityMenuComponent,
    ImmunizationRegistryFormComponent,
    FeedbackTableComponent,
    HoverMenuComponent,
    EmptyListMessageComponent,
    SelectImmunizationRegistryComponent,
    CodeMapsPipe,
    TextShortenPipe,

    ImmunizationRegistryMenuComponent,
    LocalCopyDialogComponent,
    KeysPipe,
    FeedbackIconComponent,
    VaccinationComparePipe,

    VaccinationFormComponent,
    VaccinationTableComponent,
    VaccinationFreeFormComponent,
    VaccinationDashboardComponent,
    VaccinationToolsComponent,
    VaccinationHistoryComponent,
    RecommendationTableComponent,
    RecommendationComponentTableComponent,
    FetchAndLoadComponent,

    PatientFormComponent,
    PatientListComponent,
    PatientFreeFormComponent,
    PatientTableComponent,
    PatientToolsComponent,
    PatientDashboardComponent,
    PatientMenuComponent,
    PatientHistoryComponent,
    VaccinationReceivedTableComponent,
    CardFormComponent,
    RegistryNamePipe,
    ClinicianSelectComponent,
    ClinicianFormComponent,
    RemoteGroupTableComponent,
    PatientMatchComponent,
    PatientReceivedTableComponent,
    JsonDialogComponent,
    JsonDialogButtonComponent,
    GroupFormComponent,
    GroupDashboardComponent,
    PatientGroupListComponent,
    TabWithMenuComponent,
    AbstractDataTableComponent,
    GroupTableComponent,
    GroupAllDashboardComponent,
    GroupToolsComponent,
    CardDisplayComponent,
    PatientDisplayComponent,
    VaccinationDisplayComponent,
    FacilityDashboardComponent,
    FacilityToolsComponent,
    FacilityTableComponent,
    ClinicianTableComponent,
    ClinicianToolsComponent,
    GroupBulkCardComponent,
    GroupBulkCompareComponent,
    VaccinationCompareComponent,
    RecommendationDownloadComponent,
    ImmunizationRegistryDashboardComponent,
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
    FhirBulkNdjsonManualComponent,
    Hl7PostComponent,
    IdentifierListFormComponent,
    VxuStepsComponent,
    FirstStepsComponent,
    PhoneListFormComponent,
    NextOfKinListFormComponent,
    GenericListFormComponent,
    RaceListFormComponent,
    AddressListFormComponent,
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
    MatButtonModule,
    MatSelectModule,
    MatCheckboxModule,
    MatTabsModule,
    MatDatepickerModule,
    MatButtonModule,
    MatIconModule,
    MatListModule,
    MatInputModule,
    MatDatepickerModule,
    MatGridListModule,
    MatCardModule,
    MatMenuModule,
    MatTabsModule,
    MatDialogModule,
    MatTableModule,
    MatSelectModule,
    MatAutocompleteModule,
    MatTooltipModule,
    MatProgressBarModule,
    MatBadgeModule,
    MatToolbarModule,
    MatChipsModule,
    ClipboardModule,
    MatSlideToggleModule,
    MatStepperModule,
    MatTreeModule,
  ],
  exports: [
    ReactiveFormsModule,
    FormsModule,
    MatInputModule,
    MatAutocompleteModule,
    MatBadgeModule,
    MatTooltipModule,
    MatIconModule,
    ClipboardModule,
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
    MatStepperModule,


    SelectCodebaseComponent,
    TenantFormComponent,
    FacilityFormComponent,
    TenantMenuComponent,
    FacilityMenuComponent,
    ImmunizationRegistryFormComponent,
    FeedbackTableComponent,
    HoverMenuComponent,
    EmptyListMessageComponent,
    SelectImmunizationRegistryComponent,

    VaccinationFormComponent,
    VaccinationTableComponent,
    VaccinationFreeFormComponent,
    VaccinationDashboardComponent,
    VaccinationToolsComponent,
    VaccinationHistoryComponent,
    RecommendationTableComponent,
    RecommendationComponentTableComponent,
    FetchAndLoadComponent,

    PatientFormComponent,
    PatientListComponent,
    PatientFreeFormComponent,
    PatientTableComponent,
    PatientToolsComponent,
    PatientDashboardComponent,
    PatientMenuComponent,
    PatientHistoryComponent,
    PatientReceivedTableComponent,

    CodeMapsPipe,
    TextShortenPipe,
    RegistryNamePipe,

    ImmunizationRegistryMenuComponent,
    LocalCopyDialogComponent,
    KeysPipe,
    FeedbackIconComponent,
    CardFormComponent,
    ClinicianSelectComponent,
    ClinicianFormComponent,
    RemoteGroupTableComponent,
    TabWithMenuComponent,
    ImmunizationRegistryDashboardComponent,
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
    GroupAllDashboardComponent,
    GroupFormComponent,
    MatTreeModule,

  ],
})
export class SharedModule { }

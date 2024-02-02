import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SelectCodebaseComponent } from './_components/select-codebase/select-codebase.component';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { MatBadgeModule } from '@angular/material/badge';
import { MatIconModule } from '@angular/material/icon';
import { FacilityListComponent } from './_components/_lists/_card-lists/facility-list/facility-list.component';
import { TenantListComponent } from './_components/_lists/_card-lists/tenant-list/tenant-list.component';
import { TenantCreationComponent } from './_components/_dialogs/tenant-creation/tenant-creation.component';
import { FacilityCreationComponent } from './_components/_dialogs/facility-creation/facility-creation.component';
import { TenantMenuComponent } from './_components/_menus/tenant-menu/tenant-menu.component';
import { FacilityMenuComponent } from './_components/_menus/facility-menu/facility-menu.component';
import { SettingsDialogComponent } from './_components/_dialogs/settings-dialog/settings-dialog.component';
import { FeedbackTableComponent } from './_components/feedback-table/feedback-table.component';
import { MatDividerModule } from '@angular/material/divider';
import { MatGridListModule } from '@angular/material/grid-list';
import { HoverMenuComponent } from './_components/_menus/hover-menu/hover-menu.component';
import { EmptyListMessageComponent } from './_components/empty-list-message/empty-list-message.component';
import { SelectImmregistryComponent } from './_components/select-immregistry/select-immregistry.component';
import { CodeMapsPipe } from './_pipes/code-maps.pipe';
import { TextShortenPipe } from './_pipes/text-shorten.pipe';
import { ImmunizationRegistryMenuComponent } from './_components/_menus/immunization-registry-menu/immunization-registry-menu.component';
import { LocalCopyDialogComponent } from './_components/_dialogs/local-copy-dialog/local-copy-dialog.component';
import { KeysPipe } from './_pipes/keys.pipe';
import { FeedbackIconComponent } from './_components/feedback-icon/feedback-icon.component';
import { VaccinationComparePipe } from './_pipes/vaccination-compare.pipe';
import { VaccinationFormComponent } from './_vaccination/vaccination-form/vaccination-form.component';
import { FetchAndLoadComponent } from './_vaccination/fetch-and-load/fetch-and-load.component';
import { RecommendationComponentTableComponent } from './_vaccination/recommendation-table/recommendation-component-table/recommendation-component-table.component';
import { RecommendationTableComponent } from './_vaccination/recommendation-table/recommendation-table.component';
import { VaccinationDashboardComponent } from './_vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { VaccinationDetailsComponent } from './_vaccination/vaccination-details/vaccination-details.component';
import { VaccinationFreeFormComponent } from './_vaccination/vaccination-form/vaccination-free-form/vaccination-free-form.component';
import { VaccinationHistoryComponent } from './_vaccination/vaccination-history/vaccination-history.component';
import { VaccinationTableComponent } from './_vaccination/vaccination-table/vaccination-table.component';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { PatientDashboardComponent } from './_patient/patient-dashboard/patient-dashboard.component';
import { PatientDetailsComponent } from './_patient/patient-details/patient-details.component';
import { PatientFormComponent } from './_patient/patient-form/patient-form.component';
import { PatientFreeFormComponent } from './_patient/patient-free-form/patient-free-form.component';
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
import {ClipboardModule} from '@angular/cdk/clipboard';
import { GroupFormComponent } from './_local-group/group-form/group-form.component';
import { GroupDashboardComponent } from './_local-group/group-dashboard/group-dashboard.component';
import {MatChipsModule} from '@angular/material/chips';
import { PatientGroupListComponent } from './_patient/patient-group-list/patient-group-list.component';


@NgModule({
  declarations: [
    SelectCodebaseComponent,
    TenantListComponent,
    FacilityListComponent,
    TenantCreationComponent,
    FacilityCreationComponent,
    TenantMenuComponent,
    FacilityMenuComponent,
    SettingsDialogComponent,
    FeedbackTableComponent,
    HoverMenuComponent,
    EmptyListMessageComponent,
    SelectImmregistryComponent,
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
    VaccinationDetailsComponent,
    VaccinationHistoryComponent,
    RecommendationTableComponent,
    RecommendationComponentTableComponent,
    FetchAndLoadComponent,

    PatientFormComponent,
    PatientListComponent,
    PatientFreeFormComponent,
    PatientTableComponent,
    PatientDetailsComponent,
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
  ],
  exports: [
    ReactiveFormsModule,
    FormsModule,
    MatInputModule,
    MatAutocompleteModule,
    MatBadgeModule,
    MatTooltipModule,
    MatIconModule,

    SelectCodebaseComponent,
    TenantListComponent,
    FacilityListComponent,
    TenantCreationComponent,
    FacilityCreationComponent,
    TenantMenuComponent,
    FacilityMenuComponent,
    SettingsDialogComponent,
    FeedbackTableComponent,
    HoverMenuComponent,
    EmptyListMessageComponent,
    SelectImmregistryComponent,

    VaccinationFormComponent,
    VaccinationTableComponent,
    VaccinationFreeFormComponent,
    VaccinationDashboardComponent,
    VaccinationDetailsComponent,
    VaccinationHistoryComponent,
    RecommendationTableComponent,
    RecommendationComponentTableComponent,
    FetchAndLoadComponent,

    PatientFormComponent,
    PatientListComponent,
    PatientFreeFormComponent,
    PatientTableComponent,
    PatientDetailsComponent,
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

  ],
})
export class SharedModule { }

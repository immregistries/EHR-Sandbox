import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './core/_components/dashboard/dashboard.component';
import { HomeComponent } from './core/_components/home/home.component';
import { AuthenticationDialogComponent } from './core/authentication/_components/authentication-form/authentication-dialog/authentication-dialog.component';
import { FhirMessagingComponent } from './shared/_fhir/fhir-messaging/fhir-messaging.component';
import { FeedbackTableComponent } from './shared/_data-quality-issues/feedback-table/feedback-table.component';
import { SubscriptionDashboardComponent } from './shared/_fhir/subscription-dashboard/subscription-dashboard.component';
import { ImmunizationRegistryDashboardComponent } from './shared/_immunization-registry/immunization-registry-dashboard/immunization-registry-dashboard.component';
import { FhirBulkDashboardComponent } from './shared/_fhir/fhir-bulk-dashboard/fhir-bulk-dashboard.component';
import { RemoteGroupDashboardComponent } from './core/_components/remote-group-dashboard/remote-group-dashboard.component';
import { GroupDashboardComponent } from './shared/_group/group-dashboard/group-dashboard.component';
import { GroupAllDashboardComponent } from './shared/_group/group-all-dashboard/group-all-dashboard.component';
import { FacilityDashboardComponent } from './shared/_facility/facility-dashboard/facility-dashboard.component';
import { StepsComponent } from './core/_components/steps/steps.component';
import { ClinicianTableComponent } from './shared/_clinician/clinician-table/clinician-table.component';
import { Hl7MessagingComponent } from './shared/_fhir/hl7-messaging/hl7-messaging.component';
import { GroupStepsComponent } from './core/_components/group-steps/group-steps.component';
import { VxuStepsComponent } from './shared/_components/vxu-steps/vxu-steps.component';
import { JsonFormComponent } from './shared/_components/abstract-json-form/abstract-json-form.component';
import { PatientJsonFormComponent } from './shared/_components/abstract-json-form/patient-json-form/patient-json-form.component';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'authentication', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'settings', component: ImmunizationRegistryDashboardComponent },
  { path: 'login', component: AuthenticationDialogComponent },
  { path: 'fhir', component: FhirMessagingComponent },
  { path: 'hl7', component: Hl7MessagingComponent },
  { path: 'feedback', component: FeedbackTableComponent },
  { path: 'subscription', component: SubscriptionDashboardComponent },
  { path: 'bulk', component: FhirBulkDashboardComponent },
  // { path: 'clinicians', component: ClinicianFormComponent },
  { path: 'remote-groups', component: RemoteGroupDashboardComponent },
  { path: 'group', component: GroupDashboardComponent },
  { path: 'groups', component: GroupAllDashboardComponent },
  { path: 'facilities', component: FacilityDashboardComponent },
  { path: 'clinicians', component: ClinicianTableComponent },
  { path: 'first-steps', component: StepsComponent },
  { path: 'group-steps', component: GroupStepsComponent },
  { path: 'vxu-steps', component: VxuStepsComponent },
  { path: 'json-form', component: PatientJsonFormComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

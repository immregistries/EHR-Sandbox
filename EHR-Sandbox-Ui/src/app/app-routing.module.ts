import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './core/_components/dashboard/dashboard.component';
import { HomeComponent } from './core/_components/home/home.component';
import { AuthenticationDialogComponent } from './core/authentication/_components/authentication-form/authentication-dialog/authentication-dialog.component';
import { FhirMessagingComponent } from './fhir/_components/fhir-messaging/fhir-messaging.component';
import { FeedbackTableComponent } from './shared/_data-quality-issues/feedback-table/feedback-table.component';
import { SubscriptionDashboardComponent } from './fhir/_components/subscription-dashboard/subscription-dashboard.component';
import { ImmunizationRegistryDashboardComponent } from './core/_components/immunization-registry-dashboard/immunization-registry-dashboard.component';
import { FhirBulkDashboardComponent } from './fhir/_components/fhir-bulk-dashboard/fhir-bulk-dashboard.component';
import { RemoteGroupDashboardComponent } from './core/_components/remote-group-dashboard/remote-group-dashboard.component';
import { GroupDashboardComponent } from './shared/_group/group-dashboard/group-dashboard.component';
import { GroupAllDashboardComponent } from './shared/_group/group-all-dashboard/group-all-dashboard.component';
import { FacilityDashboardComponent } from './shared/_facility/facility-dashboard/facility-dashboard.component';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'authentication', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'settings', component: ImmunizationRegistryDashboardComponent },
  { path: 'login', component: AuthenticationDialogComponent },
  { path: 'fhir', component: FhirMessagingComponent },
  { path: 'feedback', component: FeedbackTableComponent },
  { path: 'subscription', component: SubscriptionDashboardComponent },
  { path: 'bulk', component: FhirBulkDashboardComponent },
  // { path: 'clinicians', component: ClinicianFormComponent },
  { path: 'remote-groups', component: RemoteGroupDashboardComponent },
  { path: 'group', component: GroupDashboardComponent },
  { path: 'groups', component: GroupAllDashboardComponent },
  { path: 'facilities', component: FacilityDashboardComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

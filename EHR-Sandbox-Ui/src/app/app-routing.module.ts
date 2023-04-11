import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './core/_components/dashboard/dashboard.component';
import { HomeComponent } from './core/_components/home/home.component';
import { AuthenticationDialogComponent } from './core/authentication/_components/authentication-form/authentication-dialog/authentication-dialog.component';
import { FhirMessagingComponent } from './fhir/_components/fhir-messaging/fhir-messaging.component';
import { SettingsDialogComponent } from './shared/_components/_dialogs/settings-dialog/settings-dialog.component';
import { PatientFormComponent } from './shared/_patient/patient-form/patient-form.component';
import { VaccinationFormComponent } from './shared/_vaccination/vaccination-form/vaccination-form.component';
import { FeedbackTableComponent } from './shared/_components/feedback-table/feedback-table.component';
import { SubscriptionDashboardComponent } from './fhir/_components/subscription-dashboard/subscription-dashboard.component';
import { SettingsComponent } from './core/_components/settings/settings.component';
import { FhirBulkDashboardComponent } from './fhir/_components/fhir-bulk-dashboard/fhir-bulk-dashboard.component';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'authentication', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'patient_form', component: PatientFormComponent },
  { path: 'vaccination_form', component: VaccinationFormComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'login', component: AuthenticationDialogComponent },
  { path: 'fhir', component: FhirMessagingComponent },
  { path: 'feedback', component: FeedbackTableComponent },
  { path: 'subscription', component: SubscriptionDashboardComponent },
  { path: 'bulk', component: FhirBulkDashboardComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

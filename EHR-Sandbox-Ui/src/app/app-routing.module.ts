import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './core/_components/dashboard/dashboard.component';
import { HomeComponent } from './core/_components/home/home.component';
import { AuthenticationDialogComponent } from './core/authentication/_components/authentication-form/authentication-dialog/authentication-dialog.component';
import { FhirMessagingComponent } from './fhir/_components/fhir-messaging/fhir-messaging.component';
import { SettingsDialogComponent } from './core/_components/_dialogs/settings-dialog/settings-dialog.component';
import { PatientFormComponent } from './core/_components/_forms/patient-form/patient-form.component';
import { VaccinationFormComponent } from './core/_components/_forms/vaccination-form/vaccination-form.component';
import { FeedbackTableComponent } from './core/_components/_lists/_tables/feedback-table/feedback-table.component';

const routes: Routes = [
  { path: '', redirectTo: '/home', pathMatch: 'full' },
  { path: 'authentication', redirectTo: '/home', pathMatch: 'full' },
  { path: 'home', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'patient_form', component: PatientFormComponent },
  { path: 'vaccination_form', component: VaccinationFormComponent },
  { path: 'settings', component: SettingsDialogComponent },
  { path: 'login', component: AuthenticationDialogComponent },
  { path: 'fhir', component: FhirMessagingComponent },
  { path: 'feedback', component: FeedbackTableComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { useHash: true })],
  exports: [RouterModule]
})
export class AppRoutingModule { }

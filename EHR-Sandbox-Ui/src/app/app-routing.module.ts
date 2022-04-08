import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { HomeComponent } from './_components/home/home.component';
import { AuthenticationDialogComponent } from './_components/_forms/authentication-form/authentication-dialog/authentication-dialog.component';
import { FhirGetComponent } from './_components/fhir-messaging/fhir-get/fhir-get.component';
import { FhirMessagingComponent } from './_components/fhir-messaging/fhir-messaging.component';
import { SettingsDialogComponent } from './_components/_dialogs/settings-dialog/settings-dialog.component';
import { AuthenticationFormComponent } from './_components/_forms/authentication-form/authentication-form.component';
import { PatientFormComponent } from './_components/_forms/patient-form/patient-form.component';
import { VaccinationFormComponent } from './_components/_forms/vaccination-form/vaccination-form.component';

const routes: Routes = [
  { path: 'home', component: HomeComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'patient_form', component: PatientFormComponent },
  { path: 'vaccination_form', component: VaccinationFormComponent },
  { path: 'settings', component: SettingsDialogComponent },
  { path: 'login', component: AuthenticationDialogComponent },
  { path: 'fhir', component: FhirMessagingComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

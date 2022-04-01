import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { SettingsDialogComponent } from './_components/_dialogs/settings-dialog/settings-dialog.component';
import { PatientFormComponent } from './_components/_forms/patient-form/patient-form.component';
import { VaccinationFormComponent } from './_components/_forms/vaccination-form/vaccination-form.component';

const routes: Routes = [
  { path: 'home', component: DashboardComponent },
  { path: 'patient_form', component: PatientFormComponent },
  { path: 'vaccination_form', component: VaccinationFormComponent },
  { path: 'settings', component: SettingsDialogComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

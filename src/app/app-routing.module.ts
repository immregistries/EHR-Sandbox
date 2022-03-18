import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticationFormComponent } from './_components/authentication-form/authentication-form.component';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { NavigationComponent } from './_components/navigation/navigation.component';
import { PatientFormComponent } from './_components/_forms/patient-form/patient-form.component';

const routes: Routes = [
  { path: 'home', component: DashboardComponent },
  { path: 'patient_form', component: PatientFormComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

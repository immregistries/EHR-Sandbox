import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthenticationFormComponent } from './_components/authentication-form/authentication-form.component';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { NavigationComponent } from './_components/navigation/navigation.component';

const routes: Routes = [
  { path: 'home', component: DashboardComponent },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }

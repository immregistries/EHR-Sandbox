import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { LayoutModule } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import { MatInputModule } from '@angular/material/input';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatNativeDateModule } from '@angular/material/core';
import {MatDialogModule} from '@angular/material/dialog';
import {MatTableModule} from '@angular/material/table';
import {MatSelectModule} from '@angular/material/select';

import { TenantService } from './_services/tenant.service';
import { FacilityService } from './_services/facility.service';
import { PatientService } from './_services/patient.service';
import { SettingsService } from './_services/settings.service';
import { authInterceptorProviders } from './_interceptors/auth.interceptor';

import { PatientFormComponent } from './_components/_forms/patient-form/patient-form.component';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { AuthenticationFormComponent } from './_components/_forms/authentication-form/authentication-form.component';
import { NavigationComponent } from './_components/navigation/navigation.component';

import { FacilityCreationComponent } from './_components/_dialogs/facility-creation/facility-creation.component';
import { PatientCreationComponent } from './_components/_dialogs/patient-creation/patient-creation.component';
import { TenantCreationComponent } from './_components/_dialogs/tenant-creation/tenant-creation.component';

import { TenantListComponent } from './_components/_lists/tenant-list/tenant-list.component';
import { FacilityListComponent } from './_components/_lists/facility-list/facility-list.component';
import { PatientListComponent } from './_components/_lists/patient-list/patient-list.component';
import { VaccinationFormComponent } from './_components/_forms/vaccination-form/vaccination-form.component';
import { PatientFreeFormComponent } from './_components/_forms/patient-free-form/patient-free-form.component';
import { CodeMapsService, CodeMapsServiceFactory } from './_services/code-maps.service';
import { VaccinationCreationComponent } from './_components/_dialogs/vaccination-creation/vaccination-creation.component';
import { VaccinationTableComponent } from './_components/_lists/vaccination-table/vaccination-table.component';


@NgModule({
  declarations: [
    AppComponent,
    NavigationComponent,
    DashboardComponent,
    AuthenticationFormComponent,
    PatientFormComponent,
    TenantListComponent,
    FacilityListComponent,
    TenantCreationComponent,
    FacilityCreationComponent,
    PatientListComponent,
    PatientCreationComponent,
    VaccinationFormComponent,
    PatientFreeFormComponent,
    VaccinationCreationComponent,
    VaccinationTableComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    LayoutModule,
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
    ReactiveFormsModule,
    FormsModule,
    MatTabsModule,
    MatSnackBarModule,
    MatNativeDateModule,
    MatDialogModule,
    MatTableModule,
    MatSelectModule,
  ],
  providers: [
    SettingsService,
    PatientService,
    TenantService,
    FacilityService,
    CodeMapsService,
    // { provide: APP_INITIALIZER, useFactory: CodeMapsServiceFactory, deps: [CodeMapsService], multi: true },
    authInterceptorProviders,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

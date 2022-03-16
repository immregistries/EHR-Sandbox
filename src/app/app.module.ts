import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { NavigationComponent } from './_components/navigation/navigation.component';
import { HttpClientModule } from '@angular/common/http';
import { LayoutModule } from '@angular/cdk/layout';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';
import { MatSidenavModule } from '@angular/material/sidenav';
import { MatIconModule } from '@angular/material/icon';
import { MatListModule } from '@angular/material/list';
import {MatInputModule} from '@angular/material/input';
import {MatDatepickerModule} from '@angular/material/datepicker';
import { DashboardComponent } from './_components/dashboard/dashboard.component';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatCardModule } from '@angular/material/card';
import { MatMenuModule } from '@angular/material/menu';
import { AuthenticationFormComponent } from './_components/authentication-form/authentication-form.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { SettingsService } from './_services/settings.service';
import { PatientFormComponent } from './_components/patient-form/patient-form.component';
import { PatientService } from './_services/patient.service';
import { MatTabsModule } from '@angular/material/tabs';
import { MatSnackBarModule } from '@angular/material/snack-bar';
import { MatNativeDateModule } from '@angular/material/core';
import { TenantListComponent } from './_components/_lists/tenant-list/tenant-list.component';
import { FacilityListComponent } from './_components/_lists/facility-list/facility-list.component';
import {MatDialogModule} from '@angular/material/dialog';
import { TenantCreationComponent } from './_components/_dialogs/tenant-creation/tenant-creation.component';
import { authInterceptorProviders } from './_interceptors/auth.interceptor';
import { TenantService } from './_services/tenant.service';
import { FacilityService } from './_services/facility.service';
import { FacilityCreationComponent } from './_components/_dialogs/facility-creation/facility-creation.component';
import { PatientListComponent } from './_components/_lists/patient-list/patient-list.component';
import { PatientCreationComponent } from './_components/_dialogs/patient-creation/patient-creation.component';
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
    PatientCreationComponent
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
  ],
  providers: [
    SettingsService,
    PatientService,
    TenantService,
    FacilityService,
    authInterceptorProviders,
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

import { APP_INITIALIZER, NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { LayoutModule } from '@angular/cdk/layout';

import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { FhirModule } from './fhir/fhir.module';
import { CoreModule } from './core/core.module';
import { PatientModule } from './patient/patient.module';
import { VaccinationModule } from './vaccination/vaccination.module';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    BrowserAnimationsModule,
    HttpClientModule,
    LayoutModule,
    ReactiveFormsModule,
    FormsModule,

    CoreModule,
    FhirModule,
    PatientModule,
    VaccinationModule,
  ],
  providers: [
    // { provide: APP_INITIALIZER, useFactory: CodeMapsServiceFactory, deps: [CodeMapsService], multi: true },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }

import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthenticationFormComponent } from './_components/authentication-form/authentication-form.component';
import { AuthenticationDialogComponent } from './_components/authentication-form/authentication-dialog/authentication-dialog.component';
import { authInterceptorProviders } from './auth.interceptor';
import { AuthService } from './_services/auth.service';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { HttpClientModule } from '@angular/common/http';
import { MatInputModule } from '@angular/material/input';
import { SharedModule } from '../../shared/shared.module';
import { MatButtonModule } from '@angular/material/button';



@NgModule({
  declarations: [
    AuthenticationFormComponent,
    AuthenticationDialogComponent,
  ],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    FormsModule,
    HttpClientModule,
    MatInputModule,
    MatButtonModule,

    SharedModule,
  ],
  exports: [
    // ReactiveFormsModule,
    // FormsModule,
    // HttpClientModule,
    // MatInputModule,
    // MatButtonModule,

    AuthenticationFormComponent,
    AuthenticationDialogComponent,
  ],
  providers: [
    AuthService,
    authInterceptorProviders,
  ]
})
export class AuthenticationModule { }

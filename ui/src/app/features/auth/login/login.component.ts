import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {ActivatedRoute} from '@angular/router';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {CardModule} from 'primeng/card';
import {InputTextModule} from 'primeng/inputtext';
import {MessageModule} from 'primeng/message';
import {PasswordModule} from 'primeng/password';
import {ThemeService} from '../../../core/services';
import {AuthActions} from '../../../core/store/auth/auth.actions';
import {selectAuthError, selectAuthLoading} from '../../../core/store/auth/auth.selectors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    ButtonModule,
    CardModule,
    InputTextModule,
    MessageModule,
    PasswordModule,
  ],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="login-page">
      <p-button
        class="theme-toggle"
        [icon]="theme.darkMode() ? 'pi pi-sun' : 'pi pi-moon'"
        [text]="true"
        [rounded]="true"
        severity="secondary"
        ariaLabel="Toggle color scheme"
        (onClick)="theme.toggleDarkMode()"
      />

      <p-card styleClass="login-card">
        <ng-template #header>
          <div class="login-header">
            <i class="pi pi-shield"></i>
            <h1>EHR Sandbox</h1>
            <p>Sign in to continue</p>
          </div>
        </ng-template>

        <form [formGroup]="form" (ngSubmit)="submit()">
          @if (error(); as message) {
            <p-message severity="error" [text]="message" styleClass="full-width" />
          }

          <div class="field">
            <label for="username">Username</label>
            <input
              pInputText
              id="username"
              formControlName="username"
              autocomplete="username"
              placeholder="Enter username"
            />
          </div>

          <div class="field">
            <label for="password">Password</label>
            <p-password
              inputId="password"
              formControlName="password"
              [feedback]="false"
              [toggleMask]="true"
              placeholder="Enter password"
              styleClass="full-width"
              inputStyleClass="full-width"
            />
          </div>

          <p-button
            type="submit"
            label="Sign in"
            icon="pi pi-sign-in"
            [loading]="loading()"
            [disabled]="form.invalid"
            styleClass="full-width"
          />
        </form>

        <p class="disclaimer">
          <i class="pi pi-exclamation-triangle"></i>
          Test system — do not submit production or real patient data.
        </p>
      </p-card>
    </div>
  `,
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly store = inject(Store);
  private readonly fb = inject(FormBuilder);
  private readonly route = inject(ActivatedRoute);
  readonly theme = inject(ThemeService);

  readonly loading = this.store.selectSignal(selectAuthLoading);
  readonly error = this.store.selectSignal(selectAuthError);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    // Preserve the page the guard bounced the user away from.
    const redirectTo = this.route.snapshot.queryParams['redirectTo'];
    this.store.dispatch(
      AuthActions.login({user: this.form.getRawValue(), ...(redirectTo ? {redirectTo} : {})}),
    );
  }
}

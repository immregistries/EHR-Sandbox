import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {FormBuilder, ReactiveFormsModule, Validators} from '@angular/forms';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {InputTextModule} from 'primeng/inputtext';
import {AuthActions} from '../../../core/store/auth/auth.actions';
import {selectAuthError, selectAuthLoading} from '../../../core/store/auth/auth.selectors';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [ReactiveFormsModule, ButtonModule, InputTextModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <form class="login" [formGroup]="form" (ngSubmit)="submit()">
      <h1>EHR Sandbox</h1>
      <input pInputText formControlName="username" placeholder="Username" autocomplete="username" />
      <input
        pInputText
        type="password"
        formControlName="password"
        placeholder="Password"
        autocomplete="current-password"
      />
      @if (error()) {
        <small class="error">{{ error() }}</small>
      }
      <p-button type="submit" label="Log in" [loading]="loading()" [disabled]="form.invalid" />
    </form>
  `,
  styles: `
    .login {
      display: flex;
      flex-direction: column;
      gap: 1rem;
      max-width: 320px;
      margin: 10vh auto;
    }
    .error {
      color: var(--app-danger);
    }
  `,
})
export class LoginComponent {
  private readonly store = inject(Store);
  private readonly fb = inject(FormBuilder);

  readonly loading = this.store.selectSignal(selectAuthLoading);
  readonly error = this.store.selectSignal(selectAuthError);

  readonly form = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  submit(): void {
    if (this.form.valid) {
      this.store.dispatch(AuthActions.login({user: this.form.getRawValue()}));
    }
  }
}

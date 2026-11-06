import {ChangeDetectionStrategy, Component, inject, OnInit} from '@angular/core';
import {RouterOutlet} from '@angular/router';
import {Store} from '@ngrx/store';
import {ToastModule} from 'primeng/toast';
import {ThemeService} from './core/services';
import {AuthActions} from './core/store/auth/auth.actions';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ToastModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <!-- Keyed outlet for interceptor errors, plus a default one for
         everything dispatched without a key. -->
    <p-toast key="globalError" position="bottom-right" />
    <p-toast position="bottom-right" />
    <router-outlet />
  `,
})
export class AppComponent implements OnInit {
  private readonly store = inject(Store);
  // Instantiated eagerly so the stored color scheme is applied on boot.
  private readonly theme = inject(ThemeService);

  ngOnInit(): void {
    // The auth guard also asks for this; the effect ignores a duplicate
    // while one probe is in flight. Dispatching here too means the login
    // page knows about an existing session without waiting for a guard.
    this.store.dispatch(AuthActions.restoreSession());
  }
}

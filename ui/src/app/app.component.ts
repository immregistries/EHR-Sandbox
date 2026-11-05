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
    <p-toast key="globalError" position="bottom-right" />
    <router-outlet />
  `,
})
export class AppComponent implements OnInit {
  private readonly store = inject(Store);
  // Instantiated eagerly so the stored color scheme is applied on boot.
  private readonly theme = inject(ThemeService);

  ngOnInit(): void {
    this.store.dispatch(AuthActions.restoreSession());
  }
}

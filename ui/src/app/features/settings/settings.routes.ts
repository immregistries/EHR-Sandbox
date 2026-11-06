import {Routes} from '@angular/router';

export const SETTINGS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./settings-page/settings-page.component').then((m) => m.SettingsPageComponent),
  },
];

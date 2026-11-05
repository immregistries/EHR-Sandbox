import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {ApplicationConfig, isDevMode, provideZoneChangeDetection} from '@angular/core';
import {provideAnimationsAsync} from '@angular/platform-browser/animations/async';
import {provideRouter, withComponentInputBinding, withRouterConfig} from '@angular/router';
import {provideEffects} from '@ngrx/effects';
import {provideRouterStore} from '@ngrx/router-store';
import {provideStore} from '@ngrx/store';
import {provideStoreDevtools} from '@ngrx/store-devtools';
import {MessageService} from 'primeng/api';
import {providePrimeNG} from 'primeng/config';
import {DialogService} from 'primeng/dynamicdialog';

import {routes} from './app.routes';
import {authInterceptor} from './core/interceptors/auth.interceptor';
import {baseUrlInterceptor} from './core/interceptors/base-url.interceptor';
import {errorInterceptor} from './core/interceptors/error.interceptor';
import {metaReducers, reducers} from './core/store';
import {appEffects} from './core/store/effects';
import {EhrPreset} from './styles/theme-preset';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(
      routes,
      withComponentInputBinding(),
      // Nested :tenantId / :facilityId params must be visible to child routes.
      withRouterConfig({paramsInheritanceStrategy: 'always'}),
    ),
    provideHttpClient(withInterceptors([baseUrlInterceptor, authInterceptor, errorInterceptor])),
    provideAnimationsAsync(),
    provideStore(reducers, {metaReducers}),
    provideEffects(appEffects),
    provideRouterStore(),
    provideStoreDevtools({maxAge: 25, logOnly: !isDevMode(), connectInZone: true}),
    MessageService,
    DialogService,
    providePrimeNG({
      theme: {
        preset: EhrPreset,
        options: {
          darkModeSelector: '.app-dark',
          cssLayer: {name: 'primeng', order: 'theme, base, primeng, app'},
        },
      },
    }),
  ],
};

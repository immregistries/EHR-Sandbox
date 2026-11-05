import {createActionGroup, emptyProps, props} from '@ngrx/store';
import {User} from '../../models';

export const AuthActions = createActionGroup({
  source: 'Auth',
  events: {
    Login: props<{ user: User; redirectTo?: string }>(),
    'Login Success': props<{ username: string; token: string; redirectTo?: string }>(),
    'Login Failure': props<{ error: string }>(),

    Logout: emptyProps(),
    'Session Expired': emptyProps(),

    /** Restores a session held in sessionStorage on app start. */
    'Restore Session': emptyProps(),
    'Restore Session Success': props<{ username: string }>(),
    'Restore Session Failure': emptyProps(),
  },
});

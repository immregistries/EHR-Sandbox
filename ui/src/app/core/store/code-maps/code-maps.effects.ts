import {inject, Injectable} from '@angular/core';
import {Actions, createEffect, ofType} from '@ngrx/effects';
import {catchError, forkJoin, map, of, switchMap} from 'rxjs';
import {CodeMapsService} from '../../services';
import {AuthActions} from '../auth/auth.actions';
import {CodeMapsActions} from './code-maps.actions';

@Injectable()
export class CodeMapsEffects {
  private readonly actions$ = inject(Actions);
  private readonly codeMapsService = inject(CodeMapsService);

  load$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CodeMapsActions.load),
      switchMap(() =>
        forkJoin({
          codeBaseMap: this.codeMapsService.readCodeMaps(),
          identifierTypeCodeSystem: this.codeMapsService.readIdentifierTypeCodeSystem(),
          qualificationCodeSystem: this.codeMapsService.readQualificationCodeSystem(),
        }).pipe(
          map((result) => CodeMapsActions.loadSuccess(result)),
          catchError((error) => of(CodeMapsActions.loadFailure({error: error.message}))),
        ),
      ),
    ),
  );

  refresh$ = createEffect(() =>
    this.actions$.pipe(
      ofType(CodeMapsActions.refresh),
      switchMap(() =>
        this.codeMapsService.readCodeMaps().pipe(
          map((codeBaseMap) => CodeMapsActions.refreshSuccess({codeBaseMap})),
          catchError((error) => of(CodeMapsActions.loadFailure({error: error.message}))),
        ),
      ),
    ),
  );

  /** Codemaps are behind auth, so they load once a session is established. */
  loadOnAuthenticated$ = createEffect(() =>
    this.actions$.pipe(
      ofType(AuthActions.loginSuccess, AuthActions.restoreSessionSuccess),
      map(() => CodeMapsActions.load()),
    ),
  );
}

import {HttpClient} from '@angular/common/http';
import {inject, Injectable} from '@angular/core';
import {CodeSystem} from 'fhir/r5';
import {map, Observable} from 'rxjs';
import {CodeBaseMap, CodeMap} from '../models/code-base-map';
import {ApiBase, JSON_HEADERS} from './api.base';

export const IDENTIFIER_TYPE_SYSTEM_FILE = 'assets/CodeSystem-v2-0203.json';
export const QUALIFICATION_SYSTEM_FILE = 'assets/CodeSystem-v2-0360.json';

/**
 * Stateless reader for the codemaps and the bundled FHIR code systems.
 * The loaded maps are held in the code-maps store slice.
 */
@Injectable({providedIn: 'root'})
export class CodeMapsService extends ApiBase {
  private readonly http = inject(HttpClient);

  readCodeMaps(): Observable<CodeBaseMap> {
    return this.http
      .get<CodeMap>(this.url('/code_maps'), JSON_HEADERS)
      .pipe(map((codeMap) => codeMap.codeBaseMap));
  }

  readIdentifierTypeCodeSystem(): Observable<CodeSystem> {
    return this.http.get<CodeSystem>(IDENTIFIER_TYPE_SYSTEM_FILE);
  }

  readQualificationCodeSystem(): Observable<CodeSystem> {
    return this.http.get<CodeSystem>(QUALIFICATION_SYSTEM_FILE);
  }
}

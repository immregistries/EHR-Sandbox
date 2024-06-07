import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, take } from 'rxjs';
import { Code, CodeBaseMap, CodeMap } from "../_model/code-base-map";
import { SettingsService } from './settings.service';
import { CodeSystem } from 'fhir/r5';


// Potentially needed to load codemaps on application init : Not currently used in providers
export function CodeMapsServiceFactory(provider: CodeMapsService) {
  return () => provider.load();
}

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};
/**
 * Service fetching the codemaps provided by the API and making them accessible in the application
 */
@Injectable({
  providedIn: 'root'
})
export class CodeMapsService {
  private codeBaseMap!: BehaviorSubject<CodeBaseMap>;
  private readonly IDENTIFIER_TYPE_SYSTEM_FILE_NAME = "assets/CodeSystem-v2-0203.json";
  private readonly QUALIFICATION_SYSTEM_FILE_NAME = "assets/CodeSystem-v2-0360.json";

  private _qualificationTypeCodeSystem!: CodeSystem;
  public get qualificationTypeCodeSystem(): CodeSystem {
    return this._qualificationTypeCodeSystem;
  }

  private _identifierTypeCodeSystem!: CodeSystem;
  public get identifierTypeCodeSystem(): CodeSystem {
    return this._identifierTypeCodeSystem;
  }

  constructor(private http: HttpClient,
    private settings: SettingsService,) {
    this.load()
  }

  getObservableCodeBaseMap(): BehaviorSubject<CodeBaseMap> {
    if (!this.codeBaseMap) {
      this.refreshCodeMaps()
    }
    return this.codeBaseMap
  }

  getCodeMap(label: string | undefined): CodeMap {
    if (label) {
      if (!this.codeBaseMap) {
        /**
         * TODO clean this up there may be some unnecesary steps now that take(1) was added
         */
        this.readCodeMaps().pipe(take(1)).subscribe({
          next: (codeMap) => {
            this.codeBaseMap.next(codeMap["codeBaseMap"])
            return this.codeBaseMap.value[label];
          },
          error: (error) => {
            // console.error(error)
            return {}
          }
        });
        return {}
      } else {
        return this.codeBaseMap.value[label];
      }
    } else {
      return {}
    }
  }

  readCodeMaps(): Observable<any> {
    return this.http.get<any>(this.settings.getApiUrl() + '/code_maps', httpOptions)
  }

  refreshCodeMaps() {
    this.http.get<any>(this.settings.getApiUrl() + '/code_maps', httpOptions).subscribe((codeMap) => {
      this.codeBaseMap.next(codeMap["codeBaseMap"])
    });
  }

  load() {
    this.http.get<CodeSystem>(this.IDENTIFIER_TYPE_SYSTEM_FILE_NAME).subscribe(res => {
      this._identifierTypeCodeSystem = res;
    });
    this.http.get<CodeSystem>(this.QUALIFICATION_SYSTEM_FILE_NAME).subscribe(res => {
      this._qualificationTypeCodeSystem = res;
    });
    this.codeBaseMap = new BehaviorSubject<CodeBaseMap>({})
    return new Promise((resolve, reject) => {
      this.refreshCodeMaps()
    })
  }
}

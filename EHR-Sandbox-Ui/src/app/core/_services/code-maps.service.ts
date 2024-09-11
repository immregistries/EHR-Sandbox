import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable, take, tap, zip } from 'rxjs';
import { Code, CodeBaseMap, CodeMap, CodeSet } from "../_model/code-base-map";
import { SettingsService } from './settings.service';
import { CodeSystem } from 'fhir/r5';
import { TenantService } from './tenant.service';


// Potentially needed to load codemaps on application init : Not currently used in providers
export function codeMapsServiceFactory(provider: CodeMapsService) {
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
    if (!this._qualificationTypeCodeSystem) {
      this.load()
    }
    return this._qualificationTypeCodeSystem;
  }

  private _identifierTypeCodeSystem!: CodeSystem;
  public get identifierTypeCodeSystem(): CodeSystem {
    if (!this._identifierTypeCodeSystem) {
      this.load()
    }
    return this._identifierTypeCodeSystem;
  }

  constructor(private http: HttpClient,
    private settings: SettingsService,
    private tenantService: TenantService) {
    this.load()
  }

  getObservableCodeBaseMap(): BehaviorSubject<CodeBaseMap> {
    if (!this.codeBaseMap) {
      this.refreshCodeMapsObservable().subscribe()
    }
    return this.codeBaseMap
  }

  getCodeSet(label: string | undefined): CodeSet | undefined {
    if (!label) {
      return undefined
    }
    if (!this.codeBaseMap) {
      return undefined
    }
    if (this.tenantService.getCurrentId() > 0 && this.tenantService.getCurrent().nameDisplay?.toUpperCase().includes('NO_DEPRECATED')) {
      return Object.fromEntries(Object.entries<Code>(this.codeBaseMap.value[label]).filter((entry) => entry[1].codeStatus?.status?.toLowerCase() != "deprecated"))
    } else {
      return this.codeBaseMap.value[label];
    }
  }

  refreshCodeMapsObservable(): Observable<CodeMap> {
    return this.http.get<CodeMap>(this.settings.getApiUrl() + '/code_maps', httpOptions).pipe(tap((codeMap) => {
      // if (this.tenantService.getCurrentId() > 0 && this.tenantService.getCurrent().nameDisplay?.includes('NO_DEPRECATED')) {
      //   Object.values(codeMap.codeBaseMap).forEach((codeSet) => {
      //     codeSet = Object.fromEntries(Object.entries<Code>(codeSet).filter((entry) => entry[1].codeStatus?.status != "Deprecated"))
      //   })
      // }
      // if (!this.codeBaseMap) {
      //   this.codeBaseMap = new BehaviorSubject<CodeBaseMap>({})
      // }
      this.codeBaseMap.next(codeMap.codeBaseMap)
    }));
  }

  load() {
    this.codeBaseMap = new BehaviorSubject<CodeBaseMap>({})
    return zip(
      this.http.get<CodeSystem>(this.IDENTIFIER_TYPE_SYSTEM_FILE_NAME).pipe(tap(res => {
        this._identifierTypeCodeSystem = res;
      })),
      this.http.get<CodeSystem>(this.QUALIFICATION_SYSTEM_FILE_NAME).pipe(tap(res => {
        this._qualificationTypeCodeSystem = res;
      })),
      this.http.get<string>("assets/test.json").pipe(tap(res => {
        console.log("res", res)
      })),
      this.refreshCodeMapsObservable()
    )
  }
}

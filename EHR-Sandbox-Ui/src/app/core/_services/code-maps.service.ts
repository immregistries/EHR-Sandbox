import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Code, CodeBaseMap, CodeMap } from '../_model/structure';
import { SettingsService } from './settings.service';


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
  private codeBaseMap!:  BehaviorSubject<CodeBaseMap>;

  constructor(private http: HttpClient,
    private settings: SettingsService,) {
      this.load()
     }

  getObservableCodeBaseMap(): BehaviorSubject<CodeBaseMap>{
    if (!this.codeBaseMap){
      this.refreshCodeMaps()
    }
    return this.codeBaseMap
  }

  getCodeMap(label: string | undefined): CodeMap  {
    if (label){
      if (!this.codeBaseMap) {
        this.readCodeMaps().subscribe({
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
      }else {
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
    this.codeBaseMap = new BehaviorSubject<CodeBaseMap>({})
    return new Promise((resolve, reject) => {
      this.refreshCodeMaps()
    })
}
}

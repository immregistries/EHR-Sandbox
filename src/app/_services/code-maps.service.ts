import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from 'rxjs';
import { Code } from '../_model/structure';
import { SettingsService } from './settings.service';


// Needed to load codemaps on application init : NOT CURRENTLY USED IN PROVIDERS
export function CodeMapsServiceFactory(provider: CodeMapsService) {
  return () => provider.load();
}

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

@Injectable({
  providedIn: 'root'
})
export class CodeMapsService {
  private codeBaseMap!:  BehaviorSubject<{[key:string]: {[key:string]: Code}}>;

  constructor(private http: HttpClient,
    private settings: SettingsService,) {
      this.load()
     }

  getObservableCodeBaseMap(): BehaviorSubject<{[key:string]: {[key:string]: Code}}>{
    if (!this.codeBaseMap){
      this.refreshCodeMaps()
    }
    return this.codeBaseMap
  }

  getCodeMap(label: string | undefined): {[key:string]: Code}  {
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
    this.codeBaseMap = new BehaviorSubject<{[key:string]: {[key:string]: Code}}>({})
    return new Promise((resolve, reject) => {
      this.refreshCodeMaps()
    })
}
}

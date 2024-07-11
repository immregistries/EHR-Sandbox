import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable, catchError, lastValueFrom, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import packageJson from '../../../../package.json';
import { error } from 'console';

const httpOptions = {
  headers: new HttpHeaders({ 'Content-Type': 'application/json' })
};

/**
 * Service responsible for :
 *  - fetching global variables and making them accessible to the components,
 *  - interacting with the User's settings in the API
 */
@Injectable({
  providedIn: 'root'
})
export class SettingsService {
  private envConfig: EnvConfig = environment;
  private version: string = packageJson.version;

  constructor(private http: HttpClient,) {
  }

  public getVersion(): string {
    return this.version
  }

  public getApiUrl(): string {
    return this.envConfig.apiUrl
  }

  /** runtime configuration inspired by https://levelup.gitconnected.com/angular-environment-configuration-at-runtime-b44e230da585 */

  async loadEnvConfig(configPath: string): Promise<void> {
    try {
      this.envConfig = await lastValueFrom(this.http.get<EnvConfig>(configPath));
      if (this.envConfig.apiUrl.startsWith("$")) {
        console.log('Static environment config loaded!');
        this.envConfig = environment
      }
    } catch (error) {
      console.log('Static environment config loaded!');
      this.envConfig = environment
    }
  }

  getEnvConfig(): EnvConfig {
    return this.envConfig;
  }
}

export interface EnvConfig {
  apiUrl: string;
}


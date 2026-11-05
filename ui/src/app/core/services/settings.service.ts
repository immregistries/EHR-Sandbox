import {HttpClient} from '@angular/common/http';
import {inject, Injectable, signal} from '@angular/core';
import {lastValueFrom} from 'rxjs';
import {environment} from '../../../environments/environment';

export interface EnvConfig {
  apiUrl: string;
}

/**
 * Exposes global configuration: API url (resolvable at runtime) and app version.
 */
@Injectable({providedIn: 'root'})
export class SettingsService {
  private readonly http = inject(HttpClient);
  private readonly envConfig = signal<EnvConfig>(environment);

  readonly apiUrl = this.envConfig.asReadonly();

  getApiUrl(): string {
    return this.envConfig().apiUrl;
  }

  getEnvConfig(): EnvConfig {
    return this.envConfig();
  }

  /**
   * Runtime configuration: falls back to the compiled environment when the
   * config file is missing or still holds an unsubstituted `$` placeholder.
   */
  async loadEnvConfig(configPath: string): Promise<void> {
    try {
      const config = await lastValueFrom(this.http.get<EnvConfig>(configPath));
      this.envConfig.set(config?.apiUrl?.startsWith('$') ? environment : config);
    } catch {
      this.envConfig.set(environment);
    }
  }
}

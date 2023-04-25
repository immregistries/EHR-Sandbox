import { Pipe, PipeTransform } from '@angular/core';
import { Observable, concat, first, map, mergeMap, of, shareReplay, switchMap, tap } from 'rxjs';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';

/**
 * DO NOT USE, Prototype, extremely inefficient
 */
@Pipe({
  name: 'registryName',
  // pure: false
})
export class RegistryNamePipe implements PipeTransform {

  private registriesCached$?: Observable<ImmunizationRegistry[]>;

  constructor(private immunizationRegistryService: ImmunizationRegistryService) {

  }

  transform(id: number): Observable<string>{
    console.log("call")
    if (!this.registriesCached$) {
      console.log("http")

      this.registriesCached$ = this.immunizationRegistryService.readImmRegistries()
        .pipe(
          shareReplay(1)
          // tap((res) => this.registriesCached = res),
          // tap((res) => console.log(this.registries)),
          // mergeMap(registries => registries.find((reg) => id == reg.id)?.name ?? 'not found')
          // map(registries => registries?.find((reg) => id == reg.id)?.name ?? '' + id)
          )
    }
    return this.registriesCached$.pipe(
      map(registries => registries?.find((reg) => id == reg.id)?.name ?? '' + id)
    );
  }





}

import { Pipe, PipeTransform } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';

/**
 * DO NOT USE, Prototype, extremely inefficient
 */
@Pipe({
  name: 'registryName',
  pure: true
})
export class RegistryNamePipe implements PipeTransform {


  constructor(private immunizationRegistryService: ImmunizationRegistryService) {
  }

  transform(registry: number | ImmunizationRegistry, list?: ImmunizationRegistry[]): string {
    if (!registry) {
      return "";
    }
    if (typeof registry === "object") {
      return registry.name ?? ""
    }
    if (list) {
      return list?.find((reg) => registry == reg.id)?.name ?? '' + registry
    } else {
      // if (!this.registriesCached$) {
      //   this.registriesCached$ = this.immunizationRegistryService.readImmRegistries()
      //     .pipe(
      //       shareReplay(1)
      //       // tap((res) => this.registriesCached = res),
      //       // tap((res) => console.log(this.registries)),
      //       // mergeMap(registries => registries.find((reg) => id == reg.id)?.name ?? 'not found')
      //       // map(registries => registries?.find((reg) => id == reg.id)?.name ?? '' + id)
      //     )
      // }
      return this.immunizationRegistryService.registriesCached?.find((reg) => registry == reg.id)?.name ?? '' + registry
    }

  }





}

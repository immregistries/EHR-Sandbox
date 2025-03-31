import { Pipe, type PipeTransform } from '@angular/core';
import { firstValueFrom, Observable, of, tap } from 'rxjs';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Pipe({
  name: 'vaccinationCache',
  // standalone: true,
})
export class VaccinationCachePipe implements PipeTransform {

  constructor(private facilityService: FacilityService, private vaccinationService: VaccinationService) {
  }

  transform(vaccinations: (number | VaccinationEvent)[] | undefined, list?: VaccinationEvent[]): VaccinationEvent[] | undefined {
    if (!vaccinations) {
      return undefined
    }
    if (vaccinations.length < 1) {
      return []
    }
    // Ignore type to still include id if nothing is found
    // @ts-ignore
    return vaccinations.map((value) => {
      if (typeof value === "object") {
        return value
      }
      if (list) {
        return list.find((pat) => value == pat.id) ?? value
      } else {
        return this.vaccinationService.vaccinationsCached?.find(p => (value == p.id)) ?? value;

        return this.vaccinationService.quickReadVaccinationsFromFacility().subscribe((cached) => {
          return cached.find(p => (value == p.id)) ?? value;
        })
      }
    })
  }
}

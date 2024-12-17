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

  _updated_cached_vaccinations: Observable<VaccinationEvent[]> = new Observable((subscriber) => {
    /**
 * If facility is selected and no cache
 */
    if (this.facilityService.getCurrentId() > -1 && !this.vaccinationService.vaccinationsCached) {
      this.vaccinationService.quickReadVaccinations().subscribe((res) => {
        subscriber.next(res)
      })
    } else {
      subscriber.next(this.vaccinationService.vaccinationsCached)
    }
  })

  transform(vaccinations: (number | VaccinationEvent)[] | undefined, list?: VaccinationEvent[]): VaccinationEvent[] | undefined {
    if (this.facilityService.getCurrentId() > -1 && !this.vaccinationService.vaccinationsCached) {
      this.vaccinationService.quickReadVaccinations().subscribe()
    }
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
      }
    })
  }
}

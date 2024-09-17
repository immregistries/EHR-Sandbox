import { Pipe, type PipeTransform } from '@angular/core';
import { firstValueFrom, Observable, of, tap } from 'rxjs';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';

@Pipe({
  name: 'patientCache',
  // standalone: true,
})
export class PatientCachePipe implements PipeTransform {

  constructor(private facilityService: FacilityService, private patientService: PatientService) {
  }

  _updated_cached_patients: Observable<EhrPatient[]> = new Observable((subscriber) => {
    /**
 * If facility is selected and no cache
 */
    if (this.facilityService.getCurrentId() > -1 && !this.patientService.patientsCached) {
      this.patientService.quickReadPatients().subscribe((res) => {
        subscriber.next(res)
      })
    } else {
      subscriber.next(this.patientService.patientsCached)
    }
  })

  transform(patients: (number | EhrPatient)[] | undefined, list?: EhrPatient[]): (EhrPatient)[] | undefined {
    if (this.facilityService.getCurrentId() > -1 && !this.patientService.patientsCached) {
      this.patientService.quickReadPatients().subscribe()
    }
    if (!patients) {
      return undefined
    }
    if (patients.length < 1) {
      return []
    }
    // Ignore type to still include id if nothing is found
    // @ts-ignore
    return patients.map((value) => {
      if (typeof value === "object") {
        return value
      }
      if (list) {
        return list.find((pat) => value == pat.id) ?? value
      } else {
        return this.patientService.patientsCached?.find(p => (value == p.id)) ?? value;
      }
    })
  }
}

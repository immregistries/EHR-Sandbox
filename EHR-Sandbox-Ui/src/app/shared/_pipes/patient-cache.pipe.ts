import { Pipe, type PipeTransform } from '@angular/core';
import { delay, firstValueFrom, Observable, of, tap } from 'rxjs';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';

@Pipe({
  name: 'patientCache',
  // standalone: true,
})
export class PatientCachePipe implements PipeTransform {

  constructor(private patientService: PatientService) {
  }

  public transform(patients: (number | EhrPatient)[] | undefined, list?: EhrPatient[]): EhrPatient[] | undefined {
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
        return this.patientService.patientsCached?.find(p => (value === p.id)) ?? value;
        // .subscribe((patientsCached) => {
        //   console.info(patientsCached)
        //   return patientsCached?.find(p => (value === p.id)) ?? value;
        // })
      }
    })
  }
}

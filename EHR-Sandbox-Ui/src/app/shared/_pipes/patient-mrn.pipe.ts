import { Pipe, type PipeTransform } from '@angular/core';
import { firstValueFrom, Observable, of, tap } from 'rxjs';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { PatientCachePipe } from './patient-cache.pipe';

@Pipe({
  name: 'patientMrn',
  // standalone: true,
})
export class PatientMrnPipe implements PipeTransform {

  constructor(private facilityService: FacilityService, private patientService: PatientService) {
  }

  transform(patient: (number | EhrPatient) | undefined, list?: EhrPatient[]): string | undefined {
    if (this.facilityService.getCurrentId() > -1 && !this.patientService.patientsCached) {
      this.patientService.quickReadPatients().subscribe()
    }
    let obj: EhrPatient | undefined = undefined;
    if (!patient) {
      return ''
    }
    if (typeof patient === "object") {
      obj = patient
    } else if (list) {
      obj = list.find((pat) => patient == pat.id)
    } else {
      obj = this.patientService.patientsCached?.find(p => (patient == p.id)) ?? undefined;
    }
    return obj?.identifiers?.find((identifier) => {
      return identifier.type == 'MR'
    })?.value ?? ''

  }
}

import { DatePipe } from '@angular/common';
import { Pipe, type PipeTransform } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { PatientCachePipe } from './patient-cache.pipe';

@Pipe({
  name: 'patientResume',
  // standalone: true,
})
export class PatientResumePipe implements PipeTransform {

  constructor(private datePipe: DatePipe, private patientCachePipe: PatientCachePipe) {
  }

  transform(patient: (number | EhrPatient) | undefined, mode: ('name' | 'birthDate' | 'mrn' | 'full')[], list?: EhrPatient[]): string | undefined {
    // if (this.facilityService.getCurrentId() > -1 && !this.patientService.patientsCached) {
    //   this.patientService.quickReadPatients().subscribe()
    // }
    if (!patient) {
      return ''
    }
    let obj: EhrPatient | undefined = (this.patientCachePipe.transform([patient], list) ?? [undefined])[0]
    if (!obj) {
      return ""
    }
    let result = ""
    if (mode?.includes("full")) {
      mode = ['name', 'birthDate', 'mrn']
    }
    mode?.forEach(element => {
      if (element === "birthDate") {
        result += this.datePipe.transform(obj?.birthDate, "shortDate")
      } else if (element === 'mrn') {
        result += this.extractMrn(obj)
      } else if (element === 'name') {
        result += obj?.names[0].nameLast + ", " + (obj?.names[0].nameFirst ?? '')
      }
      result += " "
    });
    return result
  }

  extractMrn(obj: EhrPatient | undefined) {
    return (obj?.identifiers?.find((identifier) => {
      return identifier.type == 'MR'
    })?.value ?? '')

  }
}

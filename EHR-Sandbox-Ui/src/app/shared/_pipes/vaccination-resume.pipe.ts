import { DatePipe } from '@angular/common';
import { Pipe, type PipeTransform } from '@angular/core';
import { VaccinationEvent } from 'src/app/core/_model/rest';
import { VaccinationCachePipe } from './vaccination-cache.pipe';
import { CodeMapsPipe } from './code-maps.pipe';

@Pipe({
  name: 'vaccinationResume',
})
export class VaccinationResumePipe implements PipeTransform {

  constructor(private datePipe: DatePipe, private vaccinationCachePipe: VaccinationCachePipe, private codeMapsPipe: CodeMapsPipe) {
  }

  transform(vaccination: (number | VaccinationEvent) | undefined, mode: ('lotNumber' | 'administeredDate' | 'createdDate' | 'updatedDate' | 'cvx')[], list?: VaccinationEvent[]): string | undefined {
    // if (this.facilityService.getCurrentId() > -1 && !this.vaccinationService.vaccinationsCached) {
    //   this.vaccinationService.quickReadPatients().subscribe()
    // }
    if (!vaccination) {
      return ''
    }
    let obj: VaccinationEvent | undefined = (this.vaccinationCachePipe.transform([vaccination], list) ?? [undefined])[0]
    if (!obj) {
      return ""
    }
    let result = ""
    // if (mode?.includes("full")) {
    //   mode = ['name', 'birthDate', 'mrn']
    // }
    mode?.forEach(element => {
      if (element === "administeredDate") {
        result += this.datePipe.transform(obj?.vaccine?.administeredDate, "shortDate")
      }
      if (element === "createdDate") {
        result += this.datePipe.transform(obj?.vaccine?.createdDate, "shortDate")
      }
      if (element === "updatedDate") {
        result += this.datePipe.transform(obj?.vaccine?.updatedDate, "shortDate")
      } else if (element === 'cvx') {
        result += this.codeMapsPipe.transform(obj?.vaccine?.vaccineCvxCode, "VACCINATION_CVX_CODE").label ?? obj?.vaccine?.vaccineCvxCode
      } else if (element === 'lotNumber') {
        result += obj?.vaccine?.lotNumber
      }
      result += " "
    });
    return result
  }
}

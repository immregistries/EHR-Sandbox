import { Pipe, PipeTransform } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
import { ComparisonResult } from 'src/app/core/_model/form-structure';
import { AbstractComparisonPipe } from './abstract-comparison.pipe';

@Pipe({
  name: 'patientCompare'
})
export class PatientComparePipe extends AbstractComparisonPipe implements PipeTransform {
  readonly fields_to_ignore: string[] = [
    'patient',
    'createdDate',
    'updatedDate' // TODO change the way updated date is mapped ?
  ]

  transform(value: EhrPatient, ...args: (EhrPatient | null)[]): {} {
    const differences = this.recursiveComparison(value, args[0]);
    if (!differences) {
      return "MATCH"
    } else {
      return differences;
    }
  }

}

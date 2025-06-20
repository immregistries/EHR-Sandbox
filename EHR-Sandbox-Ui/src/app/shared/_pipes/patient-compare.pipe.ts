import { Pipe, PipeTransform } from '@angular/core';
import { EhrPatient } from 'src/app/core/_model/rest';
import { ComparisonResult } from 'src/app/core/_model/form-structure';
import { AbstractComparisonPipe } from './abstract-comparison.pipe';
import { rdiffResult } from 'recursive-diff';

@Pipe({
  name: 'patientCompare'
})
export class PatientComparePipe extends AbstractComparisonPipe implements PipeTransform {
  readonly fields_to_ignore: string[] = [
    'id',
    'patient',
    'createdDate',
    'updatedDate', // TODO change the way updated date is mapped ?
    'groupNames',
  ]

  transform(value: EhrPatient, ...args: (EhrPatient | null)[]): rdiffResult[] | string {
    const differences = this.packageComp(value, args[0]);
    if (!differences || differences.length == 0) {
      return "MATCH"
    } else {
      return differences;
    }
  }

}

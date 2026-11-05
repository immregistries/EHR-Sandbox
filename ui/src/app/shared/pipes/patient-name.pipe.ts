import {Pipe, PipeTransform} from '@angular/core';
import {EhrHumanName, EhrPatient} from '../../core/models';

/**
 * Resolves a patient's display name, preferring the official ('L') entry —
 * `names` is an array and the preferred entry is not necessarily first.
 *
 * Exported as a function too, so tables can flatten rows for sorting with
 * exactly the value the pipe renders.
 */
export function patientDisplayName(patient: EhrPatient | null | undefined): string {
  if (!patient?.names?.length) {
    return '';
  }
  const name: EhrHumanName =
    patient.names.find((candidate) => candidate.nameType === 'L') ?? patient.names[0];
  return [name.nameFirst, name.nameMiddle, name.nameLast].filter(Boolean).join(' ');
}

@Pipe({name: 'patientName', standalone: true})
export class PatientNamePipe implements PipeTransform {
  transform(patient: EhrPatient | null | undefined): string {
    return patientDisplayName(patient) || 'Unnamed patient';
  }
}

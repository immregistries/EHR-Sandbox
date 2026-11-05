import {Pipe, PipeTransform} from '@angular/core';
import {EhrHumanName, EhrPatient} from '../../core/models';

/** Renders a patient's legal name, preferring the official ('L') entry. */
@Pipe({name: 'patientName', standalone: true})
export class PatientNamePipe implements PipeTransform {
  transform(patient: EhrPatient | null | undefined): string {
    if (!patient?.names?.length) {
      return 'Unnamed patient';
    }
    const name: EhrHumanName =
      patient.names.find((candidate) => candidate.nameType === 'L') ?? patient.names[0];
    const parts = [name.nameFirst, name.nameMiddle, name.nameLast].filter(Boolean);
    return parts.length ? parts.join(' ') : 'Unnamed patient';
  }
}

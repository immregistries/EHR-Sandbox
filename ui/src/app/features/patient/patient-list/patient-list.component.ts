import {ChangeDetectionStrategy, Component} from '@angular/core';

/** Placeholder — implemented in the components step. */
@Component({
  selector: 'app-patient-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h2>Patients</h2>`,
})
export class PatientListComponent {
}

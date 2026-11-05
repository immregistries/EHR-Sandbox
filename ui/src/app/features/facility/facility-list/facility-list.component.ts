import {ChangeDetectionStrategy, Component} from '@angular/core';

/** Placeholder — implemented in the components step. */
@Component({
  selector: 'app-facility-list',
  standalone: true,
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `<h2>Facilities</h2>`,
})
export class FacilityListComponent {
}

import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { Facility } from 'src/app/core/_model/rest';

@Component({
  selector: 'app-facility-table',
  templateUrl: './facility-table.component.html',
  styleUrls: ['./facility-table.component.css']
})
export class FacilityTableComponent extends AbstractDataTableComponent<Facility> {

  @Input()
  title: String = 'Facilities'

  columns: string[] = []

  openFacility(facility: Facility) {
    // this.dialog
  }


}

import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { Facility } from 'src/app/core/_model/rest';
import { MatDialog } from '@angular/material/dialog';
import { FacilityDashboardComponent } from '../facility-dashboard/facility-dashboard.component';

@Component({
  selector: 'app-facility-table',
  templateUrl: './facility-table.component.html',
  styleUrls: ['./facility-table.component.css']
})
export class FacilityTableComponent extends AbstractDataTableComponent<Facility> {
  constructor(private dialog: MatDialog) {
    super()
  }

  @Input()
  title: String = 'Facilities'

  columns: string[] = ["nameDisplay", "childrenCount"]

  openFacility(element: Facility) {
    this.dialog.open(FacilityDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {facility: element}
    })

  }


}

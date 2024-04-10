import { Component, Input } from '@angular/core';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { Clinician, Tenant } from 'src/app/core/_model/rest';
import { MatDialog } from '@angular/material/dialog';
import { ClinicianFormComponent } from '../clinician-form/clinician-form.component';
import { ClinicianService } from 'src/app/core/_services/clinician.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-clinician-table',
  templateUrl: './clinician-table.component.html',
  styleUrls: ['./clinician-table.component.css']
})
export class ClinicianTableComponent extends AbstractDataTableComponent<Clinician> {

  @Input()
  title: string = 'Clinicians'

  constructor(private dialog: MatDialog,
    public tenantService: TenantService,
    private clinicianService: ClinicianService) {
    super()
    this.observableRefresh = tenantService.getCurrentObservable();
    this.observableSource = clinicianService.quickReadClinicians()
  }

  @Input()
  columns: (keyof Clinician)[] = [
    "nameFirst",
    "nameMiddle",
    "nameLast",
  ]

  openCreation() {
    const dialogRef = this.dialog.open(ClinicianFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: {},
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.ngAfterViewInit()
        this.onSelection(result)
      }
    });
  }



}

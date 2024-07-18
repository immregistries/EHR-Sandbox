import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityFormComponent } from '../facility-form/facility-form.component';
import { FhirMessagingComponent } from 'src/app/shared/_fhir/fhir-messaging/fhir-messaging.component';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FhirResourceService } from 'src/app/core/_services/_fhir/fhir-resource.service';
import { error } from 'console';

@Component({
  selector: 'app-facility-tools',
  templateUrl: './facility-tools.component.html',
  styleUrls: ['./facility-tools.component.css']
})
export class FacilityToolsComponent {
  @Input()
  facility!: Facility

  constructor(public dialog: MatDialog, public facilityService: FacilityService, public fhirResourceService: FhirResourceService) { }

  openEdition() {
    const dialogRef = this.dialog.open(FacilityFormComponent, {
      maxWidth: '48vw',
      maxHeight: '98vh',
      minWidth: '33vw',
      height: 'fit-content',
      width: 'fit-content',
      panelClass: 'dialog-without-bar',
      data: {
        facility: this.facility
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.facility = result

      }
    });
  }

  openFhir() {
    this.fhirResourceService.quickGetOrganizationResource(this.facility.id ?? -1).subscribe({
      next: (res) => {
        const dialogRef = this.dialog.open(FhirMessagingComponent, {
          maxWidth: '95vw',
          maxHeight: '98vh',
          height: 'fit-content',
          width: '100%',
          panelClass: 'dialog-without-bar',
          data: { resource: res, resourceType: "Organization" },
        });
        dialogRef.afterClosed().subscribe(result => {
          this.facilityService.doRefresh()
        });
      }, error: (error) => {

      }
    })

  }

  openFhirBundle() {
    if (!this.facility.id) {
      return;
    }
    const dialogRef = this.dialog.open(FhirMessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: { resourceObservable: this.fhirResourceService.getFacilityExportBundle(this.facility.id), resourceType: "", resourceLocalId: this.facility.id, operation: "$transaction" },
    });
    dialogRef.afterClosed().subscribe(result => {
      this.facilityService.doRefresh()
    });
  }

}

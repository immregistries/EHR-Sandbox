import { Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Facility } from 'src/app/core/_model/rest';
import { FacilityCreationComponent } from '../facility-creation/facility-creation.component';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FhirResourceService } from 'src/app/fhir/_services/fhir-resource.service';
import { error } from 'console';

@Component({
  selector: 'app-facility-tools',
  templateUrl: './facility-tools.component.html',
  styleUrls: ['./facility-tools.component.css']
})
export class FacilityToolsComponent {
  @Input()
  facility!: Facility

  constructor(public dialog: MatDialog, public facilityService: FacilityService, public fhirResourceService: FhirResourceService) {}

  openEdition() {
    const dialogRef = this.dialog.open(FacilityCreationComponent, { data: { facility: this.facility } });
    // dialogRef.afterClosed().subscribe(result => {
    // });
  }

  openFhir() {
    this.fhirResourceService.quickGetOrganizationResource(this.facility.id).subscribe({
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
    const dialogRef = this.dialog.open(FhirMessagingComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: { resourceObservable: this.fhirResourceService.getFacilityExportBundle(this.facility.id), resourceType: "" , resourceLocalId: this.facility.id, operation: "$transaction"},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.facilityService.doRefresh()
    });
  }

}

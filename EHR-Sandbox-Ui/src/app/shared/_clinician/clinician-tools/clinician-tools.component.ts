import { Component, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { Clinician } from 'src/app/core/_model/rest';
import { ClinicianService } from 'src/app/core/_services/clinician.service';
import { ClinicianFormComponent } from '../clinician-form/clinician-form.component';
import { FhirResourceService } from 'src/app/fhir/_services/fhir-resource.service';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';

@Component({
  selector: 'app-clinician-tools',
  templateUrl: './clinician-tools.component.html',
  styleUrls: ['./clinician-tools.component.css']
})
export class ClinicianToolsComponent {

  @Input()
  clinician!: Clinician;

  constructor(private dialog: MatDialog,
    private clinicianService: ClinicianService,
    private fhirResourceService: FhirResourceService) {

  }

  openEdition() {
    const dialogRef = this.dialog.open(ClinicianFormComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { clinician: this.clinician },
    });
    dialogRef.afterClosed().subscribe(result => {
      this.clinicianService.doRefresh()
    });
  }


  openFhir() {
    this.fhirResourceService.getClinicianResource(this.clinician.id ?? -1).subscribe((resource) => {

      const dialogRef = this.dialog.open(FhirMessagingComponent, {
        maxWidth: '95vw',
        maxHeight: '95vh',
        height: 'fit-content',
        width: '100%',
        panelClass: 'dialog-with-bar',
        data: { resource: resource, resourceType: "Clinician"},
      });
      dialogRef.afterClosed().subscribe(result => {
        this.clinicianService.doRefresh()
      });
    })

  }




}

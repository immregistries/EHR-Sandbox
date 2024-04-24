import { Component, EventEmitter, Inject, Input, OnInit, Optional, Output } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialog, MatDialogRef } from '@angular/material/dialog';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { FhirGetComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-get/fhir-get.component';
import { FhirMessagingComponent } from 'src/app/fhir/_components/fhir-messaging/fhir-messaging.component';

@Component({
  selector: 'app-immunization-registry-form',
  templateUrl: './immunization-registry-form.component.html',
  styleUrls: ['./immunization-registry-form.component.css']
})
export class ImmunizationRegistryFormComponent implements OnInit {
  @Input()
  immunizationRegistry!: ImmunizationRegistry
  @Input()
  editMode: boolean = true

  @Output()
  success: EventEmitter<ImmunizationRegistry> = new EventEmitter<ImmunizationRegistry>()

  constructor(
    private dialog: MatDialog,
    private registryService: ImmunizationRegistryService,
    private snack: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<ImmunizationRegistryFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {}) {
      console.log(data)
    if (data) {
      this.editMode = true;
      console.log(data)
      this.immunizationRegistry = data
    }
    if (_dialogRef && !data) {
      this.editMode = false;
      this.immunizationRegistry = {}
    }
  }

  ngOnInit(): void {
    // if (this.editMode) {
    //   this.registryService.getCurrentObservable().subscribe(res => {
    //     this.immunizationRegistry = res
    //   })
    // }
  }

  save() {
    this.registryService.putImmRegistry(this.immunizationRegistry).subscribe(res => {
      this.registryService.doRefresh();
      this.registryService.setCurrent(res);
      this.snack.successMessage('Saved')
      this.success.emit(res)
    })
  }

  delete() {
    if (this.immunizationRegistry.id) {
      this.registryService.deleteImmRegistry(this.immunizationRegistry.id).subscribe(res => {
        this.registryService.doRefresh();
      })
    }
  }

  new() {
    this.registryService.setCurrent({});
  }

  height(): number {
    return window.innerHeight - 200
  }

  checkMetadata() {
    this.registryService.putImmRegistry(this.immunizationRegistry).subscribe(res => {
      this.registryService.doRefresh();
      this.registryService.setCurrent(res);
      this.success.emit(res)
      const dialogRef = this.dialog.open(FhirGetComponent, {
        maxWidth: '95vw',
        maxHeight: '98vh',
        height: 'fit-content',
        width: '100%',
        panelClass: 'dialog-without-bar',
        data: {resourceType: 'metadata'},
      });
      dialogRef.afterClosed().subscribe(result => {

      });
    })

  }

}

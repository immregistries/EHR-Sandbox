import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { FhirClientService } from 'src/app/core/_services/_fhir/fhir-client.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';

@Component({
  selector: 'app-immunization-registry-check',
  templateUrl: './immunization-registry-check.component.html',
  styleUrls: ['./immunization-registry-check.component.css']
})
export class ImmunizationRegistryCheckComponent {
  hl7State: Boolean = false;
  hl7Auth: Boolean = false;
  fhirState: Boolean = false;
  fhirAuth: Boolean = false;

  private _registry!: ImmunizationRegistry;
  public get registry(): ImmunizationRegistry {
    return this._registry;
  }
  @Input()
  public set registry(value: ImmunizationRegistry) {
    this._registry = value;
    this.registryService.checkConnectivityAuth(value.id).subscribe({
      next: res => {
        this.hl7Auth = true
        this.hl7State = true
        this.hl7State = true
      },
      error: err => {
        this.hl7Auth = false
        this.registryService.checkConnectivity(value.id).subscribe({
          next: res => {
            this.hl7State = true
          },
          error: err => {
            this.hl7State = false
          }
        })
      }
    })


    this.fhirService.search('Patient', { value: '-1' }).subscribe({
      next: res => {
        this.fhirAuth = true
        this.fhirState = true
      },
      error: err => {
        this.fhirAuth = false
      }
    }
    )

  }

  constructor(
    public registryService: ImmunizationRegistryService,
    public fhirService: FhirClientService,
    public snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<ImmunizationRegistryCheckComponent>,

    @Optional() @Inject(MAT_DIALOG_DATA) public data: { registry: ImmunizationRegistry }) {
    if (data && data.registry) {
      this.registry = data.registry


    }
  }

}

import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { SettingsService } from 'src/app/core/_services/settings.service';

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

  constructor(private registryService: ImmunizationRegistryService,
    @Optional() public _dialogRef: MatDialogRef<ImmunizationRegistryFormComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {}) {
      if (data) {
        this.editMode = true;
        this.immunizationRegistry = data
      }
      if (_dialogRef && !data) {
        this.editMode = false;
        this.immunizationRegistry = {}
      }
     }

  ngOnInit(): void {
    console.log(this.immunizationRegistry)
    if (this.editMode) {
      this.registryService.getCurrentObservable().subscribe(res => {
        this.immunizationRegistry = res
      })
    }
  }

  save() {
    this.registryService.putImmRegistry(this.immunizationRegistry).subscribe(res => {
      this.registryService.doRefresh();
      this.registryService.setCurrent(res);
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

}

import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { ImmunizationRegistry } from '../../_model/rest';
import { ImmunizationRegistryService } from '../../_services/immunization-registry.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  // imm!: ImmunizationRegistry
  @Input()
  editMode = false

  constructor(@Optional() public _dialogRef: MatDialogRef<SettingsComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {}
  ) { }

  ngOnInit(): void {
  }
}

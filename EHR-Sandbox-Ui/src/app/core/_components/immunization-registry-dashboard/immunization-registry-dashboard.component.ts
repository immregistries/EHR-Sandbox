import { Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { ImmunizationRegistry } from '../../_model/rest';
import { ImmunizationRegistryService } from '../../_services/immunization-registry.service';
import { MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';

@Component({
  selector: 'app-immunization-registry-dashboard',
  templateUrl: './immunization-registry-dashboard.component.html',
  styleUrls: ['./immunization-registry-dashboard.component.css']
})
export class ImmunizationRegistryDashboardComponent implements OnInit {
  // imm!: ImmunizationRegistry
  @Input()
  editMode = false

  constructor(public immunizationRegistryService: ImmunizationRegistryService,
    @Optional() public _dialogRef: MatDialogRef<ImmunizationRegistryDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {}
  ) { }

  ngOnInit(): void {
  }
}

import { AfterViewInit, Component, Inject, OnInit, Optional, ViewChild, ViewEncapsulation } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { MatTabGroup } from '@angular/material/tabs';

@Component({
  selector: 'app-fhir-bulk-dashboard',
  templateUrl: './fhir-bulk-dashboard.component.html',
  styleUrls: ['./fhir-bulk-dashboard.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class FhirBulkDashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

  groupId: string = ""

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1;
  }

  constructor(
    @Optional() public _dialogRef: MatDialogRef<FhirBulkDashboardComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) private data: {groupId: string}) {
    if (data && data.groupId) {
      this.groupId = data.groupId;
    }
   }

  ngOnInit(): void {
  }

}

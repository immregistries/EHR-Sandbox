import { AfterViewInit, Component, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatTabGroup } from '@angular/material/tabs';

@Component({
  selector: 'app-fhir-bulk-dashboard',
  templateUrl: './fhir-bulk-dashboard.component.html',
  styleUrls: ['./fhir-bulk-dashboard.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class FhirBulkDashboardComponent implements OnInit, AfterViewInit {
  @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1;
  }

  constructor() { }

  ngOnInit(): void {
  }

}

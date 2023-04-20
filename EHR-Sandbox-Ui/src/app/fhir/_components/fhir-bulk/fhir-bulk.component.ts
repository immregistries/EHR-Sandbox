import { AfterViewChecked, AfterViewInit, Component, Input, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FhirService } from '../../_services/fhir.service';
import { MatTabGroup } from '@angular/material/tabs';

@Component({
  selector: 'app-fhir-bulk',
  templateUrl: './fhir-bulk.component.html',
  styleUrls: ['./fhir-bulk.component.css'],
  // encapsulation: ViewEncapsulation.None
})
export class FhirBulkComponent implements AfterViewInit, AfterViewChecked {
  @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

  ngOnInit(): void {
  }

  constructor() { }

  @Input() asynchronous: boolean = true;

  resultList?: [key:{type: string,url:string}];

  contentUrl: string = ''

  ndUrl: string = ''

  ngAfterViewInit(): void {
    this.tabGroup.animationDuration = 0
    this.tabGroup.selectedIndex = 1;
  }
  ngAfterViewChecked(): void {
    this.tabGroup.animationDuration = 500
  }



  rowHeight(): string {
    return (window.innerHeight - 130) + 'px'
  }

}

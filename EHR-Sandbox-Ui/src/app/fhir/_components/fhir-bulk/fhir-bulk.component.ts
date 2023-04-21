import { AfterViewChecked, AfterViewInit, Component, Input, OnInit, ViewChild, ViewEncapsulation } from '@angular/core';
import { FhirService } from '../../_services/fhir.service';
import { MatTabGroup } from '@angular/material/tabs';

@Component({
  selector: 'app-fhir-bulk',
  templateUrl: './fhir-bulk.component.html',
  styleUrls: ['./fhir-bulk.component.css'],
  // encapsulation: ViewEncapsulation.None
})
export class FhirBulkComponent implements OnInit {

  ngOnInit(): void {
  }

  constructor() { }

  @Input() asynchronous: boolean = true;

  resultList?: [key:{type: string,url:string}];

  contentUrl: string = ''

  ndUrl: string = ''


  rowHeight(): string {
    return (window.innerHeight - 130) + 'px'
  }

}

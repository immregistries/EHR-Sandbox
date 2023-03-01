import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Patient, Revision } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';

@Component({
  selector: 'app-patient-history',
  templateUrl: './patient-history.component.html',
  styleUrls: ['./patient-history.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class PatientHistoryComponent implements OnInit {

  @Input()
  patientId!: number;

  columns: string[] = [
    "revisionInstant",
    "revisionType",
    "origin"
  ]

  history = new MatTableDataSource<Revision<Patient>>([]);
  expandedElement: Revision<Patient> | any = null;

  constructor(private patientService: PatientService) {

  }

  ngOnInit(): void {
    this.patientService.readPatientHistory(this.patientId).subscribe((res) => {
      this.history.data = res
      console.log(res)
    })
  }

  onSelection(event: Revision<Patient>) {
    if (this.expandedElement && this.expandedElement.requiredRevisionNumber == event.requiredRevisionNumber){
      this.expandedElement = {}
    } else {
      this.expandedElement = event
    }
  }
}

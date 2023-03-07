import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { Patient, Revision, VaccinationEvent } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';

@Component({
  selector: 'app-vaccination-history',
  templateUrl: './vaccination-history.component.html',
  styleUrls: ['./vaccination-history.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class VaccinationHistoryComponent implements OnInit {

  @Input()
  patientId: number = -1;
  @Input()
  vaccinationId?: number;

  columns: string[] = [
    "revisionInstant",
    "revisionType",
    "origin"
  ]

  history = new MatTableDataSource<Revision<VaccinationEvent>>([]);
  expandedElement: Revision<VaccinationEvent> | any = null;

  constructor(
    private patientService: PatientService,
    private vaccinationService: VaccinationService,
    ) {

  }

  ngOnInit(): void {
    if (this.patientId < 0) {
      this.patientId = this.patientService.getPatientId()
    }
    if (this.vaccinationId) {
      this.vaccinationService.readVaccinationHistory(this.patientId,this.vaccinationId).subscribe((res) => {
        this.history.data = res
        console.log(res)
      })
    }
  }

  onSelection(event: Revision<VaccinationEvent>) {
    if (this.expandedElement && this.expandedElement.requiredRevisionNumber == event.requiredRevisionNumber){
      this.expandedElement = {}
    } else {
      this.expandedElement = event
    }
  }

}

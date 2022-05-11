import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input, OnInit } from '@angular/core';
import { MatTableDataSource } from '@angular/material/table';
import { switchMap } from 'rxjs';
import { Facility, Feedback, Patient, VaccinationEvent } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';

@Component({
  selector: 'app-feedback-table',
  templateUrl: './feedback-table.component.html',
  styleUrls: ['./feedback-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class FeedbackTableComponent implements AfterViewInit {
  dataSource = new MatTableDataSource<Feedback>([]);
  expandedElement: Feedback | null = null;

  @Input() facility: Facility = {id: -1};
  @Input() patient?: Patient;
  @Input() vaccination?: VaccinationEvent;
  @Input() title: string = 'Feedback from IIS'
  loading: boolean= false

  columns: (keyof Feedback | 'remove')[] = [
    "severity",
    "code",
    "patient",
    "vaccinationEvent",
    "content",
    "remove"
  ]
  asString(val: any) : string { return val; }

  onSelection(event: Feedback) {
    if (this.expandedElement && this.expandedElement.id == event.id){
      this.expandedElement = null
    } else {
      this.expandedElement = event
    }
  }

  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    private feedbackService: FeedbackService,
    private patientService: PatientService) { }


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: Feedback | number, filter: string) =>{
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };
    this.refreshData()
    if (!this.patient && !this.vaccination ) {
      this.patientService.getObservablePatient().subscribe((patient) => {
        this.patient = patient
        this.refreshData()
        })
      this.facilityService.getObservableFacility().subscribe(facility =>{
        this.facility = facility
        this.refreshData()
      })
    }

  }

  refreshData(){
    if (this.vaccination && this.vaccination.id && this.vaccination.id > 0){
      this.dataSource.data = this.vaccination.feedbacks ?? []
    } else if (this.patient && this.patient.id && this.patient.id > 0) {
      this.dataSource.data = this.patient.feedbacks ?? []
    } else if (this.facility && this.facility.id > -1) {
      this.feedbackService.readFacilityFeedback().subscribe((res) => {
        this.dataSource.data = res
        this.loading = false
      })
    }
  }

  openPatient(patient: Patient | number){

  }

  openVaccination(vaccination: VaccinationEvent | number){

  }

  remove(element: Feedback){

  }



}

import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Inject, Input, OnChanges, OnInit, Optional } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Facility, Feedback, EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientDashboardComponent } from 'src/app/patient/patient-dashboard/patient-dashboard.component';
import { VaccinationDashboardComponent } from 'src/app/vaccination/vaccination-dashboard/vaccination-dashboard.component';

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
export class FeedbackTableComponent implements  OnInit,AfterViewInit,OnChanges {
  dataSource = new MatTableDataSource<Feedback>([]);
  expandedElement: Feedback | null = null;

  @Input() facility: Facility = {id: -1};
  @Input() patient?: EhrPatient;
  @Input() vaccination?: VaccinationEvent;
  @Input() title: string = 'Feedback from IIS'
  loading: boolean = false

  columns!: (keyof Feedback | 'remove')[]

  onSelection(event: Feedback) {
    if (this.expandedElement && this.expandedElement.id == event.id){
      this.expandedElement = null
    } else {
      this.expandedElement = event
    }
  }

  constructor(
    private dialog: MatDialog,
    private tenantService: TenantService,
    private facilityService: FacilityService,
    private feedbackService: FeedbackService,
    private patientService: PatientService,
    private snackBarService: SnackBarService,
    @Optional() public _dialogRef: MatDialogRef<FeedbackTableComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {patient: EhrPatient, vaccination: VaccinationEvent}) {
      if (data?.patient){
        this.patient = data.patient;
      }
      if (data?.vaccination){
        this.vaccination = data.vaccination;
      }
    }


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  ngOnInit(): void {
    this.columns = [
      "severity",
      "code",
      "patient",
      "vaccinationEvent",
      "timestamp",
      "iis",
      "content",
      // "remove",
    ]
    if (this.patient) {
      this.columns = this.columns.filter((attribute => attribute != "patient"))
    }
    if (this.vaccination) {
      this.columns = this.columns.filter((attribute => attribute != "vaccinationEvent"))
    }
  }

  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: Feedback | number, filter: string) =>{
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };
    this.feedbackService.getRefresh().subscribe(bool => {
      this.refreshData()
    })
  }

  ngOnChanges(): void {
    this.ngOnInit()
    this.refreshData()
  }

  refreshData(){
    if (this.vaccination && this.vaccination.id && this.vaccination.id > 0){
      this.dataSource.data = this.vaccination.feedbacks ?? []
    } else if (this.patient && this.patient.id && this.patient.id > 0) {
      this.dataSource.data = this.patient.feedbacks ?? []
    } else if (this.facility && this.facility.id > -1) {
      this.feedbackService.readFacilityFeedback(this.facility.id).subscribe((res) => {
        this.dataSource.data = res
        this.loading = false
      })
    } else {
      this.dataSource.data = []
      this.loading = false
    }
  }



  openPatient(patient: EhrPatient | number){
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: patient},
    });
  }

  openVaccination(vaccination: VaccinationEvent | number){
    const dialogRef = this.dialog.open(VaccinationDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {vaccination: vaccination},
    });
  }

  remove(element: Feedback){

  }

}

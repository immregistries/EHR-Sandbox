import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { merge, switchMap, tap } from 'rxjs';
import { FeedbackDialogComponent } from 'src/app/shared/_components/feedback-table/feedback-dialog/feedback-dialog.component';
import { Facility, Patient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientFormDialogComponent } from '../patient-form/patient-form-dialog/patient-form-dialog.component';
import { PatientDashboardDialogComponent } from '../patient-dashboard/patient-dashboard-dialog/patient-dashboard-dialog.component';

@Component({
  selector: 'app-patient-table',
  templateUrl: './patient-table.component.html',
  styleUrls: ['./patient-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class PatientTableComponent implements AfterViewInit {

  dataSource = new MatTableDataSource<Patient>([]);
  // expandedElement: Patient | null = null;

  @Input() facility: Facility = {id: -1};
  @Input() title: string = 'Patients'
  loading: boolean= false
  lastIdSelectedBeforeRefresh: number = -1;

  columns: (keyof Patient | 'alerts')[] = [
    "nameFirst",
    "nameMiddle",
    "nameLast",
    "birthDate",
    'alerts'
  ]

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  onSelection(event: Patient) {
    if (this.patientService.getPatientId() == event.id){
      this.patientService.setPatient({})
    } else {
      this.patientService.setPatient(event)
    }
  }

  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    public patientService: PatientService,
    private dialog: MatDialog) { }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = (data: Patient, filter: string) =>{
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };

    merge(
      this.facilityService.getObservableFacility()
        .pipe(tap(facility => {this.facility = facility; this.patientService.updateLastRefreshtime()})),
      this.patientService.getRefresh()
    ).subscribe(() =>  {
      this.loading = true
      this.patientService.readPatients(this.tenantService.getTenantId(), this.facility.id).subscribe((list) => {
        this.loading = false
        this.dataSource.data = list
        this.patientService.setPatient(list.find((patient: Patient) => {return patient.id === this.patientService.getPatientId()}) ?? {})
    })})
  }

  openCreation() {
    const dialogRef = this.dialog.open(PatientFormDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '90%',
      panelClass: 'dialog-with-bar',
      data: {},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngAfterViewInit()
    });
  }


  openFeedback(element: Patient) {
    const dialogRef = this.dialog.open(FeedbackDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: element},
    });
  }

  openPatient(patient: Patient){
    const dialogRef = this.dialog.open(PatientDashboardDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: patient.id},
    });
    // dialogRef.afterClosed().subscribe(result => {
    //   this.patientService.doRefresh()
    // });
  }

}

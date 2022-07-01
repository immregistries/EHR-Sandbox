import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { switchMap } from 'rxjs';
import { FeedbackDialogComponent } from 'src/app/shared/_components/feedback-table/feedback-dialog/feedback-dialog.component';
import { Facility, Patient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientFormDialogComponent } from '../patient-form/patient-form-dialog/patient-form-dialog.component';

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
  expandedElement: Patient | null = null;

  @Input() facility: Facility = {id: -1};
  @Input() title: string = 'Patients'
  loading: boolean= false

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
    if (this.expandedElement && this.expandedElement.id == event.id){
      this.expandedElement = {}
    } else {
      this.expandedElement = event
    }
    this.patientService.setPatient(this.expandedElement)

  }

  constructor(private tenantService: TenantService,
    private facilityService: FacilityService,
    private patientService: PatientService,
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
    this.facilityService.getObservableFacility().pipe(switchMap(facility =>{
      this.facility = facility
      // if (!facility || facility.id <= 0){
      //   this.tenantService.getObservableTenant().subscribe(() => {
      //     return this.patientService.readAllPatients(this.tenantService.getTenantId())
      //   })
      //   return this.patientService.readAllPatients(this.tenantService.getTenantId())
      // }
      return this.patientService.readPatients(this.tenantService.getTenantId(), facility.id)

    })).subscribe((res) => {
      this.patientService.setPatient({})
      this.loading = true
      this.patientService.getRefresh().subscribe((bool) => {
        this.dataSource.data = res
        this.loading = false
      })
    })
    // this.patientService.getObservablePatient().subscribe(patient => {
    //   this.expandedElement = patient
    // })
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

}

import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { merge, switchMap, tap } from 'rxjs';
import { Facility, EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { FeedbackTableComponent } from 'src/app/shared/_components/feedback-table/feedback-table.component';
import { PatientDashboardComponent } from '../patient-dashboard/patient-dashboard.component';
import { PatientFormComponent } from '../patient-form/patient-form.component';

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

  dataSource = new MatTableDataSource<EhrPatient>([]);
  // expandedElement: Patient | null = null;

  @Input() facility: Facility = {id: -1};
  @Input() title: string = 'Patients'
  loading: boolean= false
  lastIdSelectedBeforeRefresh: number = -1;

  columns: (keyof EhrPatient | 'alerts')[] = [
    "mrn",
    "nameLast",
    "nameFirst",
    // "nameMiddle",
    "birthDate",
    'alerts'
  ]

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  onSelection(event: EhrPatient) {
    if (this.patientService.getCurrentId() == event.id){
      this.patientService.setCurrent({})
    } else {
      this.patientService.setCurrent(event)
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
    this.dataSource.filterPredicate = (data: EhrPatient, filter: string) =>{
      return JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1
    };

    merge(
      this.facilityService.getCurrentObservable()
        .pipe(tap(facility => {this.facility = facility})),
      this.patientService.getRefresh(),
      this.facilityService.getRefresh(),
    ).subscribe(() =>  {
      this.loading = true
      this.patientService.readPatients(this.tenantService.getCurrentId(), this.facility.id).subscribe((list) => {
        this.loading = false
        this.dataSource.data = list
        this.patientService.setCurrent(list.find((patient: EhrPatient) => {return patient.id === this.patientService.getCurrentId()}) ?? {})
    })})
  }

  openCreation() {
    const dialogRef = this.dialog.open(PatientFormComponent, {
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


  openPatient(patient: EhrPatient){
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: patient.id},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

}

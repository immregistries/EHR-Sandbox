import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { switchMap } from 'rxjs';
import { Facility, Patient } from 'src/app/_model/rest';
import { FacilityService } from 'src/app/_services/facility.service';
import { PatientService } from 'src/app/_services/patient.service';
import { TenantService } from 'src/app/_services/tenant.service';
import { FhirDialogComponent } from '../../fhir-messaging/fhir-dialog/fhir-dialog.component';
import { FhirMessagingComponent } from '../../fhir-messaging/fhir-messaging.component';
import { PatientCreationComponent } from '../../_dialogs/patient-creation/patient-creation.component';

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



  columns: (keyof Patient | 'alerts')[] = [
    "nameFirst",
    "nameMiddle",
    "nameLast",
    "birthDate",
    // 'alerts'
  ]

  // Allows Date type casting in HTML template
  asDate(val: any) : Date { return val; }

  onSelection(event: Patient) {
    if (this.expandedElement && this.expandedElement.id == event.id){
      this.expandedElement = null
      this.patientService.setPatient({})
    } else {
      this.expandedElement = event
      this.patientService.setPatient(event)
    }
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
      this.patientService.getRefresh().subscribe((bool) => {
        this.dataSource.data = res
      })
    })

  }

  openCreation() {
    const dialogRef = this.dialog.open(PatientCreationComponent, {
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

  openEdition(element: Patient) {
    const dialogRef = this.dialog.open(PatientCreationComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: {patient: element},
    });
    dialogRef.afterClosed().subscribe(result => {
      this.ngAfterViewInit()
    });
  }

  openFhir(element: Patient) {
    const dialogRef = this.dialog.open(FhirDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '50%',
      panelClass: 'dialog-without-bar',
      data: {patientId: element.id},
    });
  }

}

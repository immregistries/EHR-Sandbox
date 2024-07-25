import { animate, state, style, transition, trigger } from '@angular/animations';
import { AfterViewInit, Component, Inject, Input, OnChanges, OnInit, Optional } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Facility, Feedback, EhrPatient, VaccinationEvent, ImmunizationRegistry } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { TenantService } from 'src/app/core/_services/tenant.service';
import { PatientDashboardComponent } from 'src/app/shared/_patient/patient-dashboard/patient-dashboard.component';
import { VaccinationDashboardComponent } from 'src/app/shared/_vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';

@Component({
  selector: 'app-feedback-table',
  templateUrl: './feedback-table.component.html',
  styleUrls: ['./feedback-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class FeedbackTableComponent extends AbstractDataTableComponent<Feedback> implements OnInit, AfterViewInit {
  // dataSource = new MatTableDataSource<Feedback>([]);

  @Input() facility: Facility | null = null;
  private _patient?: EhrPatient | undefined;
  public get patient(): EhrPatient | undefined {
    return this._patient;
  }
  @Input()
  public set patient(value: EhrPatient | undefined) {
    this._patient = value;
    this.refreshColumns()
    if (value) {
      this.dataSource.data = value.feedbacks ?? []
    }
  }
  private _vaccination?: VaccinationEvent | undefined;
  public get vaccination(): VaccinationEvent | undefined {
    return this._vaccination;
  }
  @Input()
  public set vaccination(value: VaccinationEvent | undefined) {
    this._vaccination = value;
    this.refreshColumns()
    if (value) {
      this.dataSource.data = value.feedbacks ?? []
    }
  }
  @Input() title: string = 'Issues'
  // loading: boolean = false

  columns!: (keyof Feedback | 'remove')[]

  registries!: ImmunizationRegistry[];
  public registryName(id: string | undefined): string {
    return this.registries?.find((reg) => id == reg.id)?.name ?? '' + id
  }

  constructor(
    private dialog: MatDialog,
    // private tenantService: TenantService,
    // private facilityService: FacilityService,
    // private feedbackService: FeedbackService,
    // private patientService: PatientService,
    // private snackBarService: SnackBarService,
    private immunizationRegistryService: ImmunizationRegistryService,
    @Optional() public _dialogRef: MatDialogRef<FeedbackTableComponent>,

    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient: EhrPatient, vaccination: VaccinationEvent }
  ) {
    super()
    this.allow_create = false
    if (data?.patient) {
      this.patient = data.patient;
    }
    if (data.vaccination) {
      this.vaccination = data.vaccination;
    }
  }

  ngOnInit(): void {
    this.refreshColumns()
    this.immunizationRegistryService.getRefresh().subscribe(() => {
      this.immunizationRegistryService.readImmRegistries().subscribe((res) => {
        this.registries = res
      })
    })
  }

  refreshColumns(): void {
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

  // refreshData(){
  //   if (this.vaccination && this.vaccination.id && this.vaccination.id > 0){
  //     this.dataSource.data = this.vaccination.feedbacks ?? []
  //   } else if (this.patient && this.patient.id && this.patient.id > 0) {
  //     this.dataSource.data = this.patient.feedbacks ?? []
  //   } else if (this.facility && this.facility.id > -1) {
  //     this.feedbackService.readFacilityFeedback(this.facility.id).subscribe((res) => {
  //       this.dataSource.data = res
  //       this.loading = false
  //     })
  //   } else {
  //     this.dataSource.data = []
  //     this.loading = false
  //   }
  // }



  openPatient(patient: EhrPatient | number) {
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: patient },
    });
  }

  openVaccination(vaccination: VaccinationEvent | number) {
    const dialogRef = this.dialog.open(VaccinationDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { vaccination: vaccination },
    });
  }

  remove(element: Feedback) {

  }

}

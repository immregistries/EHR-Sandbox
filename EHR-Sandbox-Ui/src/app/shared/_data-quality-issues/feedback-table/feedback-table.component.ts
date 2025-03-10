import { AfterViewInit, Component, Inject, Input, OnChanges, OnInit, Optional, ViewChild } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Facility, Feedback, EhrPatient, VaccinationEvent, ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientDashboardComponent } from 'src/app/shared/_patient/patient-dashboard/patient-dashboard.component';
import { VaccinationDashboardComponent } from 'src/app/shared/_vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { Hl7Location } from 'src/app/core/_model/form-structure';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { FeedbackService } from 'src/app/core/_services/feedback.service';

@Component({
  selector: 'app-feedback-table',
  templateUrl: './feedback-table.component.html',
  styleUrls: ['./feedback-table.component.scss'],

})
export class FeedbackTableComponent extends AbstractDataTableComponent<Feedback> implements OnInit, AfterViewInit {
  // dataSource = new MatTableDataSource<Feedback>([]);
  @Input() removeRefColumns: boolean = false;

  @Input() facility: Facility | null = null;
  private _patient?: EhrPatient | undefined;
  public get patient(): EhrPatient | undefined {
    return this._patient;
  }
  @Input()
  public set patient(value: EhrPatient | undefined) {
    this._patient = value;
    this.refreshColumns()
    if (value && !this.vaccination) {
      this._data_set_input = true
      this.dataSource.data = []
      this.feedbackService.readPatientFeedback(value.id ?? -1).subscribe((res) => {
        this.dataSource.data = res ?? []
      })
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
      this._data_set_input = true
      this.dataSource.data = []
      console.log("ALLOOOOO")
      this.feedbackService.readVaccinationFeedback(value.id ?? -1).subscribe((res) => {
        this.dataSource.data = res ?? []
      })
    }
  }
  @Input() title: string = 'Issues'
  // loading: boolean = false

  columns!: (keyof Feedback | 'remove')[]

  constructor(
    private dialog: MatDialog,
    // private tenantService: TenantService,
    private facilityService: FacilityService,
    private feedbackService: FeedbackService,
    // private patientService: PatientService,
    // private snackBarService: SnackBarService,
    private immunizationRegistryService: ImmunizationRegistryService,
    @Optional() public _dialogRef: MatDialogRef<FeedbackTableComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient: EhrPatient, vaccination: VaccinationEvent }
  ) {
    super()
    console.log(data)

    this.allow_create = false
    if (data?.vaccination) {
      this._data_set_input = true
      this.vaccination = data.vaccination;
    }
    if (data?.patient) {
      this._data_set_input = true
      this.patient = data.patient;
    }
  }

  ngOnInit(): void {
    this.refreshColumns()
    // if (!this._data_set_input && !this.observableSource) {
    //   this.observableRefresh = this.facilityService.getRefresh();
    //   this.observableSource = this.feedbackService.readCurrentFacilityFeedback()
    // }
  }

  refreshColumns(): void {
    this.columns = [
      "severity",
      "code",
      "hl7Locations",
      "patient",
      "vaccinationEvent",
      "timestamp",
      "iis",
      "content",
      //  "remove",
    ]
    if (this.patient || this.removeRefColumns) {
      this.columns = this.columns.filter((attribute => attribute != "patient"))
    }
    if (this.vaccination || this.removeRefColumns) {
      this.columns = this.columns.filter((attribute => attribute != "vaccinationEvent"))
    }
  }

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

  locationDisplay(hl7Locations: Hl7Location[]): string {
    let disp = ""
    hl7Locations?.forEach(element => {
      if (element.abbreviated) {
        disp += element.abbreviated + " "
      } else {
        disp += element.segmentId
        disp += element.segmentSequence ? "[" + element.segmentSequence + "]" : ""
        disp += element.fieldPosition ? "-" + element.fieldPosition : ""
        disp += element.fieldRepetition && element.fieldRepetition > 1 ? "[" + element.fieldRepetition + "]" : ""
        disp += element.componentNumber ? "." + element.componentNumber : ""
        disp += element.subComponentNumber ? "." + element.subComponentNumber + "" : ""
        disp += " "
      }
    });
    return disp;
  }

  // @ViewChild(MatSort) sort!: MatSort;
  // override ngAfterViewInit(): void {
  //   super.ngAfterViewInit()
  //   this.dataSource.sort = this.sort;
  // }
}

import { AfterViewInit, Component, Inject, Input, OnInit, Optional } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Facility, Feedback, EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { PatientDashboardComponent } from 'src/app/shared/_patient/patient-dashboard/patient-dashboard.component';
import { VaccinationDashboardComponent } from 'src/app/shared/_vaccination/vaccination-dashboard/vaccination-dashboard.component';
import { AbstractDataTableComponent } from '../../_components/abstract-data-table/abstract-data-table.component';
import { Hl7Location } from 'src/app/core/_model/form-structure';
import { FeedbackService } from 'src/app/core/_services/feedback.service';

@Component({
  selector: 'app-feedback-table',
  templateUrl: './feedback-table.component.html',
  styleUrls: ['./feedback-table.component.scss'],

})
export class FeedbackTableComponent extends AbstractDataTableComponent<Feedback> implements OnInit, AfterViewInit {

  @Input()
  removeRefColumns: boolean = false;

  @Input()
  facility: Facility | null = null;

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
      this.feedbackService.readVaccinationFeedback(value.id ?? -1).subscribe((res) => {
        this.dataSource.data = res ?? []
      })
    }
  }

  @Input()
  title: string = 'Issues'

  columns!: (keyof Feedback | 'remove')[]

  constructor(
    private dialog: MatDialog,
    private feedbackService: FeedbackService,
    @Optional() public _dialogRef: MatDialogRef<FeedbackTableComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { patient: EhrPatient, vaccination: VaccinationEvent }
  ) {
    super()
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
  }

  /**
   * Refresh table's column depending on other settings,
   * removes patient or vaccination columns if they're constant across the dataset
   */
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

  /**
   *
   * @param hl7Locations
   * @returns Readable hl7Location for display
   */
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

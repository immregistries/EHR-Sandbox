import { Component, Inject, Input, Optional } from '@angular/core';
import { MatDialog, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { Observable, of } from 'rxjs';
import { EhrGroup, EhrPatient, VaccinationEvent } from 'src/app/core/_model/rest';
import { BulkImportStatus } from 'src/app/core/_model/form-structure';
import { GroupService } from 'src/app/core/_services/group.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { FhirBulkService } from 'src/app/core/_services/_fhir/fhir-bulk.service';
import { FhirClientService } from 'src/app/core/_services/_fhir/fhir-client.service';

@Component({
  selector: 'app-group-bulk-compare',
  templateUrl: './group-bulk-compare.component.html',
  styleUrls: ['./group-bulk-compare.component.css']
})
export class GroupBulkCompareComponent {
  public loading: boolean = false

  @Input()
  public ehrGroup!: EhrGroup;
  @Input()
  public bulkImportStatus!: BulkImportStatus;
  public selectedPatient?: EhrPatient
  public remoteSelectedPatient?: EhrPatient

  public selectedVaccination: VaccinationEvent | null = null;

  public remoteVaccinations: VaccinationEvent[] = [];
  public localVaccinations: VaccinationEvent[] = [];
  private allRemoteVaccinations: VaccinationEvent[] = [];
  public remotePatients: EhrPatient[] = [];

  public outputUrlList: { "type": string, "url": string }[] = []

  constructor(
    private dialog: MatDialog,
    private fhirClient: FhirClientService,
    public vaccinationService: VaccinationService,
    public patientService: PatientService,
    public groupService: GroupService,
    public fhirBulkService: FhirBulkService,
    @Optional() public _dialogRef: MatDialogRef<GroupBulkCompareComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { ehrGroup: EhrGroup, bulkImportStatus: BulkImportStatus }) {
    if (data) {
      if (data.ehrGroup) {
        this.ehrGroup = data.ehrGroup
      }
      if (data.bulkImportStatus) {
        this.bulkImportStatus = data.bulkImportStatus
        if (this.bulkImportStatus.result) {
          this.outputUrlList = JSON.parse(this.bulkImportStatus.result).output ?? []
          /**
           * 1 view patients to solve references
           * 2 get vaccinations
           */
          this.groupService.getGroupBulkViewResult(this.ehrGroup.id ?? -1, this.outputUrlList.find((item) => item.type == "Patient")?.url ?? "").subscribe((patients) => {
            this.remotePatients = patients
            let immUrl = this.outputUrlList.find((item) => item.type == "Immunization")?.url;
            if (immUrl) {
              this.groupService.getGroupBulkViewResult(this.ehrGroup.id ?? -1, immUrl).subscribe((res) => {
                this.allRemoteVaccinations = res
              });
            }
          });
        }
      }
    }
  }

  public selectedPatientIndex?: number
  public patientIndexSelected(value: number | undefined) {
    this.selectedPatientIndex = value
    if (value != undefined && this.ehrGroup.patientList) {
      this.patientSelected(this.ehrGroup.patientList[value] ?? undefined)
    } else {
      this.patientSelected(undefined)
    }
  }

  public patientSelected(value?: EhrPatient) {
    this.patientService.setCurrent(value ?? { id: -1, names: [] })
    this.selectedPatient = value
    this.remoteVaccinations = JSON.parse(JSON.stringify(this.allRemoteVaccinations
      .filter((vac) => {
        if (!vac.patient) {
          return false
        }
        return vac.patient == this.selectedPatient?.id
      })))
    this.vaccinationService.quickReadVaccinations().subscribe((res) => {
      this.localVaccinations = res
    })
  }

  remotePatientSelected(value?: EhrPatient) {
    this.remoteSelectedPatient = value
    // this.remoteVaccinations = JSON.parse(JSON.stringify(this.allRemoteVaccinations
    //   .filter((vac) => {
    //     if (!vac.patient) {
    //       return false
    //     }
    //     return vac.patient == this.selectedPatient?.id
    //   })))
  }

  public selectedVaccinationIndex?: number
  selectVaccinationIndex(value: number | undefined) {
    this.selectedVaccinationIndex = value
  }

  selectVaccination(value: VaccinationEvent | null | undefined) {
    this.selectedVaccination = value ?? null
  }

  refreshLocalHistoryObservable(): Observable<boolean> {
    return of(true, true)
  }

}

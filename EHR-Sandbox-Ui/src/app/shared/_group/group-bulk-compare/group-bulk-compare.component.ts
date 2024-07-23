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
  loading: boolean = false

  @Input()
  ehrGroup!: EhrGroup;
  @Input()
  bulkImportStatus!: BulkImportStatus;
  selectedPatient?: EhrPatient

  selectedVaccination: VaccinationEvent | null = null;

  remoteVaccinations: VaccinationEvent[] = [];
  localVaccinations: VaccinationEvent[] = [];
  private allRemoteVaccinations: VaccinationEvent[] = [];

  outputUrlList: { "type": string, "url": string }[] = []

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
      // this.groupService.getGroupBulkViewResult(this.ehrGroup.id ?? -1, "http://localhost:8080/iis/fhir/qq/Binary/f2P6gEDoC4HeUPYaSRHGNhPJrT6PLdKQ").subscribe((patients) => {
      //   console.log('patientsRegistered', patients)
      //   this.groupService.getGroupBulkViewResult(this.ehrGroup.id ?? -1, "http://localhost:8080/iis/fhir/qq/Binary/rwB0dyKpQghcjYOx31fBrYbemtSF4LAe").subscribe((imm) => {
      //     console.log('immunizationRegistered', imm)
      //     this.allRemoteVaccinations = imm
      //   });
      // });
      if (data.bulkImportStatus) {
        this.bulkImportStatus = data.bulkImportStatus
        if (this.bulkImportStatus.result) {
          this.outputUrlList = JSON.parse(this.bulkImportStatus.result).output ?? []
          /**
           * 1 view patients to solve references
           * 2 get vaccinations
           */
          this.groupService.getGroupBulkViewResult(this.ehrGroup.id ?? -1, this.outputUrlList.find((item) => item.type == "Patient")?.url ?? "").subscribe((patients) => {
            this.groupService.getGroupBulkViewResult(this.ehrGroup.id ?? -1, this.outputUrlList.find((item) => item.type == "Immunization")?.url ?? "").subscribe((imm) => {
              this.allRemoteVaccinations = imm
            });
          });
        }
      }
    }
  }

  patientSelected(value?: EhrPatient) {
    this.patientService.setCurrent(value ?? { id: -1 })
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

  selectVaccination(value: VaccinationEvent | null | undefined) {
    this.selectedVaccination = value ?? null
  }

  refreshLocalHistoryObservable(): Observable<boolean> {
    return of(true, true)
  }

}

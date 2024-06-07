import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, Input, OnChanges, OnInit, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Group, GroupMember } from 'fhir/r5';
import { EhrPatient } from 'src/app/core/_model/rest';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { PatientService } from 'src/app/core/_services/patient.service';
import { PatientDashboardComponent } from '../patient-dashboard/patient-dashboard.component';
import { RemoteGroupService } from 'src/app/core/_services/remote-group.service';

@Component({
  selector: 'app-patient-received-table',
  templateUrl: './patient-received-table.component.html',
  styleUrls: ['./patient-received-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class PatientReceivedTableComponent implements OnInit {
  constructor(private remoteGroupService: RemoteGroupService,
    private patientService: PatientService,
    private facilityService: FacilityService,
    private dialog: MatDialog,

  ) { }
  private _group: Group | undefined | null;
  public get group(): Group | undefined | null {
    return this._group;
  }

  @Input()
  public set group(value: Group | undefined | null) {
    this._group = value;
    this.selectedElementIndex = -1
    this.dataSource.data = this.group?.member ?? []
    this.dataSource.data.forEach(element => {
      this.matching(element)
    });
  }


  selectedElementIndex?: number
  patientsForMatching: EhrPatient[] = []

  public dataSource = new MatTableDataSource<GroupMember>();

  ngOnInit(): void {
    // this.groupService.
    this.facilityService.getCurrentObservable().subscribe(() => {
      this.patientService.quickReadPatients().subscribe((patients) => {
        this.patientsForMatching = patients
      })
    })
  }

  onSelection(index: number) {
    if (this.selectedElementIndex === index) {
      this.selectedElementIndex = undefined
    } else {
      this.selectedElementIndex = index
    }

  }
  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  public columns = [
    "reference",
    "type",
    "details",
    "remove"
  ]

  matching(member: GroupMember): any {
    if (member.entity.identifier?.type?.text == "Immunization" || member.entity.reference?.startsWith("Immunization/")) {

    } else {
      let ehrPatient: EhrPatient | undefined;
      if (member.entity.reference?.startsWith("Patient/")) {
        // let id = +member.entity.reference?.split("Patient/")[1]
        // ehrPatient = this.patientsForMatching.find((patient) => patient.id == id)
      }
      if (member.entity.identifier?.value) { // TODO check for different systems ?
        let mrn = member.entity.identifier.value

        ehrPatient = this.patientsForMatching.find((patient) => this.extractMrn(patient) == mrn)
        member.entity.identifier.type = { text: "Patient" }
        // member.entity.identifier.type = {text: "Patient"}
      }
      if (member.entity.identifier?.value) { // TODO check for different systems ?
        let mrn = member.entity.identifier.value

        ehrPatient = this.patientsForMatching.find((patient) => this.extractMrn(patient) == mrn)
        member.entity.identifier.type = { text: "Patient" }
        // member.entity.identifier.type = {text: "Patient"}
      }
      if (ehrPatient) {
        member.id = ehrPatient ? ehrPatient.id + '' : undefined
        if (!member.entity.display) {
          member.entity.display = (ehrPatient.nameFirst ?? '') + " " + (ehrPatient.nameMiddle ?? '') + " " + (ehrPatient.nameLast ?? '')
        }
        member.extension?.push({ url: 'ehrPatient', valueHumanName: { family: ehrPatient.nameLast, given: [ehrPatient.nameFirst ?? '', ehrPatient.nameMiddle ?? ''] } })
      }

    }
  }

  public openPatient(patient: EhrPatient | number) {
    const dialogRef = this.dialog.open(PatientDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: patient },
    });
    dialogRef.afterClosed().subscribe(result => {
      this.patientService.doRefresh()
    });
  }

  public remove(element: GroupMember) {
    if (element.id && this.group?.id) {
      this.remoteGroupService.removeMember(this.group.id, element.id).subscribe(() => {
        this.remoteGroupService.doRefresh()
      })
    } else if (this.group?.id) {
      this.remoteGroupService.removeMember(this.group.id, undefined, element.entity.reference, element.entity.identifier).subscribe(() => {
        this.remoteGroupService.doRefresh()
      })
    }
  }

  extractMrn(element: EhrPatient): string {
    return element.identifiers?.find((identifier) => {
      return identifier.type == 'MR'
    })?.value ?? ''
  }


}

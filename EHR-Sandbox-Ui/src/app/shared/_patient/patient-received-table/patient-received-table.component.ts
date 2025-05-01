import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { EhrHumanName, EhrPatient } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { PatientDashboardComponent } from '../patient-dashboard/patient-dashboard.component';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { PatientComparePipe } from '../../_pipes/patient-compare.pipe';
import { PatientFormComponent } from '../patient-form/patient-form.component';
import { AbstractMergingTableComponent } from '../../_components/abstract-merging-table/abstract-merging-table.component';

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
export class PatientReceivedTableComponent extends AbstractMergingTableComponent<EhrPatient> {

  public columns: (keyof EhrPatient | keyof EhrHumanName | "mrn")[] = [
    "mrn",
    "names",
    "birthDate"
  ]

  @Input() title: string = 'Patients received'

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    public patientService: PatientService,
    public patientComparePipe: PatientComparePipe) {
    super()
  }

  public selectedElementIndex?: number
  public onSelection(index: number) {
    if (this.selectedElementIndex === index) {
      this.selectedElementIndex = undefined
      this.selectEmitter.emit(undefined)
    } else {
      this.selectedElementIndex = index
      this.selectEmitter.emit(this.localValues ? this.localValues[index] : undefined)
    }
  }

  @Output()
  public selectEmitter: EventEmitter<EhrPatient | undefined> = new EventEmitter<EhrPatient | undefined>();


  // matching(member: GroupMember): any {
  //   if (member.entity.identifier?.type?.text == "Immunization" || member.entity.reference?.startsWith("Immunization/")) {

  //   } else {
  //     let ehrPatient: EhrPatient | undefined;
  //     if (member.entity.reference?.startsWith("Patient/")) {
  //       // let id = +member.entity.reference?.split("Patient/")[1]
  //       // ehrPatient = this.patientsForMatching.find((patient) => patient.id == id)
  //     }
  //     if (member.entity.identifier?.value) { // TODO check for different systems ?
  //       let mrn = member.entity.identifier.value

  //       ehrPatient = this.patientsForMatching.find((patient) => this.extractMrn(patient) == mrn)
  //       member.entity.identifier.type = { text: "Patient" }
  //       // member.entity.identifier.type = {text: "Patient"}
  //     }
  //     if (member.entity.identifier?.value) { // TODO check for different systems ?
  //       let mrn = member.entity.identifier.value

  //       ehrPatient = this.patientsForMatching.find((patient) => this.extractMrn(patient) == mrn)
  //       member.entity.identifier.type = { text: "Patient" }
  //       // member.entity.identifier.type = {text: "Patient"}
  //     }
  //     if (ehrPatient) {
  //       member.id = ehrPatient ? ehrPatient.id + '' : undefined
  //       if (!member.entity.display) {
  //         member.entity.display = (ehrPatient.names[0].nameMiddle ?? '') + " " + (ehrPatient.names[0].nameMiddle ?? '') + " " + (ehrPatient.names[0].nameLast ?? '')
  //       }
  //       member.extension?.push({ url: 'ehrPatient', valueHumanName: { family: ehrPatient.names[0].nameLast, given: [ehrPatient.names[0].nameFirst ?? '', ehrPatient.names[0].nameMiddle ?? ''] } })
  //     }

  //   }
  // }

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

  extractMrn(element: EhrPatient): string {
    return element.identifiers?.find((identifier) => {
      return identifier.type == 'MR'
    })?.value ?? ''
  }

  protected updateMatchingMatrix(): void {
    this.matchingMatrix = []
    this.remoteValues?.forEach(element => {
      let length = this.matchingMatrix.push([]); // Adds new line and returns length
      this.localValues?.forEach(local => {
        this.matchingMatrix[length - 1].push(this.patientComparePipe.transform(element, local))
      })
    });
    // console.log(this.matchingMatrix)
  }

  public openMerge(remote: EhrPatient) {
    let element = JSON.parse(JSON.stringify(remote))
    let comparedLocalElement = this.comparedWith()
    element.id = comparedLocalElement ? comparedLocalElement.id : undefined
    element.primarySource = false
    // TODO Information source
    const dialogRef = this.dialog.open(PatientFormComponent, {
      maxWidth: '98vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patient: element, comparedPatient: comparedLocalElement },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.patientService.doRefresh()
        // this.updateMatchingMatrix()
      }
    });
  }

}

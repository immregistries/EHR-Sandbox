import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { EhrHumanName, EhrPatient } from 'src/app/core/_model/rest';
import { PatientService } from 'src/app/core/_services/patient.service';
import { PatientDashboardComponent } from '../patient-dashboard/patient-dashboard.component';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { PatientComparePipe } from '../../_pipes/patient-compare.pipe';

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
export class PatientReceivedTableComponent {

  public columns: (keyof EhrPatient | keyof EhrHumanName | "mrn")[] = [
    "mrn",
    "nameLast",
    "nameFirst",
    "birthDate"
  ]

  @Input() title: string = 'Patients received'
  differencesWithSelected: any = ''
  loading = false;

  expandedElement: EhrPatient | null = null;
  dataSource = new MatTableDataSource<EhrPatient>([]);
  matchingMatrix: {}[][] = []


  selectedElementIndex?: number
  private _localPatients!: EhrPatient[];
  @Input()
  set localPatients(values: EhrPatient[] | undefined | null) {
    this._localPatients = values ?? [];
    this.updateMatchingMatrix()
  }
  get localPatients() { return this._localPatients }

  @Input()
  set remotePatients(values: EhrPatient[]) {
    this.loading = false
    this.dataSource.data = values;
    this.expandedElement = values.find((EhrPatient: EhrPatient) => { return EhrPatient.id == this.expandedElement?.id }) ?? null
    this.updateMatchingMatrix()
    // this.dataSource.sort?.sort({ id: "match", start: 'desc', disableClear: false })
  }
  get remotePatients(): EhrPatient[] {
    return this.dataSource.data
  }

  private _patientToCompare!: EhrPatient | null;
  @Input()
  public get patientToCompare(): EhrPatient | null {
    return this._patientToCompare;
  }
  public set patientToCompare(value: EhrPatient | null) {
    this._patientToCompare = value;
    this.updateDifferences()
  }

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    public patientService: PatientService,
    public patientComparePipe: PatientComparePipe) { }


  onSelection(index: number) {
    if (this.selectedElementIndex === index) {
      this.selectedElementIndex = undefined
      this.selectEmitter.emit(undefined)
    } else {
      this.selectedElementIndex = index
      this.selectEmitter.emit(this._localPatients[index])

    }
  }

  @Output() selectEmitter: EventEmitter<EhrPatient | undefined> = new EventEmitter<EhrPatient | undefined>();


  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

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

  updateMatchingMatrix() {
    this.matchingMatrix = []
    this.remotePatients?.forEach(element => {
      let length = this.matchingMatrix.push([]); // Adds new line and returns length
      this.localPatients?.forEach(local => {
        this.matchingMatrix[length - 1].push(this.patientComparePipe.transform(element, local))
      })
    });
    // console.log(this.matchingMatrix)
  }

  private updateDifferences() {
    if (this._patientToCompare && this.expandedElement) {
      this.differencesWithSelected = this.patientComparePipe.transform(this.expandedElement, this._patientToCompare)
    } else {
      this.differencesWithSelected = null
    }
  }

}

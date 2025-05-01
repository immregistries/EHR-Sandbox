import { trigger, state, style, transition, animate } from '@angular/animations';
import { AfterViewInit, Component, Input } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { VaccinationEvent, Vaccine } from 'src/app/core/_model/rest';
import { VaccinationService } from 'src/app/core/_services/vaccination.service';
import { VaccinationFormComponent } from '../vaccination-form/vaccination-form.component';
import { PatientService } from 'src/app/core/_services/patient.service';
import { VaccinationComparePipe } from '../../_pipes/vaccination-compare.pipe';
import { CodeMapsPipe } from '../../_pipes/code-maps.pipe';
import { AbstractMergingTableComponent } from '../../_components/abstract-merging-table/abstract-merging-table.component';

@Component({
  selector: 'app-vaccination-received-table',
  templateUrl: './vaccination-received-table.component.html',
  styleUrls: ['./vaccination-received-table.component.scss'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class VaccinationReceivedTableComponent extends AbstractMergingTableComponent<VaccinationEvent> implements AfterViewInit {

  columns: (keyof VaccinationEvent | keyof Vaccine | "alerts" | "index" | "match")[] = [
    "vaccineCvxCode",
    "administeredDate",
    "lotNumber",
    "primarySource",
    // "index"
  ]

  @Input() title: string = 'Vaccinations received'

  private _patientId: number = -1;
  @Input()
  public set patientId(value: number | undefined) {
    this._patientId = value ?? -1;
    // this.remoteVaccinations = []
  }
  public get patientId(): number {
    return this._patientId;
  }

  constructor(private dialog: MatDialog,
    public vaccinationService: VaccinationService,
    public patientService: PatientService,
    public vaccinationComparePipe: VaccinationComparePipe,
    private codeMapsPipe: CodeMapsPipe
  ) {
    super()
  }

  ngAfterViewInit(): void {
    // Set filter rules for research
    this.dataSource.filterPredicate = this.vaccinationFilterPredicate()
    this.dataSource.sortingDataAccessor = this.sortingAccessor
    // this.dataSource.sort = new MatSort()
    // this.dataSource.sort?.register({ id: "match", start: 'desc', disableClear: false })
  }

  vaccinationFilterPredicate() {
    return (data: VaccinationEvent, filter: string): boolean => {
      if (JSON.stringify(data).trim().toLowerCase().indexOf(filter) !== -1) {
        return true
      }
      if (JSON.stringify(this.codeMapsPipe.transform(data.vaccine.vaccineCvxCode, "VACCINATION_CVX_CODE")).trim().toLowerCase().indexOf(filter) !== -1) {
        return true
      }
      return false
    }
  }

  sortingAccessor(data: any, property: string): number | string {
    if (property === 'match') {
      if (this.valueToCompare) {
        return this.isMatch(data) ? 1 : -1
      } else {
        if (this.dataSource?.data) {
          return this.hasNoMatchObject(data) ? 1 : -1
        } else {
          return 1
        }
      }
    }
    else {
      return data[property];
    }
  }

  protected updateMatchingMatrix(): void {
    this.matchingMatrix = []
    this.remoteValues?.forEach(element => {
      let length = this.matchingMatrix.push([]); // Adds new line and returns length
      this.localValues?.forEach(local => {
        this.matchingMatrix[length - 1].push(this.vaccinationComparePipe.transform(element, local))
      })
    });
    // console.log(this.matchingMatrix)
  }

  openMerge(remote: VaccinationEvent) {
    let element = JSON.parse(JSON.stringify(remote))
    element.id = this.valueToCompare ? this.valueToCompare.id : undefined
    element.primarySource = false
    // TODO Information source
    const dialogRef = this.dialog.open(VaccinationFormComponent, {
      maxWidth: '98vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-with-bar',
      data: { patientId: this.patientId, vaccination: element, comparedVaccination: this.valueToCompare },
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result) {
        this.vaccinationService.doRefresh()
      }
      // this.patientService.doRefresh()
    });
  }

  protected updateDifferences(): void {
    if (this.valueToCompare && this.expandedElement) {
      this.differencesWithSelected = this.vaccinationComparePipe.transform(this.expandedElement, this.valueToCompare)
    } else {
      this.differencesWithSelected = null
    }
  }

}

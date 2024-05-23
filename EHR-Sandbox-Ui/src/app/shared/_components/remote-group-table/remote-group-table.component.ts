import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Group } from 'fhir/r5';
import { merge } from 'rxjs';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientListComponent } from '../../_patient/patient-list/patient-list.component';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { JsonDialogService } from 'src/app/core/_services/json-dialog.service';
import { JsonDialogComponent } from '../json-dialog/json-dialog.component';
import { FhirBulkDashboardComponent } from 'src/app/shared/_fhir/fhir-bulk-dashboard/fhir-bulk-dashboard.component';
import { RemoteGroupService } from 'src/app/core/_services/remote-group.service';

const DEFAULT_SETTINGS = {
  maxWidth: '95vw',
  maxHeight: '95vh',
  height: 'fit-content',
  width: '100%',
  panelClass: 'full-screen-modal',
}
@Component({
  selector: 'app-remote-group-table',
  templateUrl: './remote-group-table.component.html',
  styleUrls: ['./remote-group-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({ height: '0px', minHeight: '0' })),
      state('expanded', style({ height: '*' })),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class RemoteGroupTableComponent {

  columns: (string)[] = [
    "identifier",
    "name",
    // "authority",
    "member"
    // "group"
  ]

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    private remoteGroupService: RemoteGroupService,
    private facilityService: FacilityService,
    private registryService: ImmunizationRegistryService,
    private snackBarService: SnackBarService,
    public jsonDialogService: JsonDialogService,
  ) { }

  ngOnInit(): void {

  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  dataSource = new MatTableDataSource<Group>([]);
  expandedElement: Group | undefined = undefined;
  loading = false;
  @Output()
  groupEmitter: EventEmitter<Group | undefined> = new EventEmitter<Group | undefined>()

  @Input() patientId?: number = -1


  ngAfterViewInit(): void {
    // Set filter rules for research
    // this.dataSource.filterPredicate = this.vaccinationFilterPredicate()

    merge(
      this.facilityService.getRefresh(),
      this.facilityService.getCurrentObservable(),
      this.registryService.getCurrentObservable(),
      this.remoteGroupService.getRefresh(),
    ).subscribe(() => {
      this.loading = true
      this.remoteGroupService.readGroups().subscribe((res) => {
        this.loading = false
        this.dataSource.data = res
        this.onSelection(res.find((g: Group) => g?.id == this.expandedElement?.id))
      })
    })
  }

  onSelection(event: Group | undefined) {
    if (this.expandedElement && this.expandedElement.id == event?.id) {
      this.expandedElement = undefined
    } else {
      this.expandedElement = event
    }
    this.groupEmitter.emit(this.expandedElement ?? undefined)

  }

  addMember(group: Group) {
    const dialogRef = this.dialog.open(PatientListComponent, DEFAULT_SETTINGS);
    dialogRef.afterClosed().subscribe(
      selectedPatientId => {
        if (group.id) {
          this.remoteGroupService.addMember(group.id, selectedPatientId).subscribe({
            next: (res) => {
              this.remoteGroupService.triggerFetch().subscribe(() => {
                this.remoteGroupService.doRefresh()
              })
            },
            error: error => {
              this.snackBarService.errorMessage(JSON.stringify(error.error))
            }
          });
        } else {
          this.snackBarService.errorMessage("Group.id undefined")
        }
      });

  }

  json(content: Group) {
    this.jsonDialogService.open(content)
    this.dialog.open(JsonDialogComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: content,
    })
  }

  openBulk(group: Group) {
    const dialogRef = this.dialog.open(FhirBulkDashboardComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'dialog-without-bar',
      data: { groupId: group.id },
    });
    dialogRef.afterClosed().subscribe(result => {
      this.remoteGroupService.doRefresh()
    });
  }



}

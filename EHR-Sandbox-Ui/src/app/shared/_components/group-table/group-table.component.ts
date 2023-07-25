import { animate, state, style, transition, trigger } from '@angular/animations';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { MatTableDataSource } from '@angular/material/table';
import { Group } from 'fhir/r5';
import { merge } from 'rxjs';
import { CodeMapsService } from 'src/app/core/_services/code-maps.service';
import { FacilityService } from 'src/app/core/_services/facility.service';
import { GroupService } from 'src/app/core/_services/group.service';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { PatientListComponent } from '../../_patient/patient-list/patient-list.component';
import { SnackBarService } from 'src/app/core/_services/snack-bar.service';
import { JsonDialogService } from 'src/app/core/_services/json-dialog.service';
import { JsonDialogComponent } from '../json-dialog/json-dialog.component';

@Component({
  selector: 'app-group-table',
  templateUrl: './group-table.component.html',
  styleUrls: ['./group-table.component.css'],
  animations: [
    trigger('detailExpand', [
      state('collapsed', style({height: '0px', minHeight: '0'})),
      state('expanded', style({height: '*'})),
      transition('expanded <=> collapsed', animate('225ms cubic-bezier(0.4, 0.0, 0.2, 1)')),
    ]),
  ],
})
export class GroupTableComponent {

  columns: (string)[] = [
    "identifier",
    "name",
    // "authority",
    "member"
    // "group"
  ]

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    private groupService: GroupService,
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
      this.groupService.getRefresh(),
    ).subscribe(() => {
      this.loading = true
      this.groupService.readGroups().subscribe((res) => {
        this.loading = false
        this.dataSource.data = res
        this.onSelection(res.find((g: Group) => g?.id == this.expandedElement?.id))
      })
    })
  }

  onSelection(event: Group | undefined) {
    if (this.expandedElement && this.expandedElement.id == event?.id){
      this.expandedElement = undefined
    } else {
      this.expandedElement = event
    }
    this.groupEmitter.emit(this.expandedElement ?? undefined)

  }

  addMember(group: Group) {

    const dialogRef = this.dialog.open(PatientListComponent, {
      maxWidth: '95vw',
      maxHeight: '95vh',
      height: 'fit-content',
      width: '100%',
      panelClass: 'full-screen-modal',
    });
    dialogRef.afterClosed().subscribe(selectedPatientId => {
      console.log(selectedPatientId)
      if (group.id) {
        this.groupService.addMember(group.id,selectedPatientId).subscribe((res) => {
          console.log(res)
          this.groupService.triggerFetch().subscribe(() => {
            this.groupService.doRefresh()
          })
        });
      } else {
        this.snackBarService.errorMessage("Group.id undefined")
      }
    });

  }

  json(content: Group){
    this.jsonDialogService.open(content)
    // this.dialog.open(JsonDialogComponent,{
    //   maxWidth: '95vw',
    //   maxHeight: '98vh',
    //   height: '5vh',
    //   width: '100%',
    //   panelClass: 'dialog-without-bar',
    //   data : content,
    // })
  }



}

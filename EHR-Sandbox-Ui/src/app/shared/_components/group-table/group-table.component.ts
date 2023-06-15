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
    // "date",
    "authority",
    // "group"
  ]

  constructor(private dialog: MatDialog,
    public codeMapsService: CodeMapsService,
    private groupService: GroupService,
    private facilityService: FacilityService,
    private registryService: ImmunizationRegistryService
    ) { }

  ngOnInit(): void {

  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.dataSource.filter = filterValue.trim().toLowerCase();
  }

  dataSource = new MatTableDataSource<Group>([]);
  expandedElement: Group | null = null;
  loading = false;
  @Output()
  groupEmitter: EventEmitter<Group | null> = new EventEmitter<Group | null>()

  @Input() patientId?: number = -1


  ngAfterViewInit(): void {
    // Set filter rules for research
    // this.dataSource.filterPredicate = this.vaccinationFilterPredicate()

    merge(
      this.facilityService.getRefresh(),
      this.facilityService.getCurrentObservable(),
      this.registryService.getCurrentObservable(),
    ).subscribe(() => {
      this.loading = true
      this.groupService.readGroups().subscribe((res) => {
        this.loading = false
        this.dataSource.data = res
        this.expandedElement = res.find((reco: Group) => {return reco.id == this.expandedElement?.id}) ?? null
      })
    })
  }

  onSelection(event: Group) {
    if (this.expandedElement && this.expandedElement.id == event.id){
      this.expandedElement = null
    } else {
      this.expandedElement = event
    }
    this.groupEmitter.emit(this.expandedElement)

  }

}

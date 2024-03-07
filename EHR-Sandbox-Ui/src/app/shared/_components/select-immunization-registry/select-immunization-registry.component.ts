import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';

@Component({
  selector: 'app-select-immunization-registry',
  templateUrl: './select-immunization-registry.component.html',
  styleUrls: ['./select-immunization-registry.component.css']
})
export class SelectImmunizationRegistryComponent implements OnInit {
  private _selectedElement: ImmunizationRegistry | undefined;
  public get selectedElement(): ImmunizationRegistry | undefined {
    return this._selectedElement;
  }
  @Input()
  public set selectedElement(value: ImmunizationRegistry | number | undefined) {
    if (typeof(value) == 'number') {
      for (const registry of this.immunizationRegistries) {
        if (registry.id && registry.id === value) {
          this._selectedElement = registry
          break;
        }
      }
    } else {
      this._selectedElement = value;
    }
  }
  @Output()
  selectedElementChange: EventEmitter<ImmunizationRegistry | undefined> = new EventEmitter()

  immunizationRegistries: ImmunizationRegistry[] = []

  constructor(public registryService: ImmunizationRegistryService) {}

  ngOnInit(): void {
    this.registryService.getRefresh().subscribe(refresh => {
      this.registryService.readImmRegistries().subscribe(res => {
        this.immunizationRegistries = res
        if (!this.registryService.getCurrentId() && this.immunizationRegistries.length > 0) {
          this.registryService.setCurrent(this.immunizationRegistries[0]);
        }
      })
    })
  }

  onSelect(newId: number): void {
    for (const registry of this.immunizationRegistries) {
      if (registry.id && registry.id === newId) {
        this.selectedElement = registry
        break;
      }
    }
    this.selectedElementChange.emit(this.selectedElement)

  }
}

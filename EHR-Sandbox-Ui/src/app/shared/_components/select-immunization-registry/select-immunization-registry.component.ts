import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';

@Component({
  selector: 'app-select-immunization-registry',
  templateUrl: './select-immunization-registry.component.html',
  styleUrls: ['./select-immunization-registry.component.css']
})
export class SelectImmunizationRegistryComponent implements OnInit {
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
        this.registryService.setCurrent(registry);
        break;
      }
    }
  }
}

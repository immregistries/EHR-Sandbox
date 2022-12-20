import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmRegistriesService } from 'src/app/core/_services/imm-registries.service';

@Component({
  selector: 'app-select-immregistry',
  templateUrl: './select-immregistry.component.html',
  styleUrls: ['./select-immregistry.component.css']
})
export class SelectImmregistryComponent implements OnInit {
  immunizationRegistries: ImmunizationRegistry[] = []

  constructor(public immRegistryService: ImmRegistriesService) {}

  ngOnInit(): void {
    this.immRegistryService.getRefresh().subscribe(refresh => {
      this.immRegistryService.readImmRegistries().subscribe(res => {
        this.immunizationRegistries = res
        if (!this.immRegistryService.getImmRegistryId() && this.immunizationRegistries.length > 0) {
          this.immRegistryService.setImmRegistry(this.immunizationRegistries[0]);
        }
      })
    })
  }

  onSelect(newId: number): void {
    for (const registry of this.immunizationRegistries) {
      if (registry.id && registry.id === newId) {
        this.immRegistryService.setImmRegistry(registry);
        break;
      }
    }
  }
}
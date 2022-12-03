import { Component, Input, OnInit } from '@angular/core';
import { ImmunizationRegistry } from '../../_model/rest';
import { ImmRegistriesService } from '../../_services/imm-registries.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  // imm!: ImmunizationRegistry

  constructor(private immRegistryService: ImmRegistriesService) { }

  ngOnInit(): void {
    // this.immRegistryService.getObservableImmRegistry().subscribe(res => {
    //   this.imm = res
    // })
  }

  // save() {
  //   this.immRegistryService.putImmRegistry(this.imm).subscribe(res => {
  //     this.immRegistryService.doRefresh();
  //     this.immRegistryService.setImmRegistry(res);
  //   })
  // }

  // new() {
  //   this.imm = {}
  //   this.immRegistryService.setImmRegistry({});
  // }

}

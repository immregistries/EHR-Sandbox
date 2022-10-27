import { Component, Input, OnInit } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmRegistriesService } from 'src/app/core/_services/imm-registries.service';
import { SettingsService } from 'src/app/core/_services/settings.service';

@Component({
  selector: 'app-settings-dialog',
  templateUrl: './settings-dialog.component.html',
  styleUrls: ['./settings-dialog.component.css']
})
export class SettingsDialogComponent implements OnInit {
  @Input()
  immunizationRegistry!: ImmunizationRegistry

  constructor(private immRegistryService: ImmRegistriesService) { }

  ngOnInit(): void {
    this.immRegistryService.getObservableImmRegistry().subscribe(res => {
      this.immunizationRegistry = res
    })
  }

  save() {
    this.immRegistryService.putImmRegistry(this.immRegistryService.getImmRegistry()).subscribe(res => {
      this.immRegistryService.doRefresh();
      this.immRegistryService.setImmRegistry(res);
    })
  }

  new() {
    this.immRegistryService.setImmRegistry({});
    // this.immRegistryService.putImmRegistry(this.imm).subscribe(res => {
    //   this.immRegistryService.setImmRegistry(res);
    //   this.immRegistryService.doRefresh();
    //   // this.imm = res
    //   console.log(res)
    // })
  }

}

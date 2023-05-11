import { Component, Input, OnInit } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { SettingsService } from 'src/app/core/_services/settings.service';

@Component({
  selector: 'app-settings-dialog',
  templateUrl: './settings-dialog.component.html',
  styleUrls: ['./settings-dialog.component.css']
})
export class SettingsDialogComponent implements OnInit {
  @Input()
  immunizationRegistry!: ImmunizationRegistry

  constructor(private immRegistryService: ImmunizationRegistryService) { }

  ngOnInit(): void {
    this.immRegistryService.getCurrentObservable().subscribe(res => {
      this.immunizationRegistry = res
    })
  }

  save() {
    this.immRegistryService.putImmRegistry(this.immRegistryService.getCurrent()).subscribe(res => {
      this.immRegistryService.doRefresh();
      this.immRegistryService.setCurrent(res);
    })
  }

  new() {
    this.immRegistryService.setCurrent({});
    // this.immRegistryService.putImmRegistry(this.imm).subscribe(res => {
    //   this.immRegistryService.setImmRegistry(res);
    //   this.immRegistryService.doRefresh();
    //   // this.imm = res
    //   console.log(res)
    // })
  }

}

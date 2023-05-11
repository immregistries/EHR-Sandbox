import { Component, OnInit } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';

@Component({
  selector: 'app-immunization-registry-menu',
  templateUrl: './immunization-registry-menu.component.html',
  styleUrls: ['./immunization-registry-menu.component.css']
})
export class ImmunizationRegistryMenuComponent implements OnInit {
  list?: ImmunizationRegistry[];

  constructor(public service: ImmunizationRegistryService) { }

  ngOnInit(): void {
    this.service.getRefresh().subscribe((bool) => {
      this.service.readImmRegistries().subscribe((res) => {
        this.list = res
        if (!this.service.getCurrentId() && this.list.length > 0) {
          this.service.setCurrent(this.list[0]);
        }
      })
      // this.service.getObservableImmRegistry().subscribe(immRegistry => {

      // })
    })
  }
}

import { Component, OnInit } from '@angular/core';
import { MatDialog } from '@angular/material/dialog';
import { ImmunizationRegistry } from 'src/app/core/_model/rest';
import { ImmunizationRegistryService } from 'src/app/core/_services/immunization-registry.service';
import { ImmunizationRegistryFormComponent } from '../../_dialogs/immunization-registry-form/immunization-registry-form.component';

@Component({
  selector: 'app-immunization-registry-menu',
  templateUrl: './immunization-registry-menu.component.html',
  styleUrls: ['./immunization-registry-menu.component.css']
})
export class ImmunizationRegistryMenuComponent implements OnInit {
  list?: ImmunizationRegistry[];

  constructor(public service: ImmunizationRegistryService, private dialog: MatDialog) { }

  ngOnInit(): void {
    this.service.getRefresh().subscribe((bool) => {
      this.service.readImmRegistries().subscribe((res) => {
        this.list = res
        if ((!this.service.getCurrentId() || this.service.getCurrentId() < 0)  && this.list.length > 0) {
          this.service.setCurrent(this.list[0]);
        }
      })
    })
  }

  openDialog(data: undefined |ImmunizationRegistry ) {
    this.dialog.open(ImmunizationRegistryFormComponent, {
      maxWidth: '95vw',
      maxHeight: '98vh',
      height: 'fit-content',
      width: '100%',
      data: data
      // panelClass: 'dialog-without-bar',
    })
  }
}

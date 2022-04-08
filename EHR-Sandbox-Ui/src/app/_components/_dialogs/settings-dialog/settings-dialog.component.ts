import { Component, OnInit } from '@angular/core';
import { ImmunizationRegistry } from 'src/app/_model/rest';
import { SettingsService } from 'src/app/_services/settings.service';

@Component({
  selector: 'app-settings-dialog',
  templateUrl: './settings-dialog.component.html',
  styleUrls: ['./settings-dialog.component.css']
})
export class SettingsDialogComponent implements OnInit {
  imm!: ImmunizationRegistry

  constructor(private settings: SettingsService) { }

  ngOnInit(): void {
    this.settings.getSettings().subscribe(res => {
      this.imm = res
    })
  }

  save() {
    this.settings.putSettings(this.imm).subscribe(res => {
      this.imm = res
      console.log(res)
    })
  }

}

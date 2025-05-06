import { Component } from '@angular/core';
import { TokenStorageService } from 'src/app/core/authentication/_services/token-storage.service';

@Component({
  selector: 'app-user-dialog',
  templateUrl: './user-dialog.component.html',
  styleUrls: ['./user-dialog.component.css']
})
export class UserDialogComponent {
  constructor(public tokenService: TokenStorageService) {

  }
}

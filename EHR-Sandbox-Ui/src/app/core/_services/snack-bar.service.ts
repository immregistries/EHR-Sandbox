import { Injectable } from '@angular/core';
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class SnackBarService {

  constructor(private _snackBar: MatSnackBar) { }

  open(message: string) {
    this.successMessage(message)
  }

  successMessage(message: string) {
    this._snackBar.open(message,`close`,{
      duration: 1000,
   })
  }

  errorMessage(message: string) {
    this._snackBar.open(message,`close`,{
      duration: 3000,
   })
  }
}

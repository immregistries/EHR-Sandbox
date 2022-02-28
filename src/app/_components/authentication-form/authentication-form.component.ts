import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { User } from 'src/app/_model/user';
import { AuthService } from 'src/app/_services/auth.service';

@Component({
  selector: 'app-authentication-form',
  templateUrl: './authentication-form.component.html',
  styleUrls: ['./authentication-form.component.css']
})
export class AuthenticationFormComponent {

  constructor(private authService: AuthService) { }


  authFormGroup = new FormGroup({username: new FormControl(''), password: new FormControl(''), email: new FormControl('')})

  onSubmit(user: User){
    console.warn(this.authFormGroup.value)
    console.warn(user)
  }



}

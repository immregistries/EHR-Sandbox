import { Component, OnInit, Output } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { User } from 'src/app/core/_model/rest';
import { AuthService } from 'src/app/core/authentication/_services/auth.service';
import { TokenStorageService } from 'src/app/core/authentication/_services/token-storage.service';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'app-authentication-form',
  templateUrl: './authentication-form.component.html',
  styleUrls: ['./authentication-form.component.scss']
})
export class AuthenticationFormComponent implements OnInit {

  constructor(private authService: AuthService, private tokenStorage: TokenStorageService) { }


  authFormGroup = new UntypedFormGroup({username: new UntypedFormControl(''), password: new UntypedFormControl(''), email: new UntypedFormControl('')})

  user: User = {id: 0, username: '', password: ''};
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  currentUsername?: string;

  @Output() success = new EventEmitter()

  ngOnInit(): void {
    this.currentUsername = this.tokenStorage.getUser()['username'];
  }

  reloadPage(): void {
    window.location.reload();
  }

  onSubmit(){
    this.user.username = this.authFormGroup.value['username']
    this.user.password = this.authFormGroup.value['password']
    this.authService.login(this.user).subscribe({
      next: (data) => {
        console.log("data", data.ok, data.status)
        console.log("data", data)
        if (data.ok) {
          if (data.body) {
            this.tokenStorage.saveToken(data.body.accessToken);
            this.tokenStorage.saveUser(data.body);
            this.isLoginFailed = false;
            this.isLoggedIn = true;
            this.success.emit(data.status);
          }
        }
        // this.reloadPage();
      },
      error: (err) => {
        console.log(err)
        this.errorMessage = err.message;
        console.warn(this.errorMessage)
        this.isLoginFailed = true;
        this.tokenStorage.signOut()
      }
    })
  }

}

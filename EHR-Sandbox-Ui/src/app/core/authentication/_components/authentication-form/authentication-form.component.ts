import { Component, OnInit, Output } from '@angular/core';
import { UntypedFormControl, UntypedFormGroup } from '@angular/forms';
import { User } from 'src/app/core/_model/rest';
import { AuthService } from 'src/app/core/authentication/_services/auth.service';
import { TokenStorageService } from 'src/app/core/authentication/_services/token-storage.service';
import { EventEmitter } from '@angular/core';

@Component({
  selector: 'app-authentication-form',
  templateUrl: './authentication-form.component.html',
  styleUrls: ['./authentication-form.component.css']
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
        this.tokenStorage.saveToken(data.accessToken);
        this.tokenStorage.saveUser(data);
        this.isLoginFailed = false;
        this.isLoggedIn = true;
        this.success.emit("success");
        // this.reloadPage();
      },
      error: (err) => {
        this.tokenStorage.signOut()
        this.errorMessage = err.error.message;
        console.warn(this.errorMessage)
        this.isLoginFailed = true;
      }
    })
  }

}

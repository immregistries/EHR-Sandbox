import { Component, OnInit } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { User } from 'src/app/_model/rest';
import { AuthService } from 'src/app/_services/auth.service';
import { TokenStorageService } from 'src/app/_services/token-storage.service';

@Component({
  selector: 'app-authentication-form',
  templateUrl: './authentication-form.component.html',
  styleUrls: ['./authentication-form.component.css']
})
export class AuthenticationFormComponent implements OnInit {

  constructor(private authService: AuthService, private tokenStorage: TokenStorageService) { }


  authFormGroup = new FormGroup({username: new FormControl(''), password: new FormControl(''), email: new FormControl('')})

  user: User = {id: 0, username: '', password: ''};
  isLoggedIn = false;
  isLoginFailed = false;
  errorMessage = '';
  currentUsername?: string;

  ngOnInit(): void {
    this.currentUsername = this.tokenStorage.getUser()['username'];
  }

  reloadPage(): void {
    window.location.reload();
  }

  onSubmit(){
    this.user.username = this.authFormGroup.value['username']
    this.user.password = this.authFormGroup.value['password']
    console.log(this.user)
    this.authService.login(this.user).subscribe({
      next: (data) => {
        this.tokenStorage.saveToken(data.accessToken);
        this.tokenStorage.saveUser(data);
        this.isLoginFailed = false;
        this.isLoggedIn = true;
        this.reloadPage();
      },
      error: (err) => {
        this.errorMessage = err.error.message;
        console.warn(this.errorMessage)
        this.isLoginFailed = true;
      }
    })
  }

}

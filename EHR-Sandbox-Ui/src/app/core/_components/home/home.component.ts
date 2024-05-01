import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SettingsService } from 'src/app/core/_services/settings.service';
import { TokenStorageService } from 'src/app/core/authentication/_services/token-storage.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.css']
})
export class HomeComponent implements OnInit {
  public loginError?: string

  constructor(
    public settings: SettingsService,
    private route: ActivatedRoute,
    private router: Router,
    public token: TokenStorageService,
    ) {
      this.route.queryParams.subscribe(params => {
        this.loginError = params['loginError'];
    });
     }

  ngOnInit(): void {
  }

  authenticationSuccess(status: number) {
    if (status == 201) {
      this.router.navigate(['steps'])
    } else {
      this.router.navigate(['dashboard'])

    }
  }

}

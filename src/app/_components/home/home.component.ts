import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { SettingsService } from 'src/app/_services/settings.service';

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
    ) {
      this.route.queryParams.subscribe(params => {
        this.loginError = params['loginError'];
    });
     }

  ngOnInit(): void {
  }

  authenticationSuccess(event: string) {
    this.router.navigate(['dashboard'])
  }

}

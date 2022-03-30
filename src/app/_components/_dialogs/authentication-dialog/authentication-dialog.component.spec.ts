import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthenticationDialogComponent } from './authentication-dialog.component';

describe('AuthenticationDialogComponent', () => {
  let component: AuthenticationDialogComponent;
  let fixture: ComponentFixture<AuthenticationDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AuthenticationDialogComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AuthenticationDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RefreshNotificationComponent } from './refresh-notification.component';

describe('RefreshNotificationComponent', () => {
  let component: RefreshNotificationComponent;
  let fixture: ComponentFixture<RefreshNotificationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RefreshNotificationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(RefreshNotificationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

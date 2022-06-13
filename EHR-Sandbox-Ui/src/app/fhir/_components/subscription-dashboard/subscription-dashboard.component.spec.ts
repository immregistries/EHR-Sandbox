import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscriptionDashboardComponent } from './subscription-dashboard.component';

describe('SubscriptionDashboardComponent', () => {
  let component: SubscriptionDashboardComponent;
  let fixture: ComponentFixture<SubscriptionDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SubscriptionDashboardComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SubscriptionDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

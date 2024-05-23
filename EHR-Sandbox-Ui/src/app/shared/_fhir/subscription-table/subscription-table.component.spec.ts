import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SubscriptionTableComponent } from './subscription-table.component';

describe('SubscriptionTableComponent', () => {
  let component: SubscriptionTableComponent;
  let fixture: ComponentFixture<SubscriptionTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ SubscriptionTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SubscriptionTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

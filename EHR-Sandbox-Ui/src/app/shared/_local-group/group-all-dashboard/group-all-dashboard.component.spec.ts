import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GroupAllDashboardComponent } from './group-all-dashboard.component';

describe('GroupAllDashboardComponent', () => {
  let component: GroupAllDashboardComponent;
  let fixture: ComponentFixture<GroupAllDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GroupAllDashboardComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GroupAllDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

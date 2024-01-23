import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RemoteGroupDashboardComponent } from './remote-group-dashboard.component';

describe('RemoteGroupDashboardComponent', () => {
  let component: RemoteGroupDashboardComponent;
  let fixture: ComponentFixture<RemoteGroupDashboardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RemoteGroupDashboardComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RemoteGroupDashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

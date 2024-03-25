import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FacilityToolsComponent } from './facility-tools.component';

describe('FacilityToolsComponent', () => {
  let component: FacilityToolsComponent;
  let fixture: ComponentFixture<FacilityToolsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FacilityToolsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(FacilityToolsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

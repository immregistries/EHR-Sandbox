import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FacilityMenuComponent } from './facility-menu.component';

describe('FacilityMenuComponent', () => {
  let component: FacilityMenuComponent;
  let fixture: ComponentFixture<FacilityMenuComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FacilityMenuComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FacilityMenuComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { FacilityCreationComponent } from './facility-creation.component';

describe('FacilityCreationComponent', () => {
  let component: FacilityCreationComponent;
  let fixture: ComponentFixture<FacilityCreationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ FacilityCreationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(FacilityCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

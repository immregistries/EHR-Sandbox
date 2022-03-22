import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationCreationComponent } from './vaccination-creation.component';

describe('VaccinationCreationComponent', () => {
  let component: VaccinationCreationComponent;
  let fixture: ComponentFixture<VaccinationCreationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationCreationComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationCreationComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

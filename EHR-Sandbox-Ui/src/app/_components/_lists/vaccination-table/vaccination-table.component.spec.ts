import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VaccinationTableComponent } from './vaccination-table.component';

describe('VaccinationTableComponent', () => {
  let component: VaccinationTableComponent;
  let fixture: ComponentFixture<VaccinationTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VaccinationTableComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VaccinationTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

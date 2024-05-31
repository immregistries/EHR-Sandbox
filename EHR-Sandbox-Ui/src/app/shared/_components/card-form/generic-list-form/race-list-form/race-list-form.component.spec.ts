import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RaceListFormComponent } from './race-list-form.component';

describe('RaceListFormComponent', () => {
  let component: RaceListFormComponent;
  let fixture: ComponentFixture<RaceListFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ RaceListFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RaceListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

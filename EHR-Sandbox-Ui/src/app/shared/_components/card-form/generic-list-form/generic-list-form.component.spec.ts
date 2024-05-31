import { ComponentFixture, TestBed } from '@angular/core/testing';

import { GenericListFormComponent } from './generic-list-form.component';

describe('GenericListFormComponent', () => {
  let component: GenericListFormComponent;
  let fixture: ComponentFixture<GenericListFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ GenericListFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(GenericListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

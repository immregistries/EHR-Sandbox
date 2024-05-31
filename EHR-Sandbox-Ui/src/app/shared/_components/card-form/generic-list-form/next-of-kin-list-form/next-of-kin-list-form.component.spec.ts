import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NextOfKinListFormComponent } from './next-of-kin-list-form.component';

describe('NextOfKinListFormComponent', () => {
  let component: NextOfKinListFormComponent;
  let fixture: ComponentFixture<NextOfKinListFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NextOfKinListFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NextOfKinListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

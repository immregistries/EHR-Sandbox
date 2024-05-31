import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NextOfKinFormComponent } from './next-of-kin-form.component';

describe('NextOfKinFormComponent', () => {
  let component: NextOfKinFormComponent;
  let fixture: ComponentFixture<NextOfKinFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NextOfKinFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NextOfKinFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

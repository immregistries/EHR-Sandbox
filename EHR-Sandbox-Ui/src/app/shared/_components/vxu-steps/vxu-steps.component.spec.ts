import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VxuStepsComponent } from './vxu-steps.component';

describe('VxuStepsComponent', () => {
  let component: VxuStepsComponent;
  let fixture: ComponentFixture<VxuStepsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ VxuStepsComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VxuStepsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

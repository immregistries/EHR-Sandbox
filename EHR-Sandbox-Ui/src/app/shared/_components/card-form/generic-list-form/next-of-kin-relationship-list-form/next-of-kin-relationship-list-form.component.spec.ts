import { ComponentFixture, TestBed } from '@angular/core/testing';

import { NextOfKinRelationshipListFormComponent } from './next-of-kin-relationship-list-form.component';

describe('NextOfKinRelationshipListFormComponent', () => {
  let component: NextOfKinRelationshipListFormComponent;
  let fixture: ComponentFixture<NextOfKinRelationshipListFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ NextOfKinRelationshipListFormComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(NextOfKinRelationshipListFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Hl7PostComponent } from './hl7-post.component';

describe('Hl7PostComponent', () => {
  let component: Hl7PostComponent;
  let fixture: ComponentFixture<Hl7PostComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ Hl7PostComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Hl7PostComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

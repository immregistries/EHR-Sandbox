import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AckDisplayComponent } from './ack-display.component';

describe('AckDisplayComponent', () => {
  let component: AckDisplayComponent;
  let fixture: ComponentFixture<AckDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AckDisplayComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AckDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

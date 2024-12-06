import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AckTableComponent } from './ack-table.component';

describe('AckTableComponent', () => {
  let component: AckTableComponent;
  let fixture: ComponentFixture<AckTableComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AckTableComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(AckTableComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

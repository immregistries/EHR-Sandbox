import { ComponentFixture, TestBed } from '@angular/core/testing';
import { SmartHealthLinkImportComponent } from './smart-health-link-import.component';

describe('SmartHealthLinkImportComponent', () => {
  let component: SmartHealthLinkImportComponent;
  let fixture: ComponentFixture<SmartHealthLinkImportComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SmartHealthLinkImportComponent]
    }).compileComponents();

    fixture = TestBed.createComponent(SmartHealthLinkImportComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

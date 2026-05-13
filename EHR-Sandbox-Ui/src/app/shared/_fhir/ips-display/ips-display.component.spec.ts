import {ComponentFixture, TestBed} from '@angular/core/testing';

import {IpsDisplayComponent} from './ips-display.component';

describe('IpsDisplayComponent', () => {
  let component: IpsDisplayComponent;
  let fixture: ComponentFixture<IpsDisplayComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [IpsDisplayComponent]
    })
      .compileComponents();

    fixture = TestBed.createComponent(IpsDisplayComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});

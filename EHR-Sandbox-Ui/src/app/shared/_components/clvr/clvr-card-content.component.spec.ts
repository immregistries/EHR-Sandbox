import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ClvrComponent } from './clvr-card-content.component';
import { QRCodeModule } from 'angularx-qrcode';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';

describe('ClvrComponent', () => {
    let component: ClvrComponent;
    let fixture: ComponentFixture<ClvrComponent>;

    beforeEach(async () => {
        await TestBed.configureTestingModule({
            declarations: [ClvrComponent],
            imports: [QRCodeModule, MatCardModule, MatButtonModule]
        })
            .compileComponents();

        fixture = TestBed.createComponent(ClvrComponent);
        component = fixture.componentInstance;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should have default qrData', () => {
        expect(component.qrData).toBe('CLVR Component Data');
    });
});

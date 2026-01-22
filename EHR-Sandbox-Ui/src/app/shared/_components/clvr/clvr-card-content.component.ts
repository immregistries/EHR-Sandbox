import { Component, ElementRef, ViewChild } from '@angular/core';
import jsPDF from 'jspdf';

@Component({
    selector: 'app-clvr-card-content',
    templateUrl: './clvr-card-content.component.html',
    styleUrls: ['./clvr-card-content.component.css']
})
export class ClvrComponent {
    @ViewChild('qrCode', { static: false }) qrCode!: ElementRef;

    // Data to be encoded in the QR code. 
    // You might want to make this an @Input() if it needs to be dynamic.
    qrData: string = 'CLVR Component Data';

    constructor() { }

    exportToPdf() {
        const doc = new jsPDF();

        // Add Title
        doc.setFontSize(20);
        doc.text('CLVR', 105, 20, { align: 'center' });

        // Get QR Code Image Data
        // The angularx-qrcode component renders a canvas or img tag.
        // We need to extract the data URL from it.
        const qrElement = this.qrCode.nativeElement.querySelector('canvas') || this.qrCode.nativeElement.querySelector('img');

        if (qrElement) {
            let imgData;
            if (qrElement.tagName.toLowerCase() === 'canvas') {
                imgData = qrElement.toDataURL('image/png');
            } else {
                imgData = qrElement.src;
            }

            // Add QR Code to PDF
            doc.addImage(imgData, 'PNG', 55, 40, 100, 100);
        }

        doc.save('clvr-export.pdf');
    }
}

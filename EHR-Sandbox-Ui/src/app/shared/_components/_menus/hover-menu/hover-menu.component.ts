import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-hover-menu',
  templateUrl: './hover-menu.component.html',
  styleUrls: ['./hover-menu.component.css']
})
/**
 * Hover menu selection
 * Component inspired by stack overflow response
 * https://stackoverflow.com/questions/53618333/how-to-open-and-close-angular-mat-menu-on-hover
 */
export class HoverMenuComponent {

  timedOutCloser: any;

  constructor() { }

  mouseEnter(trigger: any) {
    if (this.timedOutCloser) {
      clearTimeout(this.timedOutCloser);
    }
    trigger.openMenu();
  }

  mouseLeave(trigger: any) {
    this.timedOutCloser = setTimeout(() => {
      trigger.closeMenu();
    }, 150);
  }

}

import { Component, Input, OnInit, TemplateRef, ViewChild } from '@angular/core';

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

  @Input()
  disabled: boolean = false

  timedOutCloser: any;

  constructor() { }

  mouseEnter(trigger: any) {
    if (!this.disabled) {
      if (this.timedOutCloser) {
        clearTimeout(this.timedOutCloser);
      }
      trigger.openMenu();
    }
  }

  mouseLeave(trigger: any) {
    this.timedOutCloser = setTimeout(() => {
      trigger.closeMenu();
    }, 150);
  }

  // @ViewChild('subMenuTemplate') subMenuTemplate: TemplateRef<any>;
  // @ViewChild('nestedSubMenuTemplate') nestedSubMenuTemplate: TemplateRef<any>;

  // getSubMenu(item: any) {
  //   return this.subMenuTemplate;
  // }

  // getNestedSubMenu(item: any) {
  //   return this.nestedSubMenuTemplate;
  // }

}

import { ChangeDetectorRef, Component, ElementRef, Input, ViewChild, ViewEncapsulation } from '@angular/core';
import { MatTabGroup, MatTabsConfig } from '@angular/material/tabs';

@Component({
  selector: 'app-tab-with-menu',
  templateUrl: './tab-with-menu.component.html',
  styleUrls: ['./tab-with-menu.component.css'],
  encapsulation: ViewEncapsulation.None,
})
export class TabWithMenuComponent {
  /**
   * Temporary solution to input tab label
   */
  @Input()
  public label: string = ""
  @Input()
  public label2?: string;

  // constructor(elementRef: ElementRef, changeDetectorRef: ChangeDetectorRef, defaultConfig?: MatTabsConfig, animationMode?: string) {
  //   super(elementRef, changeDetectorRef, defaultConfig, animationMode)
  //  }

  @ViewChild('tabs', {static: false}) tabGroup!: MatTabGroup;

  groupId: string = ""

  ngAfterViewInit(): void {
    this.tabGroup.selectedIndex = 1;
  }

}

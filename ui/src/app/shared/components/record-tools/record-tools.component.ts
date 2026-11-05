import {ChangeDetectionStrategy, Component, input, output} from '@angular/core';
import {ButtonModule} from 'primeng/button';
import {TooltipModule} from 'primeng/tooltip';

/**
 * Action identifiers shared by the patient and vaccination row tools.
 * Each maps to a dialog in the old UI; the dialogs themselves are not built
 * yet, so the host decides what a request does.
 */
export type RecordToolAction =
  | 'edit'
  | 'hl7'
  | 'fhir'
  | 'ips'
  | 'localCopy'
  | 'remoteHistory'
  | 'json'
  | 'open';

export interface RecordTool {
  action: RecordToolAction;
  label: string;
  icon: string;
  tooltip: string;
  /** Icon-only button. */
  compact?: boolean;
  /** Dialog not implemented yet. */
  pending?: boolean;
}

export const PATIENT_TOOLS: RecordTool[] = [
  {
    action: 'open',
    label: 'Open',
    icon: 'pi pi-external-link',
    tooltip: 'Open patient dashboard',
    compact: true,
    pending: true
  },
  {action: 'edit', label: 'Edit', icon: 'pi pi-pencil', tooltip: 'Edit patient', compact: true, pending: true},
  {action: 'hl7', label: 'HL7v2', icon: 'pi pi-send', tooltip: 'Send HL7v2 QBP message', pending: true},
  {action: 'fhir', label: 'FHIR', icon: 'pi pi-share-alt', tooltip: 'Send FHIR resource', pending: true},
  {action: 'ips', label: 'IPS', icon: 'pi pi-file-export', tooltip: 'Send FHIR IPS bundle', pending: true},
  {
    action: 'localCopy',
    label: 'Local facility',
    icon: 'pi pi-copy',
    tooltip: 'Copy to another local facility',
    pending: true
  },
  {
    action: 'remoteHistory',
    label: 'Remote history',
    icon: 'pi pi-cloud-download',
    tooltip: 'Fetch and merge remote vaccination history',
    pending: true
  },
  {action: 'json', label: 'JSON', icon: 'pi pi-code', tooltip: 'Inspect the raw record', pending: true},
];

export const VACCINATION_TOOLS: RecordTool[] = [
  {action: 'edit', label: 'Edit', icon: 'pi pi-pencil', tooltip: 'Edit immunization', compact: true, pending: true},
  {action: 'hl7', label: 'HL7v2', icon: 'pi pi-send', tooltip: 'Send HL7v2 VXU message', pending: true},
  {action: 'fhir', label: 'FHIR', icon: 'pi pi-share-alt', tooltip: 'Send FHIR resource', pending: true},
  {
    action: 'localCopy',
    label: 'Local facility',
    icon: 'pi pi-copy',
    tooltip: 'Copy to another local facility',
    pending: true
  },
  {action: 'json', label: 'JSON', icon: 'pi pi-code', tooltip: 'Inspect the raw record', pending: true},
];

/** Row action bar, shown in a table's expanded row. */
@Component({
  selector: 'app-record-tools',
  standalone: true,
  imports: [ButtonModule, TooltipModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="tools">
      @for (tool of tools(); track tool.action) {
        <p-button
          [label]="tool.compact ? undefined : tool.label"
          [icon]="tool.icon"
          size="small"
          [text]="tool.compact === true"
          [outlined]="!tool.compact"
          severity="secondary"
          [pTooltip]="tool.pending ? tool.tooltip + ' (not implemented yet)' : tool.tooltip"
          tooltipPosition="top"
          [ariaLabel]="tool.label"
          (onClick)="toolSelect.emit(tool.action)"
        />
      }
      <ng-content />
    </div>
  `,
  styles: `
    .tools {
      display: flex;
      flex-wrap: wrap;
      align-items: center;
      gap: 0.5rem;
      padding: 0.625rem 0.75rem;
    }
  `,
})
export class RecordToolsComponent {
  readonly tools = input.required<RecordTool[]>();
  readonly toolSelect = output<RecordToolAction>();
}

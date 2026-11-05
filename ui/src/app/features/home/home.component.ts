import {ChangeDetectionStrategy, Component, inject} from '@angular/core';
import {RouterLink} from '@angular/router';
import {Store} from '@ngrx/store';
import {ButtonModule} from 'primeng/button';
import {CardModule} from 'primeng/card';
import {MessageModule} from 'primeng/message';
import {TagModule} from 'primeng/tag';
import {selectRouteTenantId} from '../../core/store/router.selectors';
import {selectCurrentTenant} from '../../core/store/tenant/tenant.selectors';

interface Capability {
  icon: string;
  title: string;
  description: string;
}

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink, ButtonModule, CardModule, MessageModule, TagModule],
  changeDetection: ChangeDetectionStrategy.OnPush,
  template: `
    <div class="home">
      <section class="hero">
        <div class="hero-text">
          <h1>EHR Sandbox</h1>
          <p>
            A testing tool built by AIRA and NIST alongside the IIS Sandbox, simulating the
            behaviour of an Electronic Health Record so you can exercise an immunization
            registry end to end.
          </p>
          <div class="hero-actions">
            @if (tenantId() > 0) {
              <p-button
                label="Go to dashboard"
                icon="pi pi-th-large"
                [routerLink]="['/t', tenantId()]"
              />
            } @else {
              <p-button label="Select a tenant" icon="pi pi-building" routerLink="/tenants" />
            }
            <p-button
              label="GitHub repository"
              icon="pi pi-github"
              severity="secondary"
              [outlined]="true"
              (onClick)="openRepository()"
            />
          </div>
        </div>
      </section>

      <p-message severity="warn" styleClass="full-width">
        This system is for test purposes only. Do not submit production data — it is not
        secured for personally identifiable health information.
      </p-message>

      <section class="capabilities">
        @for (capability of capabilities; track capability.title) {
          <p-card>
            <div class="capability">
              <i [class]="capability.icon"></i>
              <div>
                <h3>{{ capability.title }}</h3>
                <p>{{ capability.description }}</p>
              </div>
            </div>
          </p-card>
        }
      </section>
    </div>
  `,
  styleUrl: './home.component.scss',
})
export class HomeComponent {
  private readonly store = inject(Store);

  readonly tenantId = this.store.selectSignal(selectRouteTenantId);
  readonly tenant = this.store.selectSignal(selectCurrentTenant);

  readonly capabilities: Capability[] = [
    {
      icon: 'pi pi-user-plus',
      title: 'Patients & vaccinations',
      description: 'Enter or randomly generate patient and immunization records.',
    },
    {
      icon: 'pi pi-send',
      title: 'HL7v2 messaging',
      description: 'Build and send VXU and QBP messages to a registry, step by step.',
    },
    {
      icon: 'pi pi-share-alt',
      title: 'FHIR messaging',
      description: 'Send resources over FHIR and inspect the registry outcome.',
    },
    {
      icon: 'pi pi-database',
      title: 'Bulk data',
      description: 'Experimental client for FHIR Bulk Data group exports and ndjson imports.',
    },
    {
      icon: 'pi pi-bell',
      title: 'Subscriptions',
      description: 'Subscribe to an IIS Sandbox instance and receive data quality issues.',
    },
    {
      icon: 'pi pi-clone',
      title: 'Groups',
      description: 'Create and manage FHIR Groups against a connected registry.',
    },
  ];

  openRepository(): void {
    window.open('https://github.com/immregistries/EHR-Sandbox', '_blank', 'noopener');
  }
}

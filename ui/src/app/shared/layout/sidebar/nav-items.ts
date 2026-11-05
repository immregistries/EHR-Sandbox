/**
 * Navigation model for the sidebar. Links are built from the tenant/facility
 * ids in the url, so every destination stays directly linkable.
 */
export interface NavItem {
  label: string;
  icon: string;
  /** Route segments appended to the current context prefix. */
  path: string[];
  tooltip?: string;
  /** Where the item becomes reachable. */
  scope: 'global' | 'tenant' | 'facility';
  /** Not yet implemented — rendered but visibly marked. */
  pending?: boolean;
}

export interface NavSection {
  title: string;
  scope: 'global' | 'tenant' | 'facility';
  items: NavItem[];
}

export const NAV_SECTIONS: NavSection[] = [
  {
    title: 'Overview',
    scope: 'global',
    items: [
      {
        label: 'Home',
        icon: 'pi pi-home',
        path: ['/home'],
        tooltip: 'Home and introduction',
        scope: 'global',
      },
      {
        label: 'Tenants',
        icon: 'pi pi-building',
        path: ['/tenants'],
        tooltip: 'Select or create a tenant',
        scope: 'global',
      },
      {
        label: 'IIS Settings',
        icon: 'pi pi-cog',
        path: ['/settings'],
        tooltip: 'IIS connection settings',
        scope: 'global',
        pending: true,
      },
    ],
  },
  {
    title: 'Tenant',
    scope: 'tenant',
    items: [
      {
        label: 'Facilities',
        icon: 'pi pi-sitemap',
        path: ['facilities'],
        tooltip: 'Facilities management',
        scope: 'tenant',
      },
      {
        label: 'Clinicians',
        icon: 'pi pi-id-card',
        path: ['clinicians'],
        tooltip: 'Clinicians management',
        scope: 'tenant',
        pending: true,
      },
    ],
  },
  {
    title: 'Facility',
    scope: 'facility',
    items: [
      {
        label: 'Dashboard',
        icon: 'pi pi-th-large',
        path: ['dashboard'],
        tooltip: 'Patients & immunizations',
        scope: 'facility',
      },
      {
        label: 'Patients',
        icon: 'pi pi-users',
        path: ['patients'],
        tooltip: 'Patient list',
        scope: 'facility',
      },
      {
        label: 'Groups',
        icon: 'pi pi-clone',
        path: ['groups'],
        tooltip: 'Group management',
        scope: 'facility',
        pending: true,
      },
    ],
  },
  {
    title: 'Messaging',
    scope: 'facility',
    items: [
      {
        label: 'VXU Step by step',
        icon: 'pi pi-send',
        path: ['vxu-steps'],
        tooltip: 'Build a VXU message step by step',
        scope: 'facility',
        pending: true,
      },
      {
        label: 'FHIR',
        icon: 'pi pi-share-alt',
        path: ['fhir'],
        tooltip: 'FHIR client dashboard',
        scope: 'facility',
        pending: true,
      },
      {
        label: 'Bulk Data',
        icon: 'pi pi-database',
        path: ['bulk'],
        tooltip: 'Bulk data import testing',
        scope: 'facility',
        pending: true,
      },
      {
        label: 'Subscriptions',
        icon: 'pi pi-bell',
        path: ['subscription'],
        tooltip: 'Subscription dashboard',
        scope: 'facility',
        pending: true,
      },
      {
        label: 'SH Links',
        icon: 'pi pi-link',
        path: ['sh-links'],
        tooltip: 'Smart Health Links import',
        scope: 'facility',
        pending: true,
      },
    ],
  },
  {
    title: 'Quality',
    scope: 'facility',
    items: [
      {
        label: 'Data Quality Issues',
        icon: 'pi pi-exclamation-triangle',
        path: ['feedback'],
        tooltip: 'Feedback reported by the IIS',
        scope: 'facility',
        pending: true,
      },
      {
        label: 'Acknowledgments',
        icon: 'pi pi-inbox',
        path: ['ack'],
        tooltip: 'Acknowledgment demo',
        scope: 'facility',
        pending: true,
      },
    ],
  },
];

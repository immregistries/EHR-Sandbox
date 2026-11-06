import {ComponentHarness, renderComponent} from '../../../../testing';
import {SidebarComponent} from './sidebar.component';

describe('SidebarComponent', () => {
  function labels(h: ComponentHarness<SidebarComponent>): string[] {
    return h.queryAll<HTMLElement>('.nav-label').map((el) => (el.textContent ?? '').trim());
  }

  it('shows only global items when no tenant is selected', async () => {
    const h = await renderComponent(SidebarComponent, {state: {route: {}}});

    expect(labels(h)).toEqual(['Home', 'Tenants', 'IIS Settings']);
  });

  it('adds tenant-scoped items once a tenant is in the url', async () => {
    const h = await renderComponent(SidebarComponent, {state: {route: {tenantId: 3}}});

    expect(labels(h)).toContain('Facilities');
    // Facility scope is still out of reach.
    expect(labels(h)).not.toContain('Dashboard');
  });

  it('adds facility-scoped items once a facility is in the url', async () => {
    const h = await renderComponent(SidebarComponent, {
      state: {route: {tenantId: 3, facilityId: 8}},
    });

    expect(labels(h)).toContain('Dashboard');
    expect(labels(h)).toContain('Data Quality Issues');
  });

  it('builds scoped links against the current tenant and facility', async () => {
    const h = await renderComponent(SidebarComponent, {
      state: {route: {tenantId: 3, facilityId: 8}},
    });

    const hrefs = h.queryAll<HTMLAnchorElement>('a.nav-link').map((a) => a.getAttribute('href'));

    expect(hrefs).toContain('/t/3/f/8/dashboard');
    expect(hrefs).toContain('/t/3/facilities');
  });

  it('keeps the selected context on context-free pages', async () => {
    const h = await renderComponent(SidebarComponent, {
      state: {route: {tenantId: 3, facilityId: 8}},
    });

    const hrefs = h.queryAll<HTMLAnchorElement>('a.nav-link').map((a) => a.getAttribute('href'));

    // Home and Tenants carry the deepest prefix, so visiting them does not
    // drop the tenant or the facility.
    expect(hrefs).toContain('/t/3/f/8/home');
    expect(hrefs).toContain('/t/3/f/8/tenants');
    expect(hrefs).not.toContain('/home');
  });

  it('prefixes context-free pages with the tenant alone when no facility is selected', async () => {
    const h = await renderComponent(SidebarComponent, {state: {route: {tenantId: 3}}});

    const hrefs = h.queryAll<HTMLAnchorElement>('a.nav-link').map((a) => a.getAttribute('href'));

    expect(hrefs).toContain('/t/3/home');
    expect(hrefs).toContain('/t/3/tenants');
  });

  it('uses bare links when nothing is selected', async () => {
    const h = await renderComponent(SidebarComponent, {state: {route: {}}});

    const hrefs = h.queryAll<HTMLAnchorElement>('a.nav-link').map((a) => a.getAttribute('href'));

    expect(hrefs).toContain('/home');
    expect(hrefs).toContain('/tenants');
  });

  it('offers Clinicians as a live tenant destination', async () => {
    const h = await renderComponent(SidebarComponent, {state: {route: {tenantId: 3}}});

    const link = h
      .queryAll<HTMLAnchorElement>('a.nav-link')
      .find((a) => a.textContent?.includes('Clinicians'));

    expect(link?.getAttribute('href')).toBe('/t/3/clinicians');
    // No "soon" badge: the page is implemented.
    expect(link?.querySelector('.nav-badge')).toBeNull();
  });

  it('hides labels when collapsed', async () => {
    const h = await renderComponent(SidebarComponent, {
      state: {route: {tenantId: 3}},
      inputs: {collapsed: true},
    });

    expect(labels(h)).toEqual([]);
    expect(h.query('.sidebar--collapsed')).not.toBeNull();
  });
});

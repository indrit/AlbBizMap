// Bismillah Hir Rahman Nir Raheem
import type { Business } from '../data/sampleData';

export interface SuggestionItem {
  type: 'city' | 'business';
  title: string;
  subtitle: string;
  lat?: number;
  lng?: number;
  businessId?: string;
}

export const POPULAR_CITIES = [
  { name: 'Tirana, Albania', lat: 41.3275, lng: 19.8187 },
  { name: 'Padova, Veneto, Italy', lat: 45.4064, lng: 11.8768 },
  { name: 'Prishtina, Kosovo', lat: 42.6629, lng: 21.1655 },
  { name: 'New York, NY, USA', lat: 40.7128, lng: -74.0060 },
  { name: 'Bronx, NY, USA', lat: 40.8448, lng: -73.8648 },
  { name: 'Stamford, CT, USA', lat: 41.0534, lng: -73.5387 },
  { name: 'Durrës, Albania', lat: 41.3246, lng: 19.4565 },
  { name: 'Shkodër, Albania', lat: 42.0693, lng: 19.5126 },
  { name: 'Vlorë, Albania', lat: 40.4667, lng: 19.4900 }
];

export function getSearchSuggestions(query: string, businesses: Business[]): SuggestionItem[] {
  const trimmed = query.trim().toLowerCase();
  if (!trimmed) return [];

  const results: SuggestionItem[] = [];

  // Match cities
  POPULAR_CITIES.forEach(city => {
    if (city.name.toLowerCase().includes(trimmed)) {
      results.push({
        type: 'city',
        title: city.name,
        subtitle: 'City / Region',
        lat: city.lat,
        lng: city.lng
      });
    }
  });

  // Match businesses by name, category, or address
  businesses.forEach(biz => {
    if (
      biz.name.toLowerCase().includes(trimmed) ||
      biz.category.toLowerCase().includes(trimmed) ||
      biz.city.toLowerCase().includes(trimmed) ||
      biz.address.toLowerCase().includes(trimmed)
    ) {
      results.push({
        type: 'business',
        title: biz.name,
        subtitle: `${biz.category} • ${biz.city}, ${biz.country}`,
        lat: biz.lat,
        lng: biz.lng,
        businessId: biz.id
      });
    }
  });

  return results.slice(0, 7); // Top 7 relevant suggestions
}

export function renderSuggestionsDropdownHTML(suggestions: SuggestionItem[]): string {
  if (suggestions.length === 0) return '';

  const itemsHTML = suggestions.map((item, idx) => `
    <div class="suggestion-item" data-idx="${idx}" style="
      padding: 10px 16px;
      display: flex;
      align-items: center;
      gap: 12px;
      cursor: pointer;
      border-bottom: 1px solid #f2f2f2;
      transition: background 0.15s ease;
    ">
      <span style="font-size: 16px;">${item.type === 'city' ? '🏙️' : '🏢'}</span>
      <div style="flex: 1;">
        <div style="font-size: 14px; font-weight: 700; color: #1A1A1A;">${item.title}</div>
        <div style="font-size: 11px; color: #757575;">${item.subtitle}</div>
      </div>
      <span style="font-size: 11px; color: #E41E20; font-weight: 600;">Select →</span>
    </div>
  `).join('');

  return `
    <div id="suggestions-dropdown" style="
      position: absolute;
      top: 100%;
      left: 0;
      width: 100%;
      background: #ffffff;
      border-radius: 16px;
      box-shadow: 0 10px 30px rgba(0,0,0,0.15);
      border: 1px solid rgba(0,0,0,0.06);
      margin-top: 8px;
      overflow: hidden;
      z-index: 1200;
    ">
      ${itemsHTML}
    </div>
  `;
}

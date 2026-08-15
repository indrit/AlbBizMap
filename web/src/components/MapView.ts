// Bismillah Hir Rahman Nir Raheem
import L from 'leaflet';
import type { Business } from '../data/sampleData';

export class MapView {
  private map: L.Map | null = null;
  private markers: L.Marker[] = [];
  private onMarkerClick: (biz: Business) => void;

  constructor(containerId: string, onMarkerClick: (biz: Business) => void) {
    this.onMarkerClick = onMarkerClick;
    this.initMap(containerId);
  }

  private initMap(containerId: string) {
    // Default center: Tirana, Albania (41.3275, 19.8187)
    this.map = L.map(containerId, {
      center: [41.3275, 19.8187],
      zoom: 13,
      zoomControl: false
    });

    L.tileLayer('https://{s}.basemaps.cartocdn.com/rastertiles/voyager/{z}/{x}/{y}{r}.png', {
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors &copy; <a href="https://carto.com/">CARTO</a>',
      maxZoom: 19
    }).addTo(this.map);

    L.control.zoom({ position: 'bottomright' }).addTo(this.map);
  }

  public renderBusinesses(businesses: Business[]) {
    if (!this.map) return;

    this.markers.forEach(m => m.remove());
    this.markers = [];

    businesses.forEach(biz => {
      if (!biz.lat || !biz.lng) return;

      const isGold = biz.isSponsored;
      const pinColor = isGold ? '#FFD700' : '#E41E20';
      const iconHtml = `
        <div style="
          background: ${pinColor};
          width: 38px;
          height: 38px;
          border-radius: 50%;
          border: 3px solid #ffffff;
          box-shadow: 0 4px 12px rgba(0,0,0,0.3);
          display: flex;
          align-items: center;
          justify-content: center;
          color: ${isGold ? '#000' : '#fff'};
          font-weight: bold;
          font-size: 16px;
          cursor: pointer;
        ">
          ${biz.isAlbanianOwned ? '🇦🇱' : '📍'}
        </div>
      `;

      const customIcon = L.divIcon({
        html: iconHtml,
        className: 'custom-leaflet-pin',
        iconSize: [38, 38],
        iconAnchor: [19, 19]
      });

      const marker = L.marker([biz.lat, biz.lng], { icon: customIcon }).addTo(this.map!);
      
      marker.bindPopup(`
        <div style="font-family: 'Inter', sans-serif; padding: 4px;">
          <h4 style="margin: 0 0 4px 0; font-size: 16px; color: #1A1A1A;">${biz.name}</h4>
          <p style="margin: 0 0 6px 0; font-size: 12px; color: #E41E20; font-weight: 600;">${biz.category} • ${biz.city}</p>
          <p style="margin: 0 0 8px 0; font-size: 12px; color: #666;">${biz.description}</p>
          <button id="popup-btn-${biz.id}" style="
            background: #E41E20;
            color: #fff;
            border: none;
            padding: 6px 12px;
            border-radius: 14px;
            font-size: 12px;
            font-weight: 600;
            cursor: pointer;
            width: 100%;
          ">View Details</button>
        </div>
      `);

      marker.on('popupopen', () => {
        const btn = document.getElementById(`popup-btn-${biz.id}`);
        if (btn) {
          btn.addEventListener('click', () => this.onMarkerClick(biz));
        }
      });

      this.markers.push(marker);
    });
  }

  public setCenter(lat: number, lng: number, zoom = 13) {
    if (this.map) {
      this.map.flyTo([lat, lng], zoom, { duration: 1.5 });
    }
  }
}

// Bismillah Hir Rahman Nir Raheem
import type { Business } from '../data/sampleData';

export function createBusinessModalHTML(biz: Business): string {
  const photo = biz.photos && biz.photos.length > 0 ? biz.photos[0] : 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5';

  return `
    <div class="modal-overlay" id="biz-detail-modal">
      <div class="modal-card" style="position: relative;">
        <!-- Clear Dark Close Button -->
        <button class="modal-close-btn" id="close-biz-modal" style="
          position: absolute;
          top: 14px;
          right: 14px;
          background: rgba(0,0,0,0.75);
          color: #ffffff;
          border: 2px solid rgba(255,255,255,0.4);
          width: 38px;
          height: 38px;
          border-radius: 50%;
          font-size: 18px;
          font-weight: bold;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 100;
          box-shadow: 0 4px 12px rgba(0,0,0,0.3);
          transition: all 0.2s ease;
        ">✕</button>
        
        <div style="width: 100%; height: 220px; border-radius: 16px; overflow: hidden; margin-bottom: 20px; position: relative;">
          <img src="${photo}" style="width: 100%; height: 100%; object-fit: cover;" />
          <div style="position: absolute; bottom: 12px; left: 12px; display: flex; gap: 8px;">
            ${biz.isVerified ? '<span style="background: #007bff; color: #fff; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: bold;">✔️ Verified</span>' : ''}
            ${biz.isAlbanianOwned ? '<span style="background: #E41E20; color: #fff; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: bold;">🇦🇱 Albanian Owned</span>' : ''}
            ${biz.isPremium ? '<span style="background: #FFD700; color: #000; padding: 4px 10px; border-radius: 6px; font-size: 11px; font-weight: bold;">👑 Premium</span>' : ''}
          </div>
        </div>

        <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
          <div>
            <h2 style="font-family: 'Outfit', sans-serif; font-size: 26px; font-weight: 800; margin-bottom: 4px;">${biz.name}</h2>
            <p style="color: #E41E20; font-weight: 700; font-size: 14px;">${biz.category} • ${biz.city}, ${biz.country}</p>
          </div>
          <div style="text-align: right;">
            <div style="font-size: 20px; font-weight: 800; color: #FFD700;">★ ${biz.rating.toFixed(1)}</div>
            <div style="font-size: 12px; color: #888;">${biz.reviewCount} reviews</div>
          </div>
        </div>

        <p style="font-size: 15px; color: #444; line-height: 1.5; margin-bottom: 16px;">${biz.description}</p>
        
        ${biz.longDescription ? `
          <div style="background: #f9f9f9; padding: 14px; border-radius: 12px; margin-bottom: 20px; font-size: 14px; color: #333; line-height: 1.6;">
            <strong>About:</strong> ${biz.longDescription}
          </div>
        ` : ''}

        <div style="border-top: 1px solid #eee; border-bottom: 1px solid #eee; padding: 16px 0; margin-bottom: 20px; display: flex; flex-direction: column; gap: 10px; font-size: 14px;">
          <div>📍 <strong>Address:</strong> ${biz.address}, ${biz.city}, ${biz.country}</div>
          <div>📞 <strong>Phone:</strong> <a href="tel:${biz.phone}" style="color: #E41E20; text-decoration: none; font-weight: 600;">${biz.phone}</a></div>
          ${biz.email ? `<div>✉️ <strong>Email:</strong> <a href="mailto:${biz.email}" style="color: #E41E20; text-decoration: none;">${biz.email}</a></div>` : ''}
          ${biz.website ? `<div>🌐 <strong>Website:</strong> <a href="${biz.website}" target="_blank" style="color: #E41E20; text-decoration: none;">${biz.website}</a></div>` : ''}
        </div>

        <div style="display: flex; gap: 12px;">
          <a href="https://maps.google.com/?q=${encodeURIComponent(biz.name + ' ' + biz.address + ' ' + biz.city)}" target="_blank" class="btn-primary" style="flex: 1; text-align: center; text-decoration: none; padding: 14px;">
            🗺️ Get Directions
          </a>
        </div>
      </div>
    </div>
  `;
}

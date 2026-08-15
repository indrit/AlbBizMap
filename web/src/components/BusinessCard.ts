// Bismillah Hir Rahman Nir Raheem
import type { Business } from '../data/sampleData';

export function createBusinessCardHTML(biz: Business): string {
  const photo = biz.photos && biz.photos.length > 0
    ? biz.photos[0]
    : 'https://images.unsplash.com/photo-1555396273-367ea4eb4db5';

  return `
    <div class="biz-card" data-biz-id="${biz.id}">
      <div class="biz-card-img-wrapper">
        <img src="${photo}" alt="${biz.name}" class="biz-card-img" />
        ${biz.isSponsored ? '<span class="badge-tag badge-sponsored">⭐ Sponsored</span>' : ''}
        ${biz.isAlbanianOwned && !biz.isSponsored ? '<span class="badge-tag badge-albanian">🇦🇱 Albanian Owned</span>' : ''}
      </div>
      <div class="biz-card-body">
        <div class="biz-card-header">
          <div>
            <h3 class="biz-card-name">${biz.name} ${biz.isVerified ? '✔️' : ''}</h3>
            <span class="biz-card-category">${biz.category} • ${biz.city}, ${biz.country}</span>
          </div>
        </div>
        <p class="biz-card-desc">${biz.description}</p>
        <div class="biz-card-footer">
          <div class="rating-badge">
            <span style="color: #FFD700;">★</span> ${biz.rating.toFixed(1)} <span style="color: #999; font-weight: normal;">(${biz.reviewCount})</span>
          </div>
          <span style="font-size: 12px; font-weight: 600; color: #E41E20;">View Details →</span>
        </div>
      </div>
    </div>
  `;
}

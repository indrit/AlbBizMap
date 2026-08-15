// Bismillah Hir Rahman Nir Raheem

export function createAddBusinessModalHTML(): string {
  return `
    <div class="modal-overlay" id="add-biz-modal">
      <div class="modal-card" style="max-width: 580px; position: relative;">
        <!-- Clear Dark Close Button -->
        <button class="modal-close-btn" id="close-add-biz-modal" style="
          position: absolute;
          top: 14px;
          right: 14px;
          background: rgba(0,0,0,0.75);
          color: #ffffff;
          border: 2px solid rgba(255,255,255,0.4);
          width: 36px;
          height: 36px;
          border-radius: 50%;
          font-size: 16px;
          font-weight: bold;
          cursor: pointer;
          display: flex;
          align-items: center;
          justify-content: center;
          z-index: 10;
        ">✕</button>

        <h2 style="font-family: 'Outfit', sans-serif; font-size: 24px; font-weight: 800; margin-bottom: 6px;">Register Your Business</h2>
        <p style="color: #666; font-size: 13px; margin-bottom: 20px;">Join the global Albanian business directory and reach customers on Web, Android, and iOS.</p>

        <form id="add-biz-form" style="display: flex; flex-direction: column; gap: 14px;">
          <div>
            <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Business Name *</label>
            <input type="text" id="add-biz-name" required placeholder="e.g. Bar Caffe Iliria" style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 8px; font-size: 14px;" />
          </div>

          <div style="display: flex; gap: 12px;">
            <div style="flex: 1;">
              <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Category *</label>
              <select id="add-biz-category" required style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 8px; font-size: 14px; background: #fff;">
                <option value="Restaurant">Restaurant</option>
                <option value="Cafe">Cafe</option>
                <option value="Market">Market</option>
                <option value="Contractor">Contractor / Ndërtim</option>
                <option value="Lawyer">Lawyer / Avokat</option>
                <option value="Dentist">Dentist / Stomatolog</option>
                <option value="Barber">Barber</option>
                <option value="Beauty Salon">Beauty Salon</option>
                <option value="Auto Shop">Auto Shop</option>
                <option value="Other">Other</option>
              </select>
            </div>
            <div style="flex: 1;">
              <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">City & Country *</label>
              <input type="text" id="add-biz-city" required placeholder="e.g. Tirana or Padova" style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 8px; font-size: 14px;" />
            </div>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Street Address *</label>
            <input type="text" id="add-biz-address" required placeholder="e.g. Prato della Valle 12" style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 8px; font-size: 14px;" />
          </div>

          <div style="display: flex; gap: 12px;">
            <div style="flex: 1;">
              <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Phone Number *</label>
              <input type="tel" id="add-biz-phone" required placeholder="+355 4 123456" style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 8px; font-size: 14px;" />
            </div>
            <div style="flex: 1;">
              <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Website (Optional)</label>
              <input type="url" id="add-biz-website" placeholder="https://..." style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 8px; font-size: 14px;" />
            </div>
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Description *</label>
            <textarea id="add-biz-desc" required rows="3" placeholder="Describe your business and services..." style="width: 100%; padding: 10px; border: 1px solid #ccc; border-radius: 8px; font-size: 14px; font-family: inherit;"></textarea>
          </div>

          <!-- Photo Upload Section (Free tier: 1 photo | Paid Tiers: Multiple photos) -->
          <div style="background: #f9f9f9; padding: 12px; border-radius: 10px; border: 1px dashed #ccc;">
            <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Business Photo URL</label>
            <input type="url" id="add-biz-photo" placeholder="https://images.unsplash.com/..." style="width: 100%; padding: 8px; border: 1px solid #ccc; border-radius: 6px; font-size: 13px;" />
            <div style="font-size: 11px; color: #b8860b; font-weight: 600; margin-top: 6px;">
              👑 Multiple photo galleries (up to 14 photos) are unlocked with Premium ($2.99/mo) or Sponsored plans.
            </div>
          </div>

          <div style="display: flex; gap: 10px; align-items: center;">
            <input type="checkbox" id="add-biz-albanian" checked style="width: 18px; height: 18px;" />
            <label for="add-biz-albanian" style="font-size: 13px; font-weight: 600;">🇦🇱 Mark as Albanian Owned Business</label>
          </div>

          <button type="submit" class="btn-primary" style="width: 100%; padding: 14px; font-size: 15px; border-radius: 10px; margin-top: 10px;">
            Submit Business Listing
          </button>
        </form>
      </div>
    </div>
  `;
}

// Bismillah Hir Rahman Nir Raheem

export function createSubscriptionsModalHTML(): string {
  return `
    <div class="modal-overlay" id="sub-modal">
      <div class="modal-card" style="max-width: 800px; position: relative;">
        <!-- Clear Dark Close Button -->
        <button class="modal-close-btn" id="close-sub-modal" style="
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

        <div style="text-align: center; margin-bottom: 28px;">
          <h2 style="font-family: 'Outfit', sans-serif; font-size: 28px; font-weight: 800; margin-bottom: 6px;">Choose Your Business Plan</h2>
          <p style="color: #666; font-size: 14px;">Unlock premium tools, map priority, gallery photos, and maximum customer visibility across Web, Android, and iOS.</p>
        </div>

        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(230px, 1fr)); gap: 20px;">
          <!-- Free Plan -->
          <div style="background: #f9f9f9; border: 1px solid #e0e0e0; border-radius: 16px; padding: 22px; display: flex; flex-direction: column; justify-content: space-between;">
            <div>
              <h3 style="font-size: 20px; font-weight: 800; margin-bottom: 4px;">Free Listing</h3>
              <p style="color: #777; font-size: 12px; margin-bottom: 12px;">Basic map visibility</p>
              <div style="font-size: 24px; font-weight: 800; margin-bottom: 16px;">$0 <span style="font-size: 13px; font-weight: normal; color: #888;">/ mo</span></div>
              <ul style="list-style: none; padding: 0; margin: 0; font-size: 13px; color: #444; display: flex; flex-direction: column; gap: 8px;">
                <li>✔️ Basic Map Pin</li>
                <li>✔️ Business Name & Category</li>
                <li>✔️ 100 character description</li>
                <li>✔️ 1 Main Photo</li>
              </ul>
            </div>
            <button class="btn-outline" style="width: 100%; margin-top: 20px;" disabled>Current Standard</button>
          </div>

          <!-- Premium Plan ($2.99) -->
          <div style="background: #fff; border: 2px solid #FFD700; border-radius: 16px; padding: 22px; display: flex; flex-direction: column; justify-content: space-between; position: relative; box-shadow: 0 4px 16px rgba(255,215,0,0.2);">
            <div style="position: absolute; top: -12px; right: 16px; background: #FFD700; color: #000; font-size: 10px; font-weight: 800; padding: 3px 10px; border-radius: 10px; text-transform: uppercase;">Most Popular</div>
            <div>
              <h3 style="font-size: 20px; font-weight: 800; margin-bottom: 4px; color: #b8860b;">👑 Premium</h3>
              <p style="color: #777; font-size: 12px; margin-bottom: 12px;">Full contact & gallery</p>
              <div style="font-size: 24px; font-weight: 800; color: #b8860b; margin-bottom: 16px;">$2.99 <span style="font-size: 13px; font-weight: normal; color: #888;">/ mo</span></div>
              <ul style="list-style: none; padding: 0; margin: 0; font-size: 13px; color: #444; display: flex; flex-direction: column; gap: 8px;">
                <li>✔️ <strong>Verified Business Badge</strong></li>
                <li>✔️ Clickable Phone, Email & Website</li>
                <li>✔️ Up to 6 High-Res Photos</li>
                <li>✔️ Extended Long Description</li>
                <li>✔️ Working Hours & Open Status</li>
              </ul>
            </div>
            <button class="btn-primary" style="width: 100%; margin-top: 20px; background: #b8860b;" id="sub-upgrade-premium-btn">Upgrade to Premium</button>
          </div>

          <!-- Sponsored Plan ($19.99) -->
          <div style="background: #fff; border: 2px solid #E41E20; border-radius: 16px; padding: 22px; display: flex; flex-direction: column; justify-content: space-between; box-shadow: 0 4px 16px rgba(228,30,32,0.15);">
            <div>
              <h3 style="font-size: 20px; font-weight: 800; margin-bottom: 4px; color: #E41E20;">⭐ Sponsored</h3>
              <p style="color: #777; font-size: 12px; margin-bottom: 12px;">Maximum map priority</p>
              <div style="font-size: 24px; font-weight: 800; color: #E41E20; margin-bottom: 16px;">$19.99 <span style="font-size: 13px; font-weight: normal; color: #888;">/ mo</span></div>
              <ul style="list-style: none; padding: 0; margin: 0; font-size: 13px; color: #444; display: flex; flex-direction: column; gap: 8px;">
                <li>✔️ <strong>Gold Map Pin & Top Placement</strong></li>
                <li>✔️ Up to 14 Gallery Photos</li>
                <li>✔️ Promoted in 24h Stories Row</li>
                <li>✔️ Deals & Promotions Banner</li>
                <li>✔️ Full Analytical Insights</li>
              </ul>
            </div>
            <button class="btn-primary" style="width: 100%; margin-top: 20px;" id="sub-upgrade-sponsored-btn">Get Sponsored Pin</button>
          </div>
        </div>
      </div>
    </div>
  `;
}

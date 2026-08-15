// Bismillah Hir Rahman Nir Raheem

export function createFooterHTML(): string {
  return `
    <footer class="footer">
      <div style="max-width: 1200px; margin: 0 auto;">
        <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 20px; margin-bottom: 24px;">
          <div style="display: flex; align-items: center; gap: 10px;">
            <img src="/logo.png" alt="MeTont Logo" style="width: 38px; height: 38px; border-radius: 10px; object-fit: cover;" />
            <div style="font-family: 'Outfit', sans-serif; font-size: 22px; font-weight: 800;">Me<span style="color: #E41E20;">Tont</span></div>
          </div>
          <div style="display: flex; gap: 12px;">
            <div style="background: #111; color: #fff; padding: 8px 16px; border-radius: 10px; font-size: 12px; font-weight: 600; display: flex; align-items: center; gap: 8px;">
              <span>🤖</span> Google Play (Android)
            </div>
            <div style="background: #111; color: #fff; padding: 8px 16px; border-radius: 10px; font-size: 12px; font-weight: 600; display: flex; align-items: center; gap: 8px;">
              <span>🍏</span> App Store (iOS)
            </div>
          </div>
        </div>

        <div class="footer-links">
          <a href="#" id="footer-link-home">Home</a>
          <a href="#" id="footer-link-directory">Directory</a>
          <a href="#" id="footer-link-add">Add Business</a>
          <a href="#" id="footer-link-pricing">Plans & Pricing</a>
          <a href="#" id="footer-link-privacy">Privacy Policy</a>
          <a href="#" id="footer-link-terms">Terms of Service</a>
        </div>

        <p style="font-size: 12px; color: #888; margin-top: 20px;">
          © 2026 MeTont (AlbBizMap). All rights reserved. Connecting Albanian businesses worldwide.
        </p>
      </div>
    </footer>
  `;
}

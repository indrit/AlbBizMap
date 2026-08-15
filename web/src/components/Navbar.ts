// Bismillah Hir Rahman Nir Raheem
import type { AppLanguage } from '../utils/translations';
import { getTranslation } from '../utils/translations';

export function createNavbarHTML(currentLang: AppLanguage, isLoggedIn: boolean, userName?: string): string {
  const t = getTranslation(currentLang);

  return `
    <nav class="navbar">
      <div class="nav-brand" id="nav-brand-logo">
        <img src="/logo.png" alt="MeTont Logo" style="width: 40px; height: 40px; border-radius: 10px; object-fit: cover;" />
        <div class="nav-title">Me<span>Tont</span></div>
      </div>

      <div style="position: relative; flex: 1; max-width: 480px;">
        <div class="nav-search-bar" style="width: 100%;">
          <span style="color: #888; font-size: 16px;">🔍</span>
          <input type="text" id="nav-search-input" class="nav-search-input" placeholder="${t.searchPlaceholder}" autocomplete="off" />
        </div>
        <div id="nav-suggestions-container"></div>
      </div>

      <div style="display: flex; align-items: center; gap: 8px;">
        <button id="nav-my-location-btn" class="nav-location-btn">
          <span>📍</span> ${t.nearMe}
        </button>
        <div style="display: flex; background: #f5f5f5; border-radius: 20px; padding: 2px 8px; border: 1px solid #eee;">
          <span style="color: #999; font-size: 13px; margin-right: 4px; align-self: center;">🏙️</span>
          <input type="text" id="nav-city-input" placeholder="e.g. Tirana or Padova" style="border: none; background: transparent; outline: none; font-size: 13px; width: 120px;" autocomplete="off" />
        </div>
      </div>

      <div class="nav-actions">
        <div class="lang-selector">
          <button id="lang-btn-en" class="lang-btn ${currentLang === 'en' ? 'active' : ''}">EN</button>
          <button id="lang-btn-sq" class="lang-btn ${currentLang === 'sq' ? 'active' : ''}">SQ</button>
        </div>

        <button id="nav-add-biz-btn" class="btn-primary">
          + ${t.addBusiness}
        </button>

        <button id="nav-sub-btn" class="btn-outline" style="border-color: #FFD700; color: #b8860b;">
          👑 ${t.pricing}
        </button>

        <button id="nav-auth-btn" class="btn-outline">
          ${isLoggedIn ? `👤 ${userName || t.profile}` : t.signIn}
        </button>
      </div>
    </nav>
  `;
}

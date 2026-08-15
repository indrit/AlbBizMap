// Bismillah Hir Rahman Nir Raheem
import './styles/index.css';
import type { AppLanguage } from './utils/translations';
import { getTranslation } from './utils/translations';
import type { Business, Story } from './data/sampleData';
import { SAMPLE_BUSINESSES, SAMPLE_STORIES, SAMPLE_EVENTS, SAMPLE_JOBS } from './data/sampleData';
import { MapView } from './components/MapView';
import { createNavbarHTML } from './components/Navbar';
import { createStoryBarHTML, createStoryViewerModalHTML } from './components/StoryBar';
import { createBusinessCardHTML } from './components/BusinessCard';
import { createBusinessModalHTML } from './components/BusinessModal';
import { createAuthModalHTML } from './components/AuthModal';
import { createAddBusinessModalHTML } from './components/AddBusinessModal';
import { createSubscriptionsModalHTML } from './components/SubscriptionsModal';
import { createPrivacyTermsModalHTML } from './components/PrivacyTermsModal';
import { createFooterHTML } from './components/Footer';
import { geocodePlace, getUserLocation } from './utils/geocoding';
import { FirestoreService } from './services/firestoreService';
import { AuthService } from './services/authService';
import type { UserProfile } from './services/authService';
import { getSearchSuggestions, renderSuggestionsDropdownHTML } from './components/SearchSuggestions';
import type { SuggestionItem } from './components/SearchSuggestions';

class App {
  private currentLang: AppLanguage = 'en';
  private currentUser: UserProfile | null = null;
  private businesses: Business[] = [...SAMPLE_BUSINESSES];
  private filteredBusinesses: Business[] = [...SAMPLE_BUSINESSES];
  private stories: Story[] = [...SAMPLE_STORIES];
  private selectedCategory: string | null = null;
  private searchQuery: string = '';
  private mapView: MapView | null = null;

  private activeSuggestions: SuggestionItem[] = [];

  constructor() {
    this.init();
  }

  private init() {
    this.render();
    this.initMap();
    this.bindEvents();
    this.initFirebaseSubscriptions();
    this.requestAutoGeolocation();
  }

  private initFirebaseSubscriptions() {
    FirestoreService.getInstance().subscribeBusinesses((liveBiz) => {
      this.businesses = liveBiz;
      this.applyFilters();
    });

    FirestoreService.getInstance().subscribeStories((liveStories) => {
      this.stories = liveStories;
      const container = document.querySelector('.story-bar-container');
      if (container) {
        container.outerHTML = createStoryBarHTML(this.stories);
        this.bindStoryEvents();
      }
    });

    AuthService.getInstance().subscribeAuth((user) => {
      this.currentUser = user;
      const authBtn = document.getElementById('nav-auth-btn');
      if (authBtn) {
        authBtn.innerHTML = user ? `👤 ${user.displayName}` : getTranslation(this.currentLang).signIn;
      }
    });
  }

  private requestAutoGeolocation() {
    getUserLocation().then(loc => {
      if (loc && this.mapView) {
        this.mapView.setCenter(loc.lat, loc.lng, 14);
      }
    }).catch(() => {
      console.log("Using default location: Tirana, Albania");
    });
  }

  private render() {
    const appEl = document.getElementById('app');
    if (!appEl) return;

    const t = getTranslation(this.currentLang);

    appEl.innerHTML = `
      <!-- Floating White Navbar -->
      ${createNavbarHTML(this.currentLang, !!this.currentUser, this.currentUser?.displayName)}

      <!-- Full-Screen Hero Map Section (Default Center: Tirana, Albania) -->
      <section class="hero-map-section">
        <div id="map"></div>
        <div class="floating-category-bar">
          <div class="category-chip ${this.selectedCategory === null ? 'active' : ''}" data-category="ALL">
            <span>✨</span> ${t.allCategories}
          </div>
          <div class="category-chip ${this.selectedCategory === 'Restaurant' ? 'active' : ''}" data-category="Restaurant">
            <span>🍽️</span> ${t.restaurant}
          </div>
          <div class="category-chip ${this.selectedCategory === 'Cafe' ? 'active' : ''}" data-category="Cafe">
            <span>☕</span> ${t.cafe}
          </div>
          <div class="category-chip ${this.selectedCategory === 'Market' ? 'active' : ''}" data-category="Market">
            <span>🛒</span> ${t.market}
          </div>
          <div class="category-chip ${this.selectedCategory === 'Contractor' ? 'active' : ''}" data-category="Contractor">
            <span>🔨</span> ${t.contractor}
          </div>
          <div class="category-chip ${this.selectedCategory === 'Lawyer' ? 'active' : ''}" data-category="Lawyer">
            <span>⚖️</span> ${t.lawyer}
          </div>
          <div class="category-chip ${this.selectedCategory === 'Dentist' ? 'active' : ''}" data-category="Dentist">
            <span>🩺</span> ${t.dentist}
          </div>
        </div>
      </section>

      <!-- Bottom Half Content Container -->
      <main class="main-container">
        <!-- 24h Community Stories -->
        <h2 class="section-title">
          <span>📸 ${t.stories}</span>
        </h2>
        ${createStoryBarHTML(this.stories)}

        <!-- Sponsored Near You -->
        <h2 class="section-title">
          <span>⭐ ${t.sponsoredNearYou}</span>
        </h2>
        <div class="business-grid" id="sponsored-grid">
          ${this.filteredBusinesses.filter(b => b.isSponsored).map(createBusinessCardHTML).join('')}
        </div>

        <!-- Featured & Top Rated Directory -->
        <h2 class="section-title" id="directory-heading">
          <span>🇦🇱 ${t.directory}</span>
          <small id="see-all-count">${this.filteredBusinesses.length} businesses</small>
        </h2>
        <div class="business-grid" id="directory-grid">
          ${this.filteredBusinesses.map(createBusinessCardHTML).join('')}
        </div>

        <!-- Community Events & Job Board Grid -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(340px, 1fr)); gap: 30px; margin-top: 40px;">
          <div>
            <h2 class="section-title"><span>🎉 ${t.communityEvents}</span></h2>
            <div style="display: flex; flex-direction: column; gap: 16px;">
              ${SAMPLE_EVENTS.map(evt => `
                <div style="background:#fff; border-radius:14px; padding:16px; box-shadow:var(--shadow-sm); border:1px solid #eee;">
                  <img src="${evt.imageUrl}" style="width:100%; height:140px; object-fit:cover; border-radius:10px; margin-bottom:10px;" />
                  <h3 style="font-size:16px; font-weight:700; margin-bottom:4px;">${evt.title}</h3>
                  <p style="font-size:12px; color:#666; margin-bottom:8px;">${evt.description}</p>
                  <div style="display:flex; justify-content:space-between; font-size:12px; font-weight:600; color:#E41E20;">
                    <span>📍 ${evt.locationName}</span>
                    <span>📅 ${evt.date}</span>
                  </div>
                </div>
              `).join('')}
            </div>
          </div>

          <div>
            <h2 class="section-title"><span>💼 ${t.jobBoard}</span></h2>
            <div style="display: flex; flex-direction: column; gap: 16px;">
              ${SAMPLE_JOBS.map(job => `
                <div style="background:#fff; border-radius:14px; padding:16px; box-shadow:var(--shadow-sm); border:1px solid #eee;">
                  <div style="display:flex; justify-content:space-between; align-items:flex-start;">
                    <h3 style="font-size:16px; font-weight:700;">${job.title}</h3>
                    <span style="background:#e6f2ff; color:#0066cc; font-size:11px; font-weight:700; padding:2px 8px; border-radius:6px;">${job.type}</span>
                  </div>
                  <p style="font-size:13px; color:#E41E20; font-weight:600; margin:4px 0;">${job.businessName} • ${job.location}</p>
                  <p style="font-size:12px; color:#666; margin-bottom:10px;">${job.description}</p>
                  ${job.salary ? `<div style="font-size:12px; font-weight:700; color:#2e7d32;">💰 ${job.salary}</div>` : ''}
                </div>
              `).join('')}
            </div>
          </div>
        </div>
      </main>

      <!-- Global Footer -->
      ${createFooterHTML()}

      <!-- Modals Container -->
      <div id="modals-root"></div>
    `;
  }

  private initMap() {
    this.mapView = new MapView('map', (biz) => {
      this.openBusinessModal(biz);
    });
    this.mapView.renderBusinesses(this.filteredBusinesses);
  }

  private bindEvents() {
    const searchInput = document.getElementById('nav-search-input') as HTMLInputElement;
    const suggestionsContainer = document.getElementById('nav-suggestions-container');

    if (searchInput && suggestionsContainer) {
      searchInput.addEventListener('input', (e) => {
        const val = (e.target as HTMLInputElement).value;
        this.searchQuery = val;
        this.applyFilters();
        
        this.activeSuggestions = getSearchSuggestions(val, this.businesses);
        suggestionsContainer.innerHTML = renderSuggestionsDropdownHTML(this.activeSuggestions);
        this.bindSuggestionEvents();
      });

      searchInput.addEventListener('keydown', async (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          const val = searchInput.value.trim();
          if (!val) return;

          if (this.activeSuggestions.length > 0) {
            this.handleSuggestionSelect(this.activeSuggestions[0]);
          } else {
            const loc = await geocodePlace(val);
            if (loc) {
              this.mapView?.setCenter(loc.lat, loc.lng, 13);
            }
          }
          suggestionsContainer.innerHTML = '';
        }
      });
    }

    const cityInput = document.getElementById('nav-city-input') as HTMLInputElement;
    if (cityInput) {
      cityInput.addEventListener('keydown', async (e) => {
        if (e.key === 'Enter') {
          e.preventDefault();
          const val = cityInput.value.trim();
          if (val) {
            const loc = await geocodePlace(val);
            if (loc) {
              this.mapView?.setCenter(loc.lat, loc.lng, 13);
              this.searchQuery = val;
              this.applyFilters();
            }
          }
        }
      });
    }

    const nearMeBtn = document.getElementById('nav-my-location-btn');
    if (nearMeBtn) {
      nearMeBtn.addEventListener('click', async () => {
        try {
          const loc = await getUserLocation();
          this.mapView?.setCenter(loc.lat, loc.lng, 14);
        } catch (err) {
          alert('Could not retrieve your location. Center is set to Tirana, Albania.');
        }
      });
    }

    document.querySelectorAll('.category-chip').forEach(chip => {
      chip.addEventListener('click', () => {
        const cat = chip.getAttribute('data-category');
        this.selectedCategory = cat === 'ALL' ? null : cat;
        this.applyFilters();
        this.updateCategoryUI();
      });
    });

    document.getElementById('lang-btn-en')?.addEventListener('click', () => this.switchLanguage('en'));
    document.getElementById('lang-btn-sq')?.addEventListener('click', () => this.switchLanguage('sq'));

    document.getElementById('nav-add-biz-btn')?.addEventListener('click', () => this.openAddBusinessModal());
    document.getElementById('nav-sub-btn')?.addEventListener('click', () => this.openSubscriptionsModal());
    document.getElementById('nav-auth-btn')?.addEventListener('click', () => this.openAuthModal());

    document.getElementById('footer-link-privacy')?.addEventListener('click', (e) => {
      e.preventDefault();
      this.openLegalModal('privacy');
    });
    document.getElementById('footer-link-terms')?.addEventListener('click', (e) => {
      e.preventDefault();
      this.openLegalModal('terms');
    });
    document.getElementById('footer-link-pricing')?.addEventListener('click', (e) => {
      e.preventDefault();
      this.openSubscriptionsModal();
    });
    document.getElementById('footer-link-add')?.addEventListener('click', (e) => {
      e.preventDefault();
      this.openAddBusinessModal();
    });

    document.addEventListener('click', (e) => {
      const target = e.target as HTMLElement;
      if (!target.closest('#nav-search-input') && !target.closest('#suggestions-dropdown')) {
        if (suggestionsContainer) suggestionsContainer.innerHTML = '';
      }
    });

    this.bindCardEvents();
    this.bindStoryEvents();
  }

  private bindSuggestionEvents() {
    document.querySelectorAll('.suggestion-item[data-idx]').forEach(item => {
      item.addEventListener('click', () => {
        const idx = parseInt(item.getAttribute('data-idx') || '0', 10);
        const suggestion = this.activeSuggestions[idx];
        if (suggestion) {
          this.handleSuggestionSelect(suggestion);
        }
        const suggestionsContainer = document.getElementById('nav-suggestions-container');
        if (suggestionsContainer) suggestionsContainer.innerHTML = '';
      });
    });
  }

  private handleSuggestionSelect(suggestion: SuggestionItem) {
    if (suggestion.lat && suggestion.lng) {
      this.mapView?.setCenter(suggestion.lat, suggestion.lng, suggestion.type === 'business' ? 15 : 13);
    }
    if (suggestion.type === 'business' && suggestion.businessId) {
      const biz = this.businesses.find(b => b.id === suggestion.businessId);
      if (biz) this.openBusinessModal(biz);
    } else if (suggestion.type === 'city') {
      this.searchQuery = suggestion.title.split(',')[0];
      const searchInput = document.getElementById('nav-search-input') as HTMLInputElement;
      if (searchInput) searchInput.value = this.searchQuery;
      this.applyFilters();
    }
  }

  private bindCardEvents() {
    document.querySelectorAll('.biz-card').forEach(card => {
      card.addEventListener('click', () => {
        const bizId = card.getAttribute('data-biz-id');
        const biz = this.businesses.find(b => b.id === bizId);
        if (biz) {
          this.openBusinessModal(biz);
        }
      });
    });
  }

  private bindStoryEvents() {
    document.querySelectorAll('.story-avatar-wrapper[data-story-idx]').forEach(avatar => {
      avatar.addEventListener('click', () => {
        const idx = parseInt(avatar.getAttribute('data-story-idx') || '0', 10);
        const story = this.stories[idx];
        if (story) {
          this.openStoryViewerModal(story);
        }
      });
    });

    document.getElementById('add-story-btn')?.addEventListener('click', () => {
      this.openAuthModal();
    });
  }

  private applyFilters() {
    this.filteredBusinesses = this.businesses.filter(biz => {
      const matchesCategory = !this.selectedCategory || biz.category.toLowerCase() === this.selectedCategory.toLowerCase();
      const q = this.searchQuery.toLowerCase();
      const matchesQuery = !q || (
        biz.name.toLowerCase().includes(q) ||
        biz.category.toLowerCase().includes(q) ||
        biz.city.toLowerCase().includes(q) ||
        biz.description.toLowerCase().includes(q)
      );
      return matchesCategory && matchesQuery;
    });

    const grid = document.getElementById('directory-grid');
    if (grid) {
      grid.innerHTML = this.filteredBusinesses.map(createBusinessCardHTML).join('');
    }

    const sponsoredGrid = document.getElementById('sponsored-grid');
    if (sponsoredGrid) {
      sponsoredGrid.innerHTML = this.filteredBusinesses.filter(b => b.isSponsored).map(createBusinessCardHTML).join('');
    }

    const countEl = document.getElementById('see-all-count');
    if (countEl) {
      countEl.textContent = `${this.filteredBusinesses.length} businesses`;
    }

    this.mapView?.renderBusinesses(this.filteredBusinesses);
    this.bindCardEvents();
  }

  private updateCategoryUI() {
    document.querySelectorAll('.category-chip').forEach(chip => {
      const cat = chip.getAttribute('data-category');
      const isActive = (cat === 'ALL' && !this.selectedCategory) || (cat === this.selectedCategory);
      if (isActive) {
        chip.classList.add('active');
      } else {
        chip.classList.remove('active');
      }
    });
  }

  private switchLanguage(lang: AppLanguage) {
    this.currentLang = lang;
    this.render();
    this.initMap();
    this.bindEvents();
  }

  private openBusinessModal(biz: Business) {
    const root = document.getElementById('modals-root');
    if (!root) return;
    root.innerHTML = createBusinessModalHTML(biz);

    document.getElementById('close-biz-modal')?.addEventListener('click', () => this.closeModals());
  }

  private openAuthModal() {
    const root = document.getElementById('modals-root');
    if (!root) return;
    root.innerHTML = createAuthModalHTML();

    document.getElementById('close-auth-modal')?.addEventListener('click', () => this.closeModals());
    document.getElementById('link-auth-privacy')?.addEventListener('click', (e) => {
      e.preventDefault();
      this.openLegalModal('privacy');
    });
    document.getElementById('link-auth-terms')?.addEventListener('click', (e) => {
      e.preventDefault();
      this.openLegalModal('terms');
    });

    // Google Sign-In action handler
    document.getElementById('google-signin-btn')?.addEventListener('click', async () => {
      this.currentUser = {
        uid: `google_user_${Date.now()}`,
        email: 'user@gmail.com',
        displayName: 'Google User'
      };
      alert('Signed in with Google!');
      this.closeModals();
      this.render();
      this.initMap();
      this.bindEvents();
    });

    document.getElementById('auth-form')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const email = (document.getElementById('auth-email-input') as HTMLInputElement).value;
      const pass = (document.getElementById('auth-password-input') as HTMLInputElement).value;

      const user = await AuthService.getInstance().login(email, pass);
      this.currentUser = user;
      alert(`Welcome, ${user.displayName}! You are signed in across Web, Android, and iOS.`);
      this.closeModals();
      this.render();
      this.initMap();
      this.bindEvents();
    });
  }

  private openAddBusinessModal() {
    const root = document.getElementById('modals-root');
    if (!root) return;
    root.innerHTML = createAddBusinessModalHTML();

    document.getElementById('close-add-biz-modal')?.addEventListener('click', () => this.closeModals());

    document.getElementById('add-biz-form')?.addEventListener('submit', async (e) => {
      e.preventDefault();
      const name = (document.getElementById('add-biz-name') as HTMLInputElement).value;
      const category = (document.getElementById('add-biz-category') as HTMLSelectElement).value;
      const city = (document.getElementById('add-biz-city') as HTMLInputElement).value;
      const address = (document.getElementById('add-biz-address') as HTMLInputElement).value;
      const phone = (document.getElementById('add-biz-phone') as HTMLInputElement).value;
      const website = (document.getElementById('add-biz-website') as HTMLInputElement).value;
      const desc = (document.getElementById('add-biz-desc') as HTMLTextAreaElement).value;
      const photoUrl = (document.getElementById('add-biz-photo') as HTMLInputElement)?.value;
      const isAlbanian = (document.getElementById('add-biz-albanian') as HTMLInputElement).checked;

      const geocoded = await geocodePlace(`${address}, ${city}`);

      const newBizData = {
        name,
        category,
        description: desc,
        address,
        city,
        country: city.includes(',') ? city.split(',')[1].trim() : 'Albania',
        phone,
        email: this.currentUser?.email || 'owner@metont.com',
        website: website || 'https://metont.com',
        lat: geocoded ? geocoded.lat : 41.3275,
        lng: geocoded ? geocoded.lng : 19.8187,
        photos: photoUrl ? [photoUrl] : ['https://images.unsplash.com/photo-1555396273-367ea4eb4db5'],
        isAlbanianOwned: isAlbanian
      };

      await FirestoreService.getInstance().addBusiness(newBizData);
      this.closeModals();
      alert(`Business "${name}" successfully registered across Web, Android, and iOS!`);

      if (geocoded) {
        this.mapView?.setCenter(geocoded.lat, geocoded.lng, 14);
      }
    });
  }

  private openSubscriptionsModal() {
    const root = document.getElementById('modals-root');
    if (!root) return;
    root.innerHTML = createSubscriptionsModalHTML();

    document.getElementById('close-sub-modal')?.addEventListener('click', () => this.closeModals());

    document.getElementById('sub-upgrade-premium-btn')?.addEventListener('click', () => {
      alert('Redirecting to Premium Plan Checkout ($2.99/mo)...');
      this.closeModals();
    });

    document.getElementById('sub-upgrade-sponsored-btn')?.addEventListener('click', () => {
      alert('Redirecting to Sponsored Plan Checkout ($19.99/mo)...');
      this.closeModals();
    });
  }

  private openLegalModal(docType: 'privacy' | 'terms') {
    const root = document.getElementById('modals-root');
    if (!root) return;
    root.innerHTML = createPrivacyTermsModalHTML(docType);

    document.getElementById('close-legal-modal')?.addEventListener('click', () => this.closeModals());

    document.getElementById('tab-legal-privacy')?.addEventListener('click', () => this.openLegalModal('privacy'));
    document.getElementById('tab-legal-terms')?.addEventListener('click', () => this.openLegalModal('terms'));
  }

  private openStoryViewerModal(story: Story) {
    const root = document.getElementById('modals-root');
    if (!root) return;
    root.innerHTML = createStoryViewerModalHTML(story);

    document.getElementById('close-story-modal')?.addEventListener('click', () => this.closeModals());

    const bizBtn = document.getElementById('story-view-biz-btn');
    if (bizBtn) {
      bizBtn.addEventListener('click', () => {
        const bizId = bizBtn.getAttribute('data-biz-id');
        const biz = this.businesses.find(b => b.id === bizId);
        this.closeModals();
        if (biz) {
          this.openBusinessModal(biz);
        }
      });
    }
  }

  private closeModals() {
    const root = document.getElementById('modals-root');
    if (root) root.innerHTML = '';
  }
}

new App();

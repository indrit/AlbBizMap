// Bismillah Hir Rahman Nir Raheem
import type { Story } from '../data/sampleData';

export function createStoryBarHTML(stories: Story[]): string {
  const storyItemsHTML = stories.map((s, idx) => `
    <div class="story-avatar-wrapper" data-story-idx="${idx}">
      <div class="story-ring ${s.isSponsored ? '' : ''}">
        <img src="${s.photoUrl}" alt="${s.userName}" class="story-img" />
      </div>
      <span class="story-name">${s.userName}</span>
    </div>
  `).join('');

  return `
    <div class="story-bar-container">
      <div class="story-avatar-wrapper" id="add-story-btn">
        <div class="story-ring add-ring" style="display:flex; align-items:center; justify-content:center; background: #FFF0F0; border: 2px dashed #E41E20;">
          <span style="font-size: 26px; color: #E41E20; font-weight: bold;">+</span>
        </div>
        <span class="story-name" style="color: #E41E20; font-weight:700;">Add Story</span>
      </div>
      ${storyItemsHTML}
    </div>
  `;
}

export function createStoryViewerModalHTML(story: Story): string {
  return `
    <div class="modal-overlay" id="story-viewer-modal">
      <div class="modal-card" style="max-width: 420px; background: #000; color: #fff; padding: 20px; text-align: center;">
        <button class="modal-close-btn" id="close-story-modal" style="background: rgba(255,255,255,0.2); color: #fff;">✕</button>
        <div style="display:flex; align-items:center; gap: 10px; margin-bottom: 16px; text-align: left;">
          <img src="${story.photoUrl}" style="width: 40px; height: 40px; border-radius: 50%; object-fit: cover;" />
          <div>
            <h4 style="margin: 0; font-size: 15px;">${story.userName}</h4>
            <p style="margin: 0; font-size: 11px; color: #aaa;">${story.location}</p>
          </div>
        </div>
        <img src="${story.photoUrl}" style="width: 100%; max-height: 420px; object-fit: cover; border-radius: 12px; margin-bottom: 16px;" />
        <p style="font-size: 15px; line-height: 1.5; margin-bottom: 20px;">${story.text}</p>
        ${story.businessId ? `<button class="btn-primary" style="width: 100%;" id="story-view-biz-btn" data-biz-id="${story.businessId}">View Business Listing</button>` : ''}
      </div>
    </div>
  `;
}

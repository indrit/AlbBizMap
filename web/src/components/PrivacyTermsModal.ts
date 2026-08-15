// Bismillah Hir Rahman Nir Raheem
import { PRIVACY_POLICY_TEXT, TERMS_OF_SERVICE_TEXT } from '../data/legalDocs';

export function createPrivacyTermsModalHTML(docType: 'privacy' | 'terms'): string {
  const isPrivacy = docType === 'privacy';
  const rawText = isPrivacy ? PRIVACY_POLICY_TEXT : TERMS_OF_SERVICE_TEXT;

  const formattedHtml = rawText
    .replace(/^# (.*$)/gim, '<h1 style="font-family:\'Outfit\',sans-serif; font-size:24px; font-weight:800; margin-bottom:12px;">$1</h1>')
    .replace(/^### (.*$)/gim, '<h3 style="font-size:16px; font-weight:700; color:#E41E20; margin:16px 0 8px 0;">$1</h3>')
    .replace(/^\*\* (.*$)/gim, '<p style="font-weight:bold; margin-bottom:8px;">$1</p>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
    .replace(/---/g, '<hr style="border:none; border-top:1px solid #eee; margin:16px 0;" />')
    .replace(/\n\n/g, '<br/>');

  return `
    <div class="modal-overlay" id="legal-doc-modal">
      <div class="modal-card" style="max-width: 720px; max-height: 85vh;">
        <button class="modal-close-btn" id="close-legal-modal">✕</button>
        <div style="display: flex; gap: 12px; margin-bottom: 20px;">
          <button id="tab-legal-privacy" class="btn-outline ${isPrivacy ? 'active' : ''}" style="${isPrivacy ? 'background: #E41E20; color: #fff; border-color: #E41E20;' : ''}">Privacy Policy</button>
          <button id="tab-legal-terms" class="btn-outline ${!isPrivacy ? 'active' : ''}" style="${!isPrivacy ? 'background: #E41E20; color: #fff; border-color: #E41E20;' : ''}">Terms of Service</button>
        </div>
        <div style="font-size: 14px; line-height: 1.6; color: #333;" id="legal-doc-content">
          ${formattedHtml}
        </div>
      </div>
    </div>
  `;
}

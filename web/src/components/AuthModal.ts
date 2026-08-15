// Bismillah Hir Rahman Nir Raheem

export function createAuthModalHTML(): string {
  return `
    <div class="modal-overlay" id="auth-modal">
      <div class="modal-card" style="max-width: 440px; position: relative;">
        <button class="modal-close-btn" id="close-auth-modal" style="position: absolute; top: 14px; right: 14px; background: rgba(0,0,0,0.7); color: #fff; border: none; width: 34px; height: 34px; border-radius: 50%; font-size: 16px; font-weight: bold; cursor: pointer; display: flex; align-items: center; justify-content: center; z-index: 10;">✕</button>

        <div style="text-align: center; margin-bottom: 20px;">
          <img src="/logo.png" alt="MeTont Logo" style="width: 50px; height: 50px; border-radius: 12px; margin-bottom: 8px; object-fit: cover;" />
          <h2 style="font-family: 'Outfit', sans-serif; font-size: 24px; font-weight: 800;">Welcome to MeTont</h2>
          <p style="color: #666; font-size: 13px; margin-top: 4px;">Explore, support, and register Albanian businesses worldwide.</p>
        </div>

        <!-- Google Sign-In Button (Matching Android & iOS) -->
        <button id="google-signin-btn" class="btn-outline" style="
          width: 100%;
          display: flex;
          align-items: center;
          justify-content: center;
          gap: 12px;
          padding: 12px;
          border-radius: 10px;
          border: 1px solid #dadce0;
          background: #ffffff;
          color: #3c4043;
          font-weight: 600;
          font-size: 14px;
          margin-bottom: 16px;
          cursor: pointer;
        ">
          <svg width="18" height="18" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48">
            <path fill="#EA4335" d="M24 9.5c3.54 0 6.71 1.22 9.21 3.6l6.85-6.85C35.9 2.38 30.47 0 24 0 14.62 0 6.51 5.38 2.56 13.22l7.98 6.19C12.43 13.72 17.74 9.5 24 9.5z"/>
            <path fill="#4285F4" d="M46.98 24.55c0-1.57-.15-3.09-.38-4.55H24v9.02h12.94c-.58 2.96-2.26 5.48-4.78 7.18l7.73 6c4.51-4.18 7.09-10.36 7.09-17.65z"/>
            <path fill="#FBBC05" d="M10.53 28.59c-.48-1.45-.76-2.99-.76-4.59s.28-3.14.76-4.59l-7.98-6.19C.92 16.46 0 20.12 0 24s.92 7.54 2.56 10.78l7.97-6.19z"/>
            <path fill="#34A853" d="M24 48c6.48 0 11.93-2.13 15.89-5.81l-7.73-6c-2.15 1.45-4.92 2.3-8.16 2.3-6.26 0-11.57-4.22-13.47-9.91l-7.98 6.19C6.51 42.62 14.62 48 24 48z"/>
          </svg>
          Continue with Google
        </button>

        <div style="display: flex; align-items: center; gap: 12px; margin-bottom: 16px;">
          <hr style="flex: 1; border: none; border-top: 1px solid #eee;" />
          <span style="font-size: 12px; color: #888; text-transform: uppercase;">or sign in with email</span>
          <hr style="flex: 1; border: none; border-top: 1px solid #eee;" />
        </div>

        <form id="auth-form" style="display: flex; flex-direction: column; gap: 14px;">
          <div>
            <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Email Address</label>
            <input type="email" id="auth-email-input" required placeholder="you@example.com" style="width: 100%; padding: 12px; border: 1px solid #ccc; border-radius: 10px; outline: none; font-size: 14px;" />
          </div>

          <div>
            <label style="font-size: 12px; font-weight: 700; color: #444; display: block; margin-bottom: 4px;">Password</label>
            <input type="password" id="auth-password-input" required placeholder="••••••••" style="width: 100%; padding: 12px; border: 1px solid #ccc; border-radius: 10px; outline: none; font-size: 14px;" />
          </div>

          <button type="submit" class="btn-primary" style="width: 100%; padding: 14px; font-size: 15px; border-radius: 10px; margin-top: 4px;">
            Sign In / Register
          </button>
        </form>

        <div style="margin-top: 20px; text-align: center; border-top: 1px solid #eee; padding-top: 16px; font-size: 12px; color: #888;">
          By continuing, you agree to MeTont's <a href="#" id="link-auth-terms" style="color: #E41E20;">Terms of Service</a> and <a href="#" id="link-auth-privacy" style="color: #E41E20;">Privacy Policy</a>.
        </div>
      </div>
    </div>
  `;
}

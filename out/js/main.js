/**
 * ApexRent Global - Interactive Logic & Form Handlers
 * Built & Designed by Adrianas Code (adrianascode.com)
 */

document.addEventListener('DOMContentLoaded', () => {
  // 1. Dynamic Copyright Year
  const yearElement = document.getElementById('current-year');
  if (yearElement) {
    yearElement.textContent = new Date().getFullYear();
  }

  // 2. Mobile Menu Toggle
  const mobileToggle = document.getElementById('mobile-toggle');
  const navLinks = document.getElementById('nav-links');
  if (mobileToggle && navLinks) {
    mobileToggle.addEventListener('click', () => {
      navLinks.classList.toggle('active');
    });
  }

  // 3. Guaranteed Rent Interactive Calculator
  const calcEstRent = document.getElementById('calc-est-rent');
  const calcPropType = document.getElementById('calc-prop-type');
  const calcGuaranteedPayout = document.getElementById('calc-guaranteed-payout');
  const calcFeePct = document.getElementById('calc-fee-pct');

  if (calcEstRent && calcGuaranteedPayout && calcFeePct) {
    const calculatePayout = () => {
      const grossRent = parseFloat(calcEstRent.value) || 0;
      const type = calcPropType ? calcPropType.value : 'condo';

      // Fee percentage structure (pre-determined percentage based on property complexity)
      let feeRate = 0.08; // 8% default for Condo
      if (type === 'house') feeRate = 0.09;      // 9% House
      if (type === 'commercial') feeRate = 0.07; // 7% Commercial
      if (type === 'land_farm') feeRate = 0.06;  // 6% Land & Farm

      const netGuaranteed = grossRent * (1 - feeRate);
      const feePercentDisplay = (feeRate * 100).toFixed(0);

      calcFeePct.textContent = `${feePercentDisplay}% agreed rate`;
      calcGuaranteedPayout.textContent = `$${Math.round(netGuaranteed).toLocaleString('en-US')}/mo`;
    };

    calcEstRent.addEventListener('input', calculatePayout);
    if (calcPropType) calcPropType.addEventListener('change', calculatePayout);
    calculatePayout();
  }

  // 4. Web3Forms Submission Handlers (New Client Sign Up & Contact Forms)
  const handleWeb3Form = (formId, statusId) => {
    const form = document.getElementById(formId);
    const statusDiv = document.getElementById(statusId);
    if (!form) return;

    form.addEventListener('submit', async (e) => {
      e.preventDefault();

      const submitBtn = form.querySelector('button[type="submit"]');
      const originalBtnText = submitBtn ? submitBtn.innerHTML : 'Submit';

      if (submitBtn) {
        submitBtn.disabled = true;
        submitBtn.innerHTML = `<span>Processing...</span>`;
      }

      if (statusDiv) {
        statusDiv.className = 'form-status';
        statusDiv.style.display = 'none';
      }

      try {
        const formData = new FormData(form);
        const object = Object.fromEntries(formData);
        const json = JSON.stringify(object);

        const response = await fetch('https://api.web3forms.com/submit', {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json'
          },
          body: json
        });

        const result = await response.json();

        if (result.success) {
          if (statusDiv) {
            statusDiv.className = 'form-status success';
            statusDiv.innerHTML = `<strong>Thank You!</strong> Your request has been submitted successfully. Our Toronto onboarding team will contact you within 24 hours.`;
            statusDiv.style.display = 'block';
          }
          form.reset();
        } else {
          throw new Error(result.message || 'Submission failed. Please try again.');
        }
      } catch (err) {
        if (statusDiv) {
          statusDiv.className = 'form-status error';
          statusDiv.innerHTML = `<strong>Error:</strong> ${err.message || 'Something went wrong. Please email us directly.'}`;
          statusDiv.style.display = 'block';
        }
      } finally {
        if (submitBtn) {
          submitBtn.disabled = false;
          submitBtn.innerHTML = originalBtnText;
        }
      }
    });
  };

  handleWeb3Form('signup-form', 'signup-status');
  handleWeb3Form('contact-form', 'contact-status');
});

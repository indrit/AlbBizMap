// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Matches Android's AuthScreen.kt: red header with logo, white card (email/password,
// forgot password, sign-up confirm-password), submit button, Google/Apple buttons,
// EN/SQ language toggle, and a mode-switch link. Google/Apple sign-in and the actual
// login/register backend are still mocked (see AuthManager.swift) — this screen only
// covers the visual/UX port; wiring up real Firebase Auth + Google Sign-In is a
// separate, bigger task.
public struct AuthScreen: View {
    @Environment(\.appStrings) private var strings

    @ObservedObject public var viewModel: AuthViewModel
    public let onAuthSuccess: () -> Void
    public let currentLanguage: AppLanguage
    public let onLanguageChange: (AppLanguage) -> Void

    public init(
        onAuthSuccess: @escaping () -> Void,
        currentLanguage: AppLanguage,
        onLanguageChange: @escaping (AppLanguage) -> Void,
        viewModel: AuthViewModel
    ) {
        self.onAuthSuccess = onAuthSuccess
        self.currentLanguage = currentLanguage
        self.onLanguageChange = onLanguageChange
        self.viewModel = viewModel
    }

    public var body: some View {
        ZStack {
            ScrollView {
                VStack(spacing: 0) {
                    header
                    card
                }
            }
            .background(Color.meTontRed.edgesIgnoringSafeArea(.top))

            if viewModel.showForgotPasswordDialog {
                forgotPasswordDialog
            }
        }
    }

    // MARK: - Red header

    private var header: some View {
        VStack(spacing: 8) {
            MeTontLogoImage()
                .frame(width: 100, height: 100)
            Text(strings.appName)
                .font(.system(size: 28, weight: .bold))
                .foregroundColor(.white)
            Text(strings.appTagline)
                .font(.system(size: 13))
                .foregroundColor(.white.opacity(0.8))
        }
        .frame(maxWidth: .infinity)
        .frame(height: 260)
        .background(Color.meTontRed)
    }

    // MARK: - White card

    private var card: some View {
        VStack(spacing: 16) {
            Text(viewModel.isSignUpMode ? strings.signUp : strings.welcomeBack)
                .font(.system(size: 24, weight: .bold))
                .foregroundColor(.meTontBlack)

            Text(viewModel.isSignUpMode ? strings.signUpToGetStarted : strings.signInToContinue)
                .font(.system(size: 14))
                .foregroundColor(.meTontGrey)
                .multilineTextAlignment(.center)

            iconField(icon: "envelope.fill", placeholder: strings.email, text: $viewModel.emailText, keyboardType: .emailAddress)

            passwordField

            if !viewModel.isSignUpMode {
                HStack {
                    Spacer()
                    Button(action: { viewModel.openForgotPassword() }) {
                        Text(strings.forgotPassword)
                            .font(.system(size: 13, weight: .medium))
                            .foregroundColor(.meTontRed)
                    }
                }
            }

            if viewModel.isSignUpMode {
                iconField(icon: "lock.fill", placeholder: strings.confirmPassword, text: $viewModel.confirmPasswordText, isSecure: true)
            }

            if let err = viewModel.errorMessage {
                Text(err)
                    .font(.system(size: 12))
                    .foregroundColor(.red)
                    .multilineTextAlignment(.center)
            }

            submitButton

            Divider().padding(.vertical, 4)

            googleButton
            appleButton

            languageToggle

            Button(action: {
                viewModel.isSignUpMode.toggle()
                viewModel.errorMessage = nil
            }) {
                Text(viewModel.isSignUpMode ? strings.haveAccount : strings.noAccount)
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.meTontRed)
            }
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 32)
        .frame(maxWidth: .infinity)
        .background(Color.white)
        .clipShape(UnevenRoundedRectangle(topLeadingRadius: 28, bottomLeadingRadius: 0, bottomTrailingRadius: 0, topTrailingRadius: 28))
        .offset(y: -24)
    }

    // MARK: - Fields

    private func iconField(icon: String, placeholder: String, text: Binding<String>, keyboardType: UIKeyboardType = .default, isSecure: Bool = false) -> some View {
        HStack(spacing: 10) {
            Image(systemName: icon)
                .foregroundColor(.meTontRed)
                .frame(width: 20)
            if isSecure {
                SecureField(placeholder, text: text)
            } else {
                TextField(placeholder, text: text)
                    .keyboardType(keyboardType)
                    .autocapitalization(.none)
                    .disableAutocorrection(true)
            }
        }
        .padding()
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.meTontLightGrey, lineWidth: 1)
        )
    }

    private var passwordField: some View {
        HStack(spacing: 10) {
            Image(systemName: "lock.fill")
                .foregroundColor(.meTontRed)
                .frame(width: 20)
            Group {
                if viewModel.passwordVisible {
                    TextField(strings.password, text: $viewModel.passwordText)
                        .autocapitalization(.none)
                        .disableAutocorrection(true)
                } else {
                    SecureField(strings.password, text: $viewModel.passwordText)
                }
            }
            Button(action: { viewModel.passwordVisible.toggle() }) {
                Image(systemName: viewModel.passwordVisible ? "eye.slash.fill" : "eye.fill")
                    .foregroundColor(.meTontGrey)
            }
        }
        .padding()
        .overlay(
            RoundedRectangle(cornerRadius: 12)
                .stroke(Color.meTontLightGrey, lineWidth: 1)
        )
    }

    // MARK: - Buttons

    private var submitButton: some View {
        Button(action: {
            viewModel.authenticate(strings: strings, isAlbanian: currentLanguage == .sq) { onAuthSuccess() }
        }) {
            HStack(spacing: 8) {
                if viewModel.isLoading {
                    ProgressView().tint(.white)
                }
                Text(viewModel.isSignUpMode ? strings.signUp : strings.signIn)
                    .font(.system(size: 16, weight: .bold))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color.meTontRed)
            .foregroundColor(.white)
            .cornerRadius(12)
        }
        .disabled(viewModel.isLoading)
    }

    private var googleButton: some View {
        Button(action: { viewModel.signInWithGoogle(isAlbanian: currentLanguage == .sq) { onAuthSuccess() } }) {
            HStack(spacing: 10) {
                GoogleLogoImage().frame(width: 18, height: 18)
                Text(strings.continueWithGoogle)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(Color(red: 0x3C / 255.0, green: 0x40 / 255.0, blue: 0x43 / 255.0))
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .overlay(
                RoundedRectangle(cornerRadius: 12)
                    .stroke(Color(red: 0xDA / 255.0, green: 0xDC / 255.0, blue: 0xE0 / 255.0), lineWidth: 1)
            )
        }
        .disabled(viewModel.isLoading)
    }

    private var appleButton: some View {
        Button(action: { viewModel.signInWithApple(isAlbanian: currentLanguage == .sq) { onAuthSuccess() } }) {
            HStack(spacing: 10) {
                Image(systemName: "apple.logo")
                    .font(.system(size: 18))
                    .foregroundColor(.white)
                Text(strings.continueWithApple)
                    .font(.system(size: 15, weight: .medium))
                    .foregroundColor(.white)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .background(Color.black)
            .cornerRadius(12)
        }
        .disabled(viewModel.isLoading)
    }

    private var languageToggle: some View {
        HStack(spacing: 8) {
            Button(action: { onLanguageChange(.en) }) {
                Text("🇬🇧 EN")
                    .font(.system(size: 14, weight: currentLanguage == .en ? .bold : .regular))
                    .foregroundColor(currentLanguage == .en ? .meTontRed : .meTontGrey)
            }
            Text("|").foregroundColor(.meTontGrey)
            Button(action: { onLanguageChange(.sq) }) {
                Text("🇦🇱 SQ")
                    .font(.system(size: 14, weight: currentLanguage == .sq ? .bold : .regular))
                    .foregroundColor(currentLanguage == .sq ? .meTontRed : .meTontGrey)
            }
        }
        .padding(.top, 4)
    }

    // MARK: - Forgot password dialog

    private var forgotPasswordDialog: some View {
        ZStack {
            Color.black.opacity(0.4)
                .edgesIgnoringSafeArea(.all)
                .onTapGesture { viewModel.showForgotPasswordDialog = false }

            VStack(alignment: .leading, spacing: 14) {
                Text(strings.resetPasswordTitle)
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(.meTontBlack)
                Text(strings.resetPasswordDescription)
                    .font(.system(size: 13))
                    .foregroundColor(.meTontGrey)

                iconField(icon: "envelope.fill", placeholder: strings.email, text: $viewModel.resetEmailText, keyboardType: .emailAddress)

                if let msg = viewModel.resetSentMessage {
                    Text(msg)
                        .font(.system(size: 12))
                        .foregroundColor(.meTontGrey)
                }

                HStack {
                    Button(action: { viewModel.showForgotPasswordDialog = false }) {
                        Text(strings.cancel)
                            .foregroundColor(.meTontGrey)
                    }
                    Spacer()
                    Button(action: { viewModel.sendPasswordReset(strings: strings, isAlbanian: currentLanguage == .sq) }) {
                        if viewModel.isSendingReset {
                            ProgressView().tint(.white)
                        } else {
                            Text(strings.sendResetLink)
                                .foregroundColor(.white)
                        }
                    }
                    .padding(.horizontal, 18)
                    .padding(.vertical, 10)
                    .background(Color.meTontRed)
                    .cornerRadius(10)
                    .disabled(viewModel.isSendingReset)
                }
            }
            .padding(20)
            .background(Color.white)
            .cornerRadius(20)
            .padding(.horizontal, 32)
        }
    }
}

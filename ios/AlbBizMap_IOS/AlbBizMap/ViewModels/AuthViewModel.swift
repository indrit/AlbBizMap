// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import GoogleSignIn
import AuthenticationServices

public class AuthViewModel: ObservableObject {
    @Published public var emailText: String = ""
    @Published public var passwordText: String = ""
    @Published public var confirmPasswordText: String = ""
    @Published public var firstNameText: String = ""
    @Published public var lastNameText: String = ""
    @Published public var isSignUpMode: Bool = false
    @Published public var errorMessage: String? = nil
    @Published public var isLoading: Bool = false
    @Published public var passwordVisible: Bool = false

    // Forgot-password dialog state
    @Published public var showForgotPasswordDialog: Bool = false
    @Published public var resetEmailText: String = ""
    @Published public var isSendingReset: Bool = false
    @Published public var resetSentMessage: String? = nil

    @Published public var currentUser: UserProfile? = nil

    private var cancellables = Set<AnyCancellable>()

    public init() {
        AuthManager.shared.$currentUser
            .assign(to: \.currentUser, on: self)
            .store(in: &cancellables)
    }

    public func authenticate(strings: AppStrings, isAlbanian: Bool, onSuccess: @escaping () -> Void) {
        errorMessage = nil
        if emailText.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = strings.emailRequired
            return
        }
        if passwordText.isEmpty {
            errorMessage = strings.passwordRequired
            return
        }

        if isSignUpMode {
            if passwordText != confirmPasswordText {
                errorMessage = strings.passwordsDoNotMatch
                return
            }
            if passwordText.count < 6 {
                errorMessage = strings.passwordTooShort
                return
            }
            isLoading = true
            Task {
                let res = await AuthManager.shared.register(email: emailText, pass: passwordText, firstName: firstNameText, lastName: lastNameText)
                await MainActor.run {
                    self.isLoading = false
                    switch res {
                    case .success:
                        onSuccess()
                    case .failure(let err):
                        self.errorMessage = AuthManager.mapFirebaseError(err, isAlbanian: isAlbanian)
                    }
                }
            }
        } else {
            isLoading = true
            Task {
                let res = await AuthManager.shared.login(email: emailText, pass: passwordText)
                await MainActor.run {
                    self.isLoading = false
                    switch res {
                    case .success:
                        onSuccess()
                    case .failure(let err):
                        self.errorMessage = AuthManager.mapFirebaseError(err, isAlbanian: isAlbanian)
                    }
                }
            }
        }
    }

    public func openForgotPassword() {
        resetEmailText = emailText
        resetSentMessage = nil
        showForgotPasswordDialog = true
    }

    public func sendPasswordReset(strings: AppStrings, isAlbanian: Bool) {
        guard !resetEmailText.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        isSendingReset = true
        Task {
            let res = await AuthManager.shared.sendPasswordReset(email: resetEmailText)
            await MainActor.run {
                self.isSendingReset = false
                switch res {
                case .success:
                    self.resetSentMessage = strings.resetEmailSent
                case .failure(let err):
                    self.resetSentMessage = AuthManager.mapFirebaseError(err, isAlbanian: isAlbanian)
                }
            }
        }
    }

    // Real Google Sign-In: presents the native account picker, then hands the
    // resulting ID token to Firebase — mirrors Android's requestGoogleIdToken +
    // signInWithGoogle(idToken) pair in AuthScreen.kt/AuthViewModel.kt.
    public func signInWithGoogle(isAlbanian: Bool, onSuccess: @escaping () -> Void) {
        if isLoading { return }
        isLoading = true
        errorMessage = nil
        Task {
            let res = await AuthManager.shared.signInWithGoogle()
            await MainActor.run {
                self.isLoading = false
                switch res {
                case .success:
                    onSuccess()
                case .failure(let err):
                    let nsError = err as NSError
                    // Cancelling the account picker isn't an error — same as
                    // Android treating a cancelled Credential Manager picker as
                    // "nothing to show", not a scary error message.
                    if nsError.domain == "com.google.GIDSignIn", nsError.code == GIDSignInError.canceled.rawValue {
                        return
                    }
                    self.errorMessage = AuthManager.mapFirebaseError(err, isAlbanian: isAlbanian)
                }
            }
        }
    }

    // Real Apple Sign-In: presents the system sheet, then hands the identity
    // token to Firebase — same shape as signInWithGoogle() above. Android's own
    // "Continue with Apple" button is still a placeholder there.
    public func signInWithApple(isAlbanian: Bool, onSuccess: @escaping () -> Void) {
        if isLoading { return }
        isLoading = true
        errorMessage = nil
        Task {
            let res = await AuthManager.shared.signInWithApple()
            await MainActor.run {
                self.isLoading = false
                switch res {
                case .success:
                    onSuccess()
                case .failure(let err):
                    let nsError = err as NSError
                    // User tapped Cancel on the system sheet — not an error to show.
                    if nsError.domain == ASAuthorizationError.errorDomain, nsError.code == ASAuthorizationError.canceled.rawValue {
                        return
                    }
                    self.errorMessage = AuthManager.mapFirebaseError(err, isAlbanian: isAlbanian)
                }
            }
        }
    }

    public func logout() {
        AuthManager.shared.logout()
    }
}

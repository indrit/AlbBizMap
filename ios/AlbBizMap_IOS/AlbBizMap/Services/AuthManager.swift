// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import FirebaseAuth
import GoogleSignIn
import AuthenticationServices
import CryptoKit
import UIKit

public struct UserProfile {
    public var uid: String
    public var email: String
    public var firstName: String
    public var lastName: String
    public var isAdmin: Bool

    public init(uid: String = "", email: String = "", firstName: String = "", lastName: String = "", isAdmin: Bool = false) {
        self.uid = uid
        self.email = email
        self.firstName = firstName
        self.lastName = lastName
        self.isAdmin = isAdmin
    }

    // Firebase only gives us a single displayName (set by Google Sign-In, or never
    // set at all for plain email/password signup — Android's own sign-up screen
    // doesn't collect a name either, that happens later in Profile). Split it the
    // same way the drawer's "Welcome, {name}" line already falls back.
    public init(firebaseUser: User) {
        self.uid = firebaseUser.uid
        self.email = firebaseUser.email ?? ""
        let parts = (firebaseUser.displayName ?? "").split(separator: " ", maxSplits: 1)
        self.firstName = parts.first.map(String.init) ?? ""
        self.lastName = parts.count > 1 ? String(parts[1]) : ""
        // isAdmin is a separate Firestore "users/{uid}" document field on Android
        // (AdminViewModel.isUserAdmin), not part of Firebase Auth itself, so it
        // isn't known yet at construction time — AuthManager.loadUserBackedState
        // fetches and overwrites this right after login.
        self.isAdmin = false
    }
}

public class AuthManager: ObservableObject {
    public static let shared = AuthManager()

    @Published public var currentUser: UserProfile? = nil
    @Published public var isLoggedIn: Bool = false

    private var authStateHandle: AuthStateDidChangeListenerHandle?

    private init() {
        if let user = Auth.auth().currentUser {
            self.currentUser = UserProfile(firebaseUser: user)
            self.isLoggedIn = true
            loadUserBackedState(uid: user.uid)
        }
        // Keeps currentUser/isLoggedIn in sync with Firebase's own session state —
        // same role as Android's _currentUser StateFlow tracking auth.currentUser.
        authStateHandle = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            guard let self = self else { return }
            if let user = user {
                self.currentUser = UserProfile(firebaseUser: user)
                self.isLoggedIn = true
                self.loadUserBackedState(uid: user.uid)
            } else {
                self.currentUser = nil
                self.isLoggedIn = false
                Task { await FirestoreService.shared.loadFavorites(userId: "") }
            }
        }
    }

    // Firestore-backed state that isn't part of Firebase Auth itself: the user's
    // saved favorites (users/{uid}.favorites) and their isAdmin flag
    // (users/{uid}.isAdmin) — fetched once on login, mirroring Android's
    // getFavoriteIds()/isUserAdmin() calls right after sign-in.
    private func loadUserBackedState(uid: String) {
        Task {
            await FirestoreService.shared.loadFavorites(userId: uid)
            let admin = await FirestoreService.shared.isUserAdmin(userId: uid)
            await MainActor.run {
                self.currentUser?.isAdmin = admin
            }
        }
    }

    public func requireLogin(onNotLoggedIn: @escaping () -> Void, action: @escaping () -> Void) {
        if isLoggedIn, currentUser != nil {
            action()
        } else {
            onNotLoggedIn()
        }
    }

    public func login(email: String, pass: String) async -> Result<UserProfile, Error> {
        do {
            let result = try await Auth.auth().signIn(withEmail: email, password: pass)
            let profile = UserProfile(firebaseUser: result.user)
            await MainActor.run {
                self.currentUser = profile
                self.isLoggedIn = true
            }
            return .success(profile)
        } catch {
            return .failure(error)
        }
    }

    public func register(email: String, pass: String, firstName: String, lastName: String) async -> Result<UserProfile, Error> {
        do {
            let result = try await Auth.auth().createUser(withEmail: email, password: pass)
            let profile = UserProfile(firebaseUser: result.user)
            await MainActor.run {
                self.currentUser = profile
                self.isLoggedIn = true
            }
            return .success(profile)
        } catch {
            return .failure(error)
        }
    }

    // Matches Android's signInWithGoogle(idToken) — presents the native Google
    // account picker, then exchanges the resulting ID token for a Firebase
    // session. Firebase auto-populates displayName from the Google account, same
    // as on Android.
    public func signInWithGoogle() async -> Result<UserProfile, Error> {
        guard let presenter = Self.topViewController() else {
            return .failure(NSError(domain: "AuthManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "Unable to present Google Sign-In."]))
        }
        do {
            let gidResult = try await GIDSignIn.sharedInstance.signIn(withPresenting: presenter)
            guard let idToken = gidResult.user.idToken?.tokenString else {
                return .failure(NSError(domain: "AuthManager", code: -2, userInfo: [NSLocalizedDescriptionKey: "Google Sign-In did not return a token."]))
            }
            let credential = GoogleAuthProvider.credential(withIDToken: idToken, accessToken: gidResult.user.accessToken.tokenString)
            let result = try await Auth.auth().signIn(with: credential)
            let profile = UserProfile(firebaseUser: result.user)
            await MainActor.run {
                self.currentUser = profile
                self.isLoggedIn = true
            }
            return .success(profile)
        } catch {
            return .failure(error)
        }
    }

    // Real "Sign in with Apple": presents Apple's native system sheet, then
    // exchanges the identity token for a Firebase session — the iOS-native
    // counterpart to signInWithGoogle() above. Android's own Apple button is
    // still a placeholder (per AuthScreen.kt), so this only affects iOS.
    public func signInWithApple() async -> Result<UserProfile, Error> {
        let rawNonce = Self.randomNonceString()
        let coordinator = AppleSignInCoordinator(rawNonce: rawNonce)
        do {
            let appleIDCredential = try await coordinator.start()
            guard let identityToken = appleIDCredential.identityToken,
                  let idTokenString = String(data: identityToken, encoding: .utf8) else {
                return .failure(NSError(domain: "AuthManager", code: -3, userInfo: [NSLocalizedDescriptionKey: "Apple did not return an identity token."]))
            }
            let credential = OAuthProvider.credential(providerID: .apple, idToken: idTokenString, rawNonce: rawNonce)
            let result = try await Auth.auth().signIn(with: credential)

            // Apple only ever includes the person's name on the VERY FIRST sign-in
            // for this app — every sign-in after that returns nil for it — so we
            // save it onto the Firebase user's displayName right away, the same
            // way Google Sign-In auto-populates it, or we'd lose it for good.
            if let fullName = appleIDCredential.fullName {
                let displayName = PersonNameComponentsFormatter().string(from: fullName)
                if !displayName.isEmpty, result.user.displayName != displayName {
                    let changeRequest = result.user.createProfileChangeRequest()
                    changeRequest.displayName = displayName
                    try? await changeRequest.commitChanges()
                }
            }

            let profile = UserProfile(firebaseUser: Auth.auth().currentUser ?? result.user)
            await MainActor.run {
                self.currentUser = profile
                self.isLoggedIn = true
            }
            return .success(profile)
        } catch {
            return .failure(error)
        }
    }

    private static func randomNonceString(length: Int = 32) -> String {
        var randomBytes = [UInt8](repeating: 0, count: length)
        _ = SecRandomCopyBytes(kSecRandomDefault, randomBytes.count, &randomBytes)
        let charset: [Character] = Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        return String(randomBytes.map { charset[Int($0) % charset.count] })
    }

    // Firebase compares this against the hash inside Apple's identity token to
    // guard against replay attacks — the raw value is what gets sent to
    // Firebase, the SHA-256 hash is what gets sent to Apple in the request.
    fileprivate static func sha256(_ input: String) -> String {
        SHA256.hash(data: Data(input.utf8)).compactMap { String(format: "%02x", $0) }.joined()
    }

    // Firebase composes and sends the actual reset email itself — nothing else
    // needed here, same as Android's sendPasswordResetEmail().
    public func sendPasswordReset(email: String) async -> Result<Void, Error> {
        do {
            try await Auth.auth().sendPasswordReset(withEmail: email)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    public func updateDisplayName(_ displayName: String) async -> Result<Void, Error> {
        guard let user = Auth.auth().currentUser else {
            return .failure(NSError(domain: "AuthManager", code: -1, userInfo: [NSLocalizedDescriptionKey: "No signed-in user"]))
        }
        do {
            let request = user.createProfileChangeRequest()
            request.displayName = displayName
            try await request.commitChanges()
            await MainActor.run {
                self.currentUser = UserProfile(firebaseUser: user)
            }
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    public func logout() {
        try? Auth.auth().signOut()
        GIDSignIn.sharedInstance.signOut()
        self.currentUser = nil
        self.isLoggedIn = false
    }

    @MainActor
    private static func topViewController() -> UIViewController? {
        guard let scene = UIApplication.shared.connectedScenes.first(where: { $0.activationState == .foregroundActive }) as? UIWindowScene,
              let root = scene.windows.first(where: { $0.isKeyWindow })?.rootViewController else {
            return nil
        }
        var top = root
        while let presented = top.presentedViewController {
            top = presented
        }
        return top
    }

    // Mirrors Android's mapFirebaseError(e, isAlbanian) — same error codes, same
    // EN/SQ copy, so the messages a user sees match across both platforms.
    public static func mapFirebaseError(_ error: Error, isAlbanian: Bool) -> String {
        let code = AuthErrorCode(rawValue: (error as NSError).code)
        if isAlbanian {
            switch code {
            case .invalidEmail: return "Formati i emailit është i pavlefshëm."
            case .wrongPassword: return "Fjalëkalimi është i gabuar. Ju lutemi provoni përsëri."
            case .userNotFound: return "Nuk u gjet asnjë llogari me këtë email."
            case .userDisabled: return "Kjo llogari është çaktivizuar."
            case .emailAlreadyInUse: return "Ky email është tashmë i regjistruar."
            case .weakPassword: return "Fjalëkalimi duhet të ketë të paktën 6 karaktere."
            case .networkError: return "Gabim rrjeti. Kontrolloni lidhjen tuaj."
            case .tooManyRequests: return "Shumë tentativa. Ju lutemi provoni më vonë."
            case .invalidCredential: return "Email ose fjalëkalim i gabuar. Provoni përsëri."
            default: return "Diçka shkoi keq. Ju lutemi provoni përsëri."
            }
        } else {
            switch code {
            case .invalidEmail: return "Invalid email address format."
            case .wrongPassword: return "Incorrect password. Please try again."
            case .userNotFound: return "No account found with this email."
            case .userDisabled: return "This account has been disabled."
            case .emailAlreadyInUse: return "This email is already registered."
            case .weakPassword: return "Password must be at least 6 characters."
            case .networkError: return "Network error. Check your connection."
            case .tooManyRequests: return "Too many attempts. Please try again later."
            case .invalidCredential: return "Incorrect email or password. Please try again."
            default: return "Something went wrong. Please try again."
            }
        }
    }
}


// Bridges ASAuthorizationController's delegate-based API to async/await, the
// same shape as GIDSignIn's own async signIn(withPresenting:) above.
private final class AppleSignInCoordinator: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {
    private let rawNonce: String
    private var continuation: CheckedContinuation<ASAuthorizationAppleIDCredential, Error>?

    init(rawNonce: String) {
        self.rawNonce = rawNonce
    }

    func start() async throws -> ASAuthorizationAppleIDCredential {
        try await withCheckedThrowingContinuation { continuation in
            self.continuation = continuation
            let provider = ASAuthorizationAppleIDProvider()
            let request = provider.createRequest()
            request.requestedScopes = [.fullName, .email]
            request.nonce = AuthManager.sha256(rawNonce)

            let controller = ASAuthorizationController(authorizationRequests: [request])
            controller.delegate = self
            controller.presentationContextProvider = self
            controller.performRequests()
        }
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        if let credential = authorization.credential as? ASAuthorizationAppleIDCredential {
            continuation?.resume(returning: credential)
        } else {
            continuation?.resume(throwing: NSError(domain: "AppleSignIn", code: -1, userInfo: [NSLocalizedDescriptionKey: "Unexpected credential type."]))
        }
        continuation = nil
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        continuation?.resume(throwing: error)
        continuation = nil
    }

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .first(where: { $0.isKeyWindow }) ?? ASPresentationAnchor()
    }
}

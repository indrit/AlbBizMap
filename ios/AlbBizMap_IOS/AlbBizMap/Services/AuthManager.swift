// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

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
}

public class AuthManager: ObservableObject {
    public static let shared = AuthManager()
    
    @Published public var currentUser: UserProfile? = nil
    @Published public var isLoggedIn: Bool = false
    
    private init() {
        // Initial state or cached session check
    }
    
    public func requireLogin(onNotLoggedIn: @escaping () -> Void, action: @escaping () -> Void) {
        if isLoggedIn, currentUser != nil {
            action()
        } else {
            onNotLoggedIn()
        }
    }
    
    public func login(email: String, pass: String) async -> Result<UserProfile, Error> {
        // Simulate or integrate with Firebase Auth
        let user = UserProfile(uid: "user_" + UUID().uuidString.prefix(8), email: email, firstName: "User", lastName: "Alb", isAdmin: email.contains("admin"))
        await MainActor.run {
            self.currentUser = user
            self.isLoggedIn = true
        }
        return .success(user)
    }
    
    public func register(email: String, pass: String, firstName: String, lastName: String) async -> Result<UserProfile, Error> {
        let user = UserProfile(uid: "user_" + UUID().uuidString.prefix(8), email: email, firstName: firstName, lastName: lastName, isAdmin: false)
        await MainActor.run {
            self.currentUser = user
            self.isLoggedIn = true
        }
        return .success(user)
    }
    
    public func logout() {
        self.currentUser = nil
        self.isLoggedIn = false
    }
}

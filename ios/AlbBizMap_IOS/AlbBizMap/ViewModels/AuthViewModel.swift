// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class AuthViewModel: ObservableObject {
    @Published public var emailText: String = ""
    @Published public var passwordText: String = ""
    @Published public var confirmPasswordText: String = ""
    @Published public var firstNameText: String = ""
    @Published public var lastNameText: String = ""
    @Published public var isSignUpMode: Bool = false
    @Published public var errorMessage: String? = nil
    @Published public var isLoading: Bool = false
    
    @Published public var currentUser: UserProfile? = nil
    
    private var cancellables = Set<AnyCancellable>()
    
    public init() {
        AuthManager.shared.$currentUser
            .assign(to: \.currentUser, on: self)
            .store(in: &cancellables)
    }
    
    public func authenticate(onSuccess: @escaping () -> Void) {
        errorMessage = nil
        if emailText.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "Email is required"
            return
        }
        if passwordText.isEmpty {
            errorMessage = "Password is required"
            return
        }
        
        if isSignUpMode {
            if passwordText != confirmPasswordText {
                errorMessage = "Passwords don't match"
                return
            }
            if passwordText.count < 6 {
                errorMessage = "Password must be at least 6 characters"
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
                        self.errorMessage = err.localizedDescription
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
                        self.errorMessage = err.localizedDescription
                    }
                }
            }
        }
    }
    
    public func logout() {
        AuthManager.shared.logout()
    }
}

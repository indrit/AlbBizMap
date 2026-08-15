// Bismillah Hir Rahman Nir Raheem
import SwiftUI

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
        VStack(spacing: 20) {
            Spacer()
            
            // App Branding
            VStack(spacing: 8) {
                Image(systemName: "map.fill")
                    .font(.system(size: 60))
                    .foregroundColor(.meTontRed)
                Text(strings.appName)
                    .font(.title).fontWeight(.bold)
                Text(strings.welcomeDesc)
                    .font(.subheadline)
                    .foregroundColor(.gray)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 24)
            }
            
            // Mode Segment Control
            Picker("", selection: $viewModel.isSignUpMode) {
                Text(strings.signIn).tag(false)
                Text(strings.signUp).tag(true)
            }
            .pickerStyle(SegmentedPickerStyle())
            .padding(.horizontal, 24)
            
            VStack(spacing: 12) {
                if viewModel.isSignUpMode {
                    HStack(spacing: 12) {
                        TextField(strings.firstName, text: $viewModel.firstNameText)
                            .padding()
                            .background(Color.gray.opacity(0.1))
                            .cornerRadius(10)
                        TextField(strings.lastName, text: $viewModel.lastNameText)
                            .padding()
                            .background(Color.gray.opacity(0.1))
                            .cornerRadius(10)
                    }
                }
                
                TextField(strings.email, text: $viewModel.emailText)
                    .keyboardType(.emailAddress)
                    .autocapitalization(.none)
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(10)
                
                SecureField(strings.password, text: $viewModel.passwordText)
                    .padding()
                    .background(Color.gray.opacity(0.1))
                    .cornerRadius(10)
                
                if viewModel.isSignUpMode {
                    SecureField(strings.confirmPassword, text: $viewModel.confirmPasswordText)
                        .padding()
                        .background(Color.gray.opacity(0.1))
                        .cornerRadius(10)
                }
            }
            .padding(.horizontal, 24)
            
            if let err = viewModel.errorMessage {
                Text(err).foregroundColor(.red).font(.caption)
            }
            
            Button(action: {
                viewModel.authenticate {
                    onAuthSuccess()
                }
            }) {
                Text(viewModel.isSignUpMode ? strings.signUp : strings.signIn)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 14)
                    .background(Color.meTontRed)
                    .foregroundColor(.white)
                    .cornerRadius(12)
            }
            .padding(.horizontal, 24)
            
            Spacer()
        }
        .background(Color.white.edgesIgnoringSafeArea(.all))
    }
}

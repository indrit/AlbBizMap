// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct UserProfileScreen: View {
    @Environment(\.appStrings) private var strings
    
    @ObservedObject public var viewModel: AuthViewModel
    public let onBackClick: () -> Void
    public let onLogout: () -> Void
    public let onAdminClick: () -> Void
    public let onMyBusinessesClick: () -> Void
    public let currentLanguage: AppLanguage
    public let onLanguageChange: (AppLanguage) -> Void
    
    public init(
        viewModel: AuthViewModel,
        onBackClick: @escaping () -> Void,
        onLogout: @escaping () -> Void,
        onAdminClick: @escaping () -> Void,
        onMyBusinessesClick: @escaping () -> Void,
        currentLanguage: AppLanguage,
        onLanguageChange: @escaping (AppLanguage) -> Void
    ) {
        self.viewModel = viewModel
        self.onBackClick = onBackClick
        self.onLogout = onLogout
        self.onAdminClick = onAdminClick
        self.onMyBusinessesClick = onMyBusinessesClick
        self.currentLanguage = currentLanguage
        self.onLanguageChange = onLanguageChange
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.myProfile)
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            ScrollView {
                VStack(spacing: 20) {
                    // Profile Header Card
                    VStack(spacing: 12) {
                        ZStack {
                            Circle().fill(Color.meTontRed.opacity(0.1))
                                .frame(width: 80, height: 80)
                            Image(systemName: "person.fill")
                                .font(.system(size: 40))
                                .foregroundColor(.meTontRed)
                        }
                        
                        Text("\(viewModel.currentUser?.firstName ?? "User") \(viewModel.currentUser?.lastName ?? "")")
                            .font(.title3).fontWeight(.bold)
                        Text(viewModel.currentUser?.email ?? "")
                            .font(.subheadline).foregroundColor(.gray)
                    }
                    .padding(20)
                    .frame(maxWidth: .infinity)
                    .background(Color.white)
                    .cornerRadius(16)
                    
                    // Options Card
                    VStack(spacing: 0) {
                        profileOptionRow(icon: "building.2.fill", title: strings.myBusinesses) {
                            onMyBusinessesClick()
                        }
                        Divider()
                        
                        if viewModel.currentUser?.isAdmin == true {
                            profileOptionRow(icon: "shield.fill", title: "Admin Dashboard", color: .purple) {
                                onAdminClick()
                            }
                            Divider()
                        }
                        
                        // Language Switcher Row
                        HStack {
                            Image(systemName: "globe")
                                .foregroundColor(.meTontRed)
                                .frame(width: 24)
                            Text("Language")
                                .font(.body)
                            Spacer()
                            Picker("Language", selection: Binding(
                                get: { currentLanguage },
                                set: { onLanguageChange($0) }
                            )) {
                                Text("English").tag(AppLanguage.en)
                                Text("Shqip").tag(AppLanguage.sq)
                            }
                            .pickerStyle(SegmentedPickerStyle())
                            .frame(width: 140)
                        }
                        .padding(16)
                        
                        Divider()
                        
                        profileOptionRow(icon: "rectangle.portrait.and.arrow.right", title: strings.logout, color: .red) {
                            onLogout()
                        }
                    }
                    .background(Color.white)
                    .cornerRadius(16)
                }
                .padding(16)
            }
        }
        .background(Color.meTontBackground)
    }
    
    private func profileOptionRow(icon: String, title: String, color: Color = .meTontBlack, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.headline)
                    .foregroundColor(color)
                    .frame(width: 24)
                Text(title)
                    .font(.body)
                    .foregroundColor(color)
                Spacer()
                Image(systemName: "chevron.right")
                    .font(.caption)
                    .foregroundColor(.gray)
            }
            .padding(16)
        }
    }
}

// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct UserProfileScreen: View {
    @Environment(\.appStrings) private var strings

    @ObservedObject public var viewModel: AuthViewModel
    @ObservedObject public var mapViewModel: MapViewModel
    public let onBackClick: () -> Void
    public let onLogout: () -> Void
    public let onAdminClick: () -> Void
    public let onMyBusinessesClick: () -> Void
    public let onMyEventsClick: () -> Void
    public let currentLanguage: AppLanguage
    public let onLanguageChange: (AppLanguage) -> Void

    @State private var firstName: String = ""
    @State private var lastName: String = ""
    @State private var isSaving: Bool = false
    @State private var saveMessage: String? = nil

    public init(
        viewModel: AuthViewModel,
        mapViewModel: MapViewModel,
        onBackClick: @escaping () -> Void,
        onLogout: @escaping () -> Void,
        onAdminClick: @escaping () -> Void,
        onMyBusinessesClick: @escaping () -> Void,
        onMyEventsClick: @escaping () -> Void,
        currentLanguage: AppLanguage,
        onLanguageChange: @escaping (AppLanguage) -> Void
    ) {
        self.viewModel = viewModel
        self.mapViewModel = mapViewModel
        self.onBackClick = onBackClick
        self.onLogout = onLogout
        self.onAdminClick = onAdminClick
        self.onMyBusinessesClick = onMyBusinessesClick
        self.onMyEventsClick = onMyEventsClick
        self.currentLanguage = currentLanguage
        self.onLanguageChange = onLanguageChange
    }

    private var ownedBusinesses: [Business] {
        guard let uid = viewModel.currentUser?.uid else { return [] }
        return mapViewModel.businesses.filter { $0.ownerId == uid }
    }

    private var ownedEventCount: Int {
        guard let uid = viewModel.currentUser?.uid else { return 0 }
        return EventsRepository.shared.events.filter { $0.organizerId == uid }.count
    }

    // Mirrors Android's UserProfileScreen: highest-tier owned business wins
    // (sponsored > featured > premium), shown as the real MeTont coin badge
    // instead of a generic icon.
    private var tierBadgeResourceName: String? {
        if ownedBusinesses.contains(where: { $0.isSponsored }) { return "metont_gold" }
        if ownedBusinesses.contains(where: { $0.isFeatured }) { return "metont_silver" }
        if ownedBusinesses.contains(where: { $0.isPremium }) { return "metont_bronze" }
        return nil
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            ScrollView {
                VStack(spacing: 0) {
                    header
                        .padding(.bottom, 16)

                    personalInfoCard
                        .padding(.horizontal, 16)

                    Spacer().frame(height: 12)

                    navCard(
                        icon: "storefront.fill",
                        title: strings.myBusinesses,
                        subtitle: ownedBusinesses.isEmpty
                            ? strings.myBusinessesSubtitle
                            : "\(ownedBusinesses.count) \(ownedBusinesses.count == 1 ? "business" : "businesses")",
                        action: onMyBusinessesClick
                    )
                    .padding(.horizontal, 16)

                    Spacer().frame(height: 12)

                    navCard(
                        icon: "calendar",
                        title: strings.myEvents,
                        subtitle: ownedEventCount > 0
                            ? "\(ownedEventCount) \(ownedEventCount == 1 ? "event" : "events")"
                            : strings.myEventsSubtitle,
                        action: onMyEventsClick
                    )
                    .padding(.horizontal, 16)

                    if viewModel.currentUser?.isAdmin == true {
                        Spacer().frame(height: 12)
                        navCard(
                            icon: "shield.fill",
                            title: "Admin Panel",
                            subtitle: "Manage claims and data",
                            background: Color(red: 0xFF/255.0, green: 0xEB/255.0, blue: 0xEE/255.0),
                            action: onAdminClick
                        )
                        .padding(.horizontal, 16)
                    }

                    Spacer().frame(height: 12)

                    languageCard
                        .padding(.horizontal, 16)

                    Spacer().frame(height: 12)

                    logoutButton
                        .padding(.horizontal, 16)

                    Spacer().frame(height: 32)
                }
            }
        }
        .background(Color.meTontBackground)
        .onAppear {
            if let name = viewModel.currentUser?.firstName, !name.isEmpty {
                firstName = viewModel.currentUser?.firstName ?? ""
                lastName = viewModel.currentUser?.lastName ?? ""
            }
        }
    }

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.myProfile)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    private var header: some View {
        VStack(spacing: 8) {
            if let tierBadgeResourceName {
                TierBadgeImage(resourceName: tierBadgeResourceName)
                    .frame(width: 80, height: 80)
            } else {
                Image(systemName: "person.crop.circle.fill")
                    .font(.system(size: 60))
                    .foregroundColor(.white)
            }

            Text(firstName.isEmpty ? "User" : "\(firstName) \(lastName)")
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.white)
            Text(viewModel.currentUser?.email ?? "")
                .font(.system(size: 13))
                .foregroundColor(.white.opacity(0.8))
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .background(Color.meTontRed)
    }

    private var personalInfoCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text(strings.personalInformation)
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.meTontRed)

            profileTextField(title: strings.firstName, text: $firstName)
            profileTextField(title: strings.lastName, text: $lastName)

            if let saveMessage {
                Text(saveMessage)
                    .font(.caption)
                    .foregroundColor(.gray)
            }

            Button(action: saveProfile) {
                HStack {
                    if isSaving {
                        ProgressView().tint(.white)
                    }
                    Text(strings.saveProfile).fontWeight(.bold)
                }
                .frame(maxWidth: .infinity)
                .frame(height: 48)
                .background(Color.meTontRed)
                .foregroundColor(.white)
                .cornerRadius(12)
            }
            .disabled(isSaving)
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
    }

    private func profileTextField(title: String, text: Binding<String>) -> some View {
        HStack(spacing: 8) {
            Image(systemName: "person.fill").foregroundColor(.meTontRed)
            TextField(title, text: text)
        }
        .padding(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
    }

    private func navCard(icon: String, title: String, subtitle: String, background: Color = .white, action: @escaping () -> Void) -> some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 26))
                .foregroundColor(.meTontRed)
                .frame(width: 32)
            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(.meTontRed)
                Text(subtitle)
                    .font(.system(size: 12))
                    .foregroundColor(.gray)
            }
            Spacer()
            Button(action: action) {
                Text(strings.openButton)
                    .font(.system(size: 12))
                    .foregroundColor(.white)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 8)
                    .background(Color.meTontRed)
                    .cornerRadius(8)
            }
        }
        .padding(16)
        .background(background)
        .cornerRadius(16)
    }

    private var languageCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Language / Gjuha")
                .font(.system(size: 14, weight: .bold))
                .foregroundColor(.meTontRed)
            HStack(spacing: 8) {
                languageButton(title: "🇬🇧 English", isSelected: currentLanguage == .en) {
                    onLanguageChange(.en)
                }
                languageButton(title: "🇦🇱 Shqip", isSelected: currentLanguage == .sq) {
                    onLanguageChange(.sq)
                }
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
    }

    private func languageButton(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 14))
                .frame(maxWidth: .infinity)
                .frame(height: 40)
                .foregroundColor(isSelected ? .white : .meTontRed)
                .background(isSelected ? Color.meTontRed : Color.clear)
                .cornerRadius(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed, lineWidth: 1))
        }
    }

    private var logoutButton: some View {
        Button(action: {
            viewModel.logout()
            onLogout()
        }) {
            HStack {
                Image(systemName: "rectangle.portrait.and.arrow.right")
                Text(strings.logout).fontWeight(.bold)
            }
            .frame(maxWidth: .infinity)
            .frame(height: 48)
            .foregroundColor(.meTontRed)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed, lineWidth: 1))
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
    }

    private func saveProfile() {
        guard !firstName.trimmingCharacters(in: .whitespaces).isEmpty else {
            saveMessage = strings.firstNameRequired
            return
        }
        isSaving = true
        saveMessage = nil
        let displayName = "\(firstName) \(lastName)".trimmingCharacters(in: .whitespaces)
        Task {
            let result = await AuthManager.shared.updateDisplayName(displayName)
            await MainActor.run {
                isSaving = false
                switch result {
                case .success:
                    viewModel.currentUser?.firstName = firstName
                    viewModel.currentUser?.lastName = lastName
                    saveMessage = strings.profileSaved
                case .failure:
                    saveMessage = strings.profileSaveFailed
                }
            }
        }
    }
}

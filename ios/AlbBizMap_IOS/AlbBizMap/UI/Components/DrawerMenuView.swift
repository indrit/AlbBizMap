// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct DrawerMenuView: View {
    @Environment(\.appStrings) private var strings
    
    public let isOpen: Bool
    public let onClose: () -> Void
    public let onNavigate: (String) -> Void
    public let currentLanguage: AppLanguage
    public let currentUserName: String
    public let onLogout: () -> Void
    
    public init(isOpen: Bool, onClose: @escaping () -> Void, onNavigate: @escaping (String) -> Void, currentLanguage: AppLanguage, currentUserName: String = "User", onLogout: @escaping () -> Void) {
        self.isOpen = isOpen
        self.onClose = onClose
        self.onNavigate = onNavigate
        self.currentLanguage = currentLanguage
        self.currentUserName = currentUserName
        self.onLogout = onLogout
    }
    
    private var firstName: String {
        currentUserName.split(separator: " ").first.map(String.init) ?? currentUserName
    }
    
    public var body: some View {
        ZStack(alignment: .leading) {
            if isOpen {
                Color.black.opacity(0.4)
                    .edgesIgnoringSafeArea(.all)
                    .onTapGesture {
                        onClose()
                    }
                
                VStack(alignment: .leading, spacing: 0) {
                    // Header block: red header (fixed 180pt, matching Android) + welcome bar,
                    // combined into one continuous piece so there's no seam/gap between them.
                    VStack(spacing: 0) {
                        VStack(alignment: .center, spacing: 8) {
                            MeTontLogoImage()
                                .frame(width: 80, height: 80)
                            Text(strings.appName)
                                .font(.title2)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                            Text("Albanian Business Map & Directory")
                                .font(.caption)
                                .foregroundColor(.white.opacity(0.8))
                        }
                        .frame(maxWidth: .infinity)
                        .frame(height: 180)
                        .background(
                            ZStack {
                                Color.meTontRed
                                MeTontLogoImage(contentMode: .fill)
                                    .opacity(0.15)
                                    .clipped()
                            }
                        )
                        
                        Text("\(strings.welcomeUser), \(firstName)")
                            .font(.system(size: 14, weight: .medium))
                            .foregroundColor(.white)
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .padding(.horizontal, 16)
                            .background(Color.meTontRedDark)
                    }
                    .clipped()
                    
                    // Menu Items
                    ScrollView {
                        VStack(alignment: .leading, spacing: 4) {
                            drawerItem(icon: "person.circle.fill", title: strings.profile) {
                                onNavigate("profile")
                            }
                            drawerItem(icon: "heart.fill", title: strings.favorites) {
                                onNavigate("favorites")
                            }
                            drawerItem(icon: "calendar", title: strings.communityEvents) {
                                onNavigate("events")
                            }
                            drawerItem(icon: "briefcase.fill", title: strings.jobs) {
                                onNavigate("jobs")
                            }
                            drawerItem(icon: "plus.circle.fill", title: strings.addBusiness) {
                                onNavigate("addBusiness")
                            }
                            drawerItem(icon: "list.bullet", title: strings.listView) {
                                onNavigate("businessList")
                            }
                        }
                        .padding(.vertical, 12)
                    }
                    
                    Spacer()
                    
                    Divider()
                    drawerItem(icon: "rectangle.portrait.and.arrow.right", title: strings.logout, iconColor: .meTontRed, textColor: .meTontRed, bold: true) {
                        onLogout()
                    }
                    .padding(.bottom, 12)
                }
                .frame(width: 280)
                .background(Color.white)
                .edgesIgnoringSafeArea(.all)
                .transition(.move(edge: .leading))
            }
        }
        .animation(.easeInOut(duration: 0.25), value: isOpen)
    }
    
    private func drawerItem(icon: String, title: String, iconColor: Color = .meTontRed, textColor: Color = .meTontBlack, bold: Bool = false, action: @escaping () -> Void) -> some View {
        Button(action: {
            onClose()
            action()
        }) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.headline)
                    .foregroundColor(iconColor)
                    .frame(width: 24)
                Text(title)
                    .font(.body)
                    .fontWeight(bold ? .bold : .medium)
                    .foregroundColor(textColor)
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
        }
    }
}

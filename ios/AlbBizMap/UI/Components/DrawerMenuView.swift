// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct DrawerMenuView: View {
    @Environment(\.appStrings) private var strings
    
    public let isOpen: Bool
    public let onClose: () -> Void
    public let onNavigate: (String) -> Void
    public let currentLanguage: AppLanguage
    public let onLogout: () -> Void
    
    public init(isOpen: Bool, onClose: @escaping () -> Void, onNavigate: @escaping (String) -> Void, currentLanguage: AppLanguage, onLogout: @escaping () -> Void) {
        self.isOpen = isOpen
        self.onClose = onClose
        self.onNavigate = onNavigate
        self.currentLanguage = currentLanguage
        self.onLogout = onLogout
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
                    // Header
                    VStack(alignment: .leading, spacing: 8) {
                        HStack {
                            Image(systemName: "map.fill")
                                .font(.title)
                                .foregroundColor(.white)
                            Text(strings.appName)
                                .font(.title)
                                .fontWeight(.bold)
                                .foregroundColor(.white)
                        }
                        Text("Albanian Business Map & Directory")
                            .font(.caption)
                            .foregroundColor(.white.opacity(0.8))
                    }
                    .padding(.top, 60)
                    .padding(.horizontal, 20)
                    .padding(.bottom, 24)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(Color.meTontRed)
                    
                    // Menu Items
                    ScrollView {
                        VStack(alignment: .leading, spacing: 4) {
                            drawerItem(icon: "list.bullet", title: strings.directory) {
                                onNavigate("businessList")
                            }
                            drawerItem(icon: "plus.circle.fill", title: strings.addBusiness) {
                                onNavigate("addBusiness")
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
                            drawerItem(icon: "person.circle.fill", title: strings.profile) {
                                onNavigate("profile")
                            }
                            
                            Divider().padding(.vertical, 8)
                            
                            drawerItem(icon: "rectangle.portrait.and.arrow.right", title: strings.logout, color: .red) {
                                onLogout()
                            }
                        }
                        .padding(.vertical, 12)
                    }
                    
                    Spacer()
                }
                .frame(width: 280)
                .background(Color.white)
                .edgesIgnoringSafeArea(.all)
                .transition(.move(edge: .leading))
            }
        }
        .animation(.easeInOut(duration: 0.25), value: isOpen)
    }
    
    private func drawerItem(icon: String, title: String, color: Color = .meTontBlack, action: @escaping () -> Void) -> some View {
        Button(action: {
            onClose()
            action()
        }) {
            HStack(spacing: 16) {
                Image(systemName: icon)
                    .font(.headline)
                    .foregroundColor(color)
                    .frame(width: 24)
                Text(title)
                    .font(.body)
                    .fontWeight(.medium)
                    .foregroundColor(color)
                Spacer()
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 12)
        }
    }
}

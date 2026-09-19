// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct SubscriptionScreen: View {
    @Environment(\.appStrings) private var strings
    
    public var business: Business
    public let onBackClick: () -> Void
    
    public init(business: Business, onBackClick: @escaping () -> Void) {
        self.business = business
        self.onBackClick = onBackClick
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.choosePlan)
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            ScrollView {
                VStack(spacing: 16) {
                    planCard(
                        title: strings.premium,
                        price: "$2.99 / month",
                        features: ["Verified Business Badge", "Up to 6 Photos", "Extended Long Description", "Contact Info & Website"],
                        isCurrent: business.isPremium,
                        color: .orange
                    )
                    
                    planCard(
                        title: strings.sponsored,
                        price: "$9.99 / month",
                        features: ["Gold Map Marker Pin", "Top Placement on Search", "Up to 14 Photos", "Highlighted Banner"],
                        isCurrent: business.isSponsored,
                        color: .meTontRed
                    )
                }
                .padding(16)
            }
        }
        .background(Color.meTontBackground)
    }
    
    private func planCard(title: String, price: String, features: [String], isCurrent: Bool, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text(title).font(.title3).fontWeight(.bold)
                Spacer()
                if isCurrent {
                    Text(strings.currentPlan)
                        .font(.caption).fontWeight(.bold)
                        .padding(.horizontal, 8).padding(.vertical, 4)
                        .background(Color.green.opacity(0.2)).foregroundColor(.green)
                        .cornerRadius(6)
                }
            }
            Text(price).font(.title2).fontWeight(.bold).foregroundColor(color)
            
            VStack(alignment: .leading, spacing: 6) {
                ForEach(features, id: \.self) { ft in
                    HStack(spacing: 8) {
                        Image(systemName: "checkmark.circle.fill").foregroundColor(color)
                        Text(ft).font(.subheadline)
                    }
                }
            }
            
            Button(action: {
                // Trigger plan request
            }) {
                Text(strings.requestUpgrade)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(color)
                    .foregroundColor(.white)
                    .cornerRadius(10)
            }
        }
        .padding(18)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(radius: 4)
    }
}

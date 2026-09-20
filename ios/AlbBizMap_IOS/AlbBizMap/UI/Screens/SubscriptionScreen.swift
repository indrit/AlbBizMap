// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Mirrors Android's SubscriptionScreen.kt: four plan cards (Free, Premium,
// Featured, Sponsored) with real feature checklists and current-tier
// detection, instead of the old two-card placeholder.
public struct SubscriptionScreen: View {
    @Environment(\.appStrings) private var strings

    public var business: Business
    public let onBackClick: () -> Void

    public init(business: Business, onBackClick: @escaping () -> Void) {
        self.business = business
        self.onBackClick = onBackClick
    }

    private enum Tier { case free, premium, featured, sponsored }

    private var currentTier: Tier {
        if business.isSponsored { return .sponsored }
        if business.isFeatured { return .featured }
        if business.isPremium { return .premium }
        return .free
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
            .padding()
            .background(Color.white)

            ScrollView {
                VStack(spacing: 16) {
                    header

                    planCard(
                        title: strings.freeTierName,
                        price: "$0",
                        period: strings.forever,
                        accentColor: .gray,
                        isCurrent: currentTier == .free,
                        features: [
                            (strings.planFeatureNameCategory, true),
                            (strings.planFeatureLocationOnMap, true),
                            (strings.planFeature100CharDesc, true),
                            (strings.planFeature1Photo, true)
                        ],
                        buttonText: currentTier == .free ? strings.currentPlanButton : strings.notAvailableDash,
                        isCurrentPlan: currentTier == .free,
                        onButtonClick: {}
                    )

                    planCard(
                        title: strings.premium,
                        price: "$2.99",
                        period: strings.perMonth,
                        accentColor: .tierBronze,
                        isCurrent: currentTier == .premium,
                        features: [
                            (strings.planFeatureEverythingPremium, true),
                            (strings.planFeatureUp6Photos, true),
                            (strings.planFeaturePhoneNumber, true),
                            (strings.planFeatureEmailWebsite, true),
                            (strings.planFeaturePremiumBadge, true)
                        ],
                        buttonText: currentTier == .premium ? strings.currentPlanButton : strings.requestUpgrade,
                        isCurrentPlan: currentTier == .premium,
                        onButtonClick: { requestPlan("premium_subscription") }
                    )

                    planCard(
                        title: strings.featured2,
                        price: "$9.99",
                        period: strings.perMonth,
                        accentColor: .tierSilver,
                        isCurrent: currentTier == .featured,
                        features: [
                            (strings.planFeatureEverythingPremium, true),
                            (strings.planFeatureUp10Photos, true),
                            (strings.planFeatureFeaturedBadge, true),
                            (strings.planFeatureFeaturedDiscoveryRow, true)
                        ],
                        buttonText: currentTier == .featured ? strings.currentPlanButton : strings.requestFeatured,
                        isCurrentPlan: currentTier == .featured,
                        onButtonClick: { requestPlan("featured_subscription") }
                    )

                    planCard(
                        title: strings.sponsored,
                        price: "$19.99",
                        period: strings.perMonth,
                        accentColor: .tierGold,
                        isCurrent: currentTier == .sponsored,
                        features: [
                            (strings.planFeatureEverythingPremium, true),
                            (strings.planFeatureUp14Photos, true),
                            (strings.planFeatureHighlightedMapPin, true),
                            (strings.planFeatureTopSearchResults, true),
                            (strings.planFeatureSponsoredBadge, true)
                        ],
                        buttonText: currentTier == .sponsored ? strings.currentPlanButton : strings.requestSponsorship,
                        isCurrentPlan: currentTier == .sponsored,
                        onButtonClick: { requestPlan("sponsored_subscription") }
                    )
                }
                .padding(16)
            }
        }
        .background(Color.meTontBackground)
    }

    private var header: some View {
        VStack(spacing: 8) {
            Image(systemName: "star.fill")
                .font(.system(size: 32))
                .foregroundColor(.white)
            Text(strings.choosePlan)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.white)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .background(Color.meTontRed)
        .cornerRadius(16)
    }

    // Android's buttons call BillingViewModel.launchBillingFlow (Google Play
    // Billing). Porting real in-app purchases needs App Store Connect product
    // setup + StoreKit 2 integration — out of scope for this pass, flagged
    // rather than faked. The buttons render and route correctly; wiring an
    // actual purchase/request flow is a follow-up.
    private func requestPlan(_ planId: String) {
        // Intentionally a no-op for now — see comment above.
    }

    private func planCard(
        title: String,
        price: String,
        period: String,
        accentColor: Color,
        isCurrent: Bool,
        features: [(String, Bool)],
        buttonText: String,
        isCurrentPlan: Bool,
        onButtonClick: @escaping () -> Void
    ) -> some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                HStack(spacing: 8) {
                    Circle().fill(accentColor).frame(width: 12, height: 12)
                    Text(title).font(.title3).fontWeight(.bold).foregroundColor(.black)
                }
                Spacer()
                if isCurrent {
                    Text(strings.currentPlan)
                        .font(.caption2).fontWeight(.bold)
                        .padding(.horizontal, 8).padding(.vertical, 4)
                        .background(Color.gray.opacity(0.15)).foregroundColor(.gray)
                        .cornerRadius(6)
                }
            }

            HStack(alignment: .bottom, spacing: 4) {
                Text(price).font(.system(size: 32)).fontWeight(.bold).foregroundColor(accentColor)
                Text(period).font(.caption).foregroundColor(.secondary).padding(.bottom, 4)
            }

            Divider()

            VStack(alignment: .leading, spacing: 8) {
                ForEach(features, id: \.0) { feature, included in
                    HStack(spacing: 10) {
                        Image(systemName: included ? "checkmark.circle.fill" : "xmark.circle.fill")
                            .font(.system(size: 14))
                            .foregroundColor(included ? Color(red: 0.3, green: 0.69, blue: 0.31) : Color(white: 0.88))
                        Text(feature)
                            .font(.subheadline)
                            .foregroundColor(included ? .black : .secondary)
                    }
                }
            }

            Button(action: onButtonClick) {
                Text(buttonText)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .background(isCurrentPlan ? Color(white: 0.93) : accentColor)
                    .foregroundColor(isCurrentPlan ? .secondary : .white)
                    .cornerRadius(12)
            }
            .disabled(isCurrentPlan)
        }
        .padding(20)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
    }
}

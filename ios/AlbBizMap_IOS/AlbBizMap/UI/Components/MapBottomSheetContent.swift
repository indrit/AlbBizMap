// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// The map screen's persistent bottom sheet: Stories bar + the four business/event
// carousels (Top Recommended, Near You, Community Announcements, Most Favorited
// Worldwide). Mirrors the sheetContent Column in Android's MapScreen.kt.
public struct MapBottomSheetContent: View {
    @Environment(\.appStrings) private var strings

    @ObservedObject public var viewModel: MapViewModel
    @ObservedObject public var storiesViewModel: StoriesViewModel
    @ObservedObject public var authViewModel: AuthViewModel
    @ObservedObject private var eventsRepository = EventsRepository.shared

    public let onAddStoryClick: () -> Void
    public let onStoryClick: (Int) -> Void
    public let onBusinessClick: (String) -> Void
    public let onEventsClick: () -> Void
    public let onMostFavoritedSeeMore: () -> Void

    @State private var topRecommendedExpanded = false
    @State private var nearYouExpanded = false

    public init(
        viewModel: MapViewModel,
        storiesViewModel: StoriesViewModel,
        authViewModel: AuthViewModel,
        onAddStoryClick: @escaping () -> Void,
        onStoryClick: @escaping (Int) -> Void,
        onBusinessClick: @escaping (String) -> Void,
        onEventsClick: @escaping () -> Void,
        onMostFavoritedSeeMore: @escaping () -> Void
    ) {
        self.viewModel = viewModel
        self.storiesViewModel = storiesViewModel
        self.authViewModel = authViewModel
        self.onAddStoryClick = onAddStoryClick
        self.onStoryClick = onStoryClick
        self.onBusinessClick = onBusinessClick
        self.onEventsClick = onEventsClick
        self.onMostFavoritedSeeMore = onMostFavoritedSeeMore
    }

    public var body: some View {
        // StoryBarView is deliberately OUTSIDE the ScrollView below, as a fixed
        // (non-scrolling) header — not nested inside it. SwiftUI's ScrollView is
        // backed by UIScrollView, and a horizontal ScrollView (the story row) nested
        // inside a vertical one silently swallows taps on real devices (confirmed by
        // testing an identical row rendered outside any scroll nesting elsewhere on
        // this screen: taps worked instantly there, and nowhere inside this ScrollView).
        // Keeping it as a fixed header sidesteps that nested-scroll problem entirely,
        // and also matches Android's layout, which pins the story bar above the
        // scrollable carousels rather than scrolling it with them.
        VStack(spacing: 0) {
            StoryBarView(
                stories: storiesViewModel.stories,
                currentUserId: authViewModel.currentUser?.uid ?? "",
                onAddStoryClick: onAddStoryClick,
                onStoryClick: onStoryClick
            )
            .padding(.top, 8)

            Divider().padding(.horizontal, 16)

            ScrollView {
                VStack(spacing: 0) {
                    topRecommendedSection
                    nearYouSection
                    communityAnnouncementsSection
                    mostFavoritedSection

                    Spacer(minLength: 24)
                }
            }
        }
    }

    private func sectionTitle(_ text: String) -> some View {
        Text(text)
            .font(.system(size: 17, weight: .bold))
            .foregroundColor(.meTontBlack)
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)
            .frame(maxWidth: .infinity, alignment: .leading)
    }

    @ViewBuilder
    private var topRecommendedSection: some View {
        let topPicks = viewModel.topPicks
        if !topPicks.isEmpty {
            sectionTitle(strings.topRecommended)
            let visible = topRecommendedExpanded ? topPicks : Array(topPicks.prefix(2))
            VStack(spacing: 12) {
                ForEach(visible) { business in
                    FeaturedPickCard(business: business) { onBusinessClick(business.id) }
                }
            }
            .padding(.horizontal, 16)
            if topPicks.count > 2 {
                SeeMoreButton(expanded: topRecommendedExpanded) {
                    topRecommendedExpanded.toggle()
                }
                .padding(.horizontal, 16)
            }
        }
    }

    @ViewBuilder
    private var nearYouSection: some View {
        sectionTitle(strings.nearYou)
        let nearMe = viewModel.nearMe
        if nearMe.isEmpty {
            VStack(spacing: 4) {
                Image(systemName: "location.slash")
                    .font(.system(size: 28))
                    .foregroundColor(.meTontGrey.opacity(0.5))
                Text(strings.noBusinessesNearYou)
                    .font(.system(size: 13, weight: .medium))
                    .foregroundColor(.meTontGrey)
                Text(strings.appGrowingMessage)
                    .font(.system(size: 11))
                    .foregroundColor(.meTontGrey.opacity(0.7))
            }
            .frame(maxWidth: .infinity)
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        } else {
            let visible = nearYouExpanded ? nearMe : Array(nearMe.prefix(2))
            VStack(spacing: 12) {
                ForEach(visible) { business in
                    FeaturedPickCard(business: business) { onBusinessClick(business.id) }
                }
            }
            .padding(.horizontal, 16)
            if nearMe.count > 2 {
                SeeMoreButton(expanded: nearYouExpanded) {
                    nearYouExpanded.toggle()
                }
                .padding(.horizontal, 16)
            }
        }
    }

    @ViewBuilder
    private var communityAnnouncementsSection: some View {
        sectionTitle(strings.communityAnnouncements)
        let announcements = eventsRepository.events
        if announcements.isEmpty {
            Text(strings.noUpcomingEventsShort)
                .font(.system(size: 13))
                .foregroundColor(.meTontGrey)
                .padding(.horizontal, 16)
        } else {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(Array(announcements.prefix(6))) { event in
                        EventAnnouncementCard(event: event) { onEventsClick() }
                    }
                }
                .padding(.horizontal, 16)
            }
            if announcements.count > 6 {
                SeeMoreNavButton { onEventsClick() }
                    .padding(.horizontal, 16)
            }
        }
    }

    @ViewBuilder
    private var mostFavoritedSection: some View {
        sectionTitle(strings.mostFavoritedWorldwide)
        let mostFavorited = viewModel.mostFavorited
        if mostFavorited.isEmpty {
            Text(strings.noBusinessesYetHome)
                .font(.system(size: 13))
                .foregroundColor(.meTontGrey)
                .padding(.horizontal, 16)
        } else {
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 12) {
                    ForEach(Array(mostFavorited.prefix(6))) { business in
                        MapBusinessCard(business: business) { onBusinessClick(business.id) }
                    }
                }
                .padding(.horizontal, 16)
            }
            if mostFavorited.count > 6 {
                SeeMoreNavButton { onMostFavoritedSeeMore() }
                    .padding(.horizontal, 16)
            }
        }
    }
}

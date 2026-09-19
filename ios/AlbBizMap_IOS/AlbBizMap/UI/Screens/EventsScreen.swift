// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import UIKit

public struct EventsScreen: View {
    @Environment(\.appStrings) private var strings

    @ObservedObject private var repo = EventsRepository.shared
    public let onBackClick: () -> Void
    public let onAddEventClick: () -> Void

    public init(onBackClick: @escaping () -> Void, onAddEventClick: @escaping () -> Void) {
        self.onBackClick = onBackClick
        self.onAddEventClick = onAddEventClick
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            ZStack(alignment: .bottomTrailing) {
                if repo.events.isEmpty {
                    emptyState
                } else {
                    ScrollView {
                        VStack(alignment: .leading, spacing: 16) {
                            Text("\(repo.events.count) upcoming \(repo.events.count == 1 ? "event" : "events")")
                                .font(.system(size: 13))
                                .foregroundColor(.meTontGrey)

                            ForEach(repo.events) { evt in
                                EventItemCard(event: evt)
                            }
                        }
                        .padding(16)
                    }
                }

                Button(action: onAddEventClick) {
                    Image(systemName: "plus")
                        .font(.system(size: 22, weight: .bold))
                        .foregroundColor(.white)
                        .frame(width: 56, height: 56)
                        .background(Color.meTontRed)
                        .clipShape(Circle())
                        .shadow(color: .black.opacity(0.25), radius: 6, x: 0, y: 3)
                }
                .padding(20)
            }
        }
        .background(Color.meTontBackground)
    }

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.communityEventsTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            ZStack {
                Circle().fill(Color.meTontRed.opacity(0.1)).frame(width: 80, height: 80)
                Image(systemName: "calendar")
                    .font(.system(size: 34))
                    .foregroundColor(.meTontRed.opacity(0.5))
            }
            Text(strings.noEventsFound)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.meTontGrey)
            Text("Be the first to add a community event!")
                .font(.system(size: 13))
                .foregroundColor(.meTontGrey.opacity(0.7))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct EventItemCard: View {
    @Environment(\.appStrings) private var strings
    let event: Event
    @State private var expanded: Bool = false

    private var formattedDate: String {
        let date = Date(timeIntervalSince1970: TimeInterval(event.date) / 1000.0)
        let formatter = DateFormatter()
        formatter.dateFormat = "EEEE, MMM d, yyyy 'at' h:mm a"
        return formatter.string(from: date)
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // Event image / placeholder
            if let imgUrl = event.imageUrl, let url = URL(string: imgUrl) {
                AsyncImage(url: url) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color.gray.opacity(0.15)
                }
                .frame(height: 180)
                .clipped()
            } else {
                ZStack {
                    Color.meTontRed.opacity(0.1)
                    Image(systemName: "calendar")
                        .font(.system(size: 40))
                        .foregroundColor(.meTontRed.opacity(0.4))
                }
                .frame(height: 100)
            }

            VStack(alignment: .leading, spacing: 0) {
                // Title + Promoted badge
                HStack(alignment: .top, spacing: 8) {
                    Text(event.title)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.black)
                        .frame(maxWidth: .infinity, alignment: .leading)

                    if event.isPromoted {
                        Text(strings.promoted)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(.white)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 3)
                            .background(Color(red: 0xFF/255.0, green: 0xC1/255.0, blue: 0x07/255.0))
                            .cornerRadius(6)
                    }
                }

                // Category chip
                Text(event.category)
                    .font(.system(size: 11, weight: .medium))
                    .foregroundColor(.meTontRed)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 3)
                    .background(Color.meTontRed.opacity(0.1))
                    .cornerRadius(6)
                    .padding(.top, 4)

                Spacer().frame(height: 12)

                // Date
                HStack(spacing: 6) {
                    Image(systemName: "calendar")
                        .font(.system(size: 14))
                        .foregroundColor(.meTontRed)
                    Text(formattedDate)
                        .font(.system(size: 13))
                        .foregroundColor(.meTontGrey)
                }

                Spacer().frame(height: 6)

                // Location
                HStack(spacing: 6) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.system(size: 14))
                        .foregroundColor(.meTontRed)
                    Text(event.locationName)
                        .font(.system(size: 13))
                        .foregroundColor(.meTontGrey)
                }

                Spacer().frame(height: 10)

                // Description
                Text(event.description)
                    .font(.system(size: 13))
                    .foregroundColor(.black.opacity(0.7))
                    .lineLimit(expanded ? nil : 3)
                    .onTapGesture { expanded.toggle() }

                Text(expanded ? strings.readLess : strings.readMore)
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(.meTontRed)
                    .padding(.top, 4)
                    .onTapGesture { expanded.toggle() }

                if let websiteUrl = event.websiteUrl, !websiteUrl.isEmpty {
                    Spacer().frame(height: 8)
                    Button(action: {
                        var urlString = websiteUrl
                        if !urlString.hasPrefix("http://") && !urlString.hasPrefix("https://") {
                            urlString = "https://\(urlString)"
                        }
                        if let url = URL(string: urlString) {
                            UIApplication.shared.open(url)
                        }
                    }) {
                        HStack(spacing: 4) {
                            Image(systemName: "globe").font(.system(size: 12))
                            Text(strings.viewEventWebsite).font(.system(size: 12, weight: .medium))
                        }
                        .foregroundColor(.meTontRed)
                        .padding(.horizontal, 12)
                        .padding(.vertical, 6)
                        .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.meTontRed, lineWidth: 1))
                    }
                }
            }
            .padding(16)
        }
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

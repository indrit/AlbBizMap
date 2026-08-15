// Bismillah Hir Rahman Nir Raheem
import SwiftUI

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
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.communityEventsTitle)
                    .font(.title2).fontWeight(.bold)
                Spacer()
                Button(action: onAddEventClick) {
                    Image(systemName: "plus.circle.fill").font(.title2).foregroundColor(.meTontRed)
                }
            }
            .padding().background(Color.white)
            
            if repo.events.isEmpty {
                VStack {
                    Spacer()
                    Text(strings.noEventsFound).foregroundColor(.gray)
                    Spacer()
                }
            } else {
                ScrollView {
                    VStack(spacing: 16) {
                        ForEach(repo.events) { evt in
                            VStack(alignment: .leading, spacing: 10) {
                                if let imgUrl = evt.imageUrl, let url = URL(string: imgUrl) {
                                    AsyncImage(url: url) { image in
                                        image.resizable().aspectRatio(contentMode: .fill)
                                    } placeholder: {
                                        Color.gray.opacity(0.2)
                                    }
                                    .frame(height: 160)
                                    .cornerRadius(12)
                                }
                                
                                Text(evt.title)
                                    .font(.headline)
                                    .fontWeight(.bold)
                                
                                Text(evt.description)
                                    .font(.subheadline)
                                    .foregroundColor(.secondary)
                                
                                HStack {
                                    Image(systemName: "mappin.circle").foregroundColor(.meTontRed)
                                    Text(evt.locationName).font(.caption)
                                    Spacer()
                                    Image(systemName: "calendar").foregroundColor(.meTontRed)
                                    Text("Upcoming").font(.caption)
                                }
                            }
                            .padding(14)
                            .background(Color.white)
                            .cornerRadius(14)
                            .shadow(color: Color.black.opacity(0.05), radius: 4, x: 0, y: 2)
                        }
                    }
                    .padding(16)
                }
            }
        }
        .background(Color.meTontBackground)
    }
}

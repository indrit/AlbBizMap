// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// The events a logged-in user submitted — reachable from the "My Events" card
// on the Profile screen. Mirrors Android's MyEventsScreen.kt: same structure
// as MyBusinessesScreen, but (unlike businesses, which have no in-app delete
// yet) exposes a delete button per row.
public struct MyEventsScreen: View {
    @Environment(\.appStrings) private var strings

    @ObservedObject private var eventsRepository = EventsRepository.shared
    public let onBackClick: () -> Void
    public let onAddEventClick: () -> Void

    @State private var eventPendingDelete: Event? = nil
    @State private var isDeleting = false

    public init(onBackClick: @escaping () -> Void, onAddEventClick: @escaping () -> Void) {
        self.onBackClick = onBackClick
        self.onAddEventClick = onAddEventClick
    }

    private var ownedEvents: [Event] {
        let uid = AuthManager.shared.currentUser?.uid ?? ""
        return eventsRepository.getEventsByOrganizer(organizerId: uid).sorted { $0.date < $1.date }
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            if ownedEvents.isEmpty {
                emptyState
            } else {
                ScrollView {
                    VStack(spacing: 10) {
                        Text("\(ownedEvents.count) \(ownedEvents.count == 1 ? "event" : "events")")
                            .font(.system(size: 13))
                            .foregroundColor(.meTontGrey)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 16)

                        ForEach(ownedEvents) { event in
                            MyEventRow(event: event, onDeleteClick: { eventPendingDelete = event })
                        }

                        Button(action: onAddEventClick) {
                            HStack {
                                Image(systemName: "plus")
                                Text(strings.submitEventButton)
                            }
                            .frame(maxWidth: .infinity)
                            .frame(height: 44)
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed, lineWidth: 1))
                            .foregroundColor(.meTontRed)
                        }
                        .padding(.horizontal, 16)
                    }
                    .padding(.vertical, 16)
                }
            }
        }
        .background(Color.meTontBackground)
        .alert(strings.deleteEventConfirmTitle, isPresented: Binding(
            get: { eventPendingDelete != nil },
            set: { if !$0 { eventPendingDelete = nil } }
        )) {
            Button(strings.cancel, role: .cancel) { eventPendingDelete = nil }
            Button(strings.deleteEvent, role: .destructive) {
                guard let event = eventPendingDelete else { return }
                isDeleting = true
                Task {
                    _ = await eventsRepository.deleteEvent(event)
                    await MainActor.run {
                        isDeleting = false
                        eventPendingDelete = nil
                    }
                }
            }
            .disabled(isDeleting)
        } message: {
            Text(strings.deleteEventConfirmMessage)
        }
    }

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.myEvents)
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
            Spacer()
            ZStack {
                Circle()
                    .fill(Color.meTontRed.opacity(0.1))
                    .frame(width: 80, height: 80)
                Image(systemName: "calendar")
                    .font(.system(size: 32))
                    .foregroundColor(.meTontRed.opacity(0.5))
            }
            Text(strings.noEventsYet)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.meTontGrey)
                .multilineTextAlignment(.center)
            Text(strings.noEventsYetSubtitle)
                .font(.system(size: 13))
                .foregroundColor(.meTontGrey.opacity(0.7))
                .multilineTextAlignment(.center)
            Button(action: onAddEventClick) {
                HStack {
                    Image(systemName: "plus")
                    Text(strings.submitEventButton).fontWeight(.bold)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 10)
                .background(Color.meTontRed)
                .foregroundColor(.white)
                .cornerRadius(24)
            }
            Spacer()
        }
        .padding(.horizontal, 32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

// MARK: - Row

private struct MyEventRow: View {
    let event: Event
    let onDeleteClick: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            if let imageUrl = event.imageUrl, !imageUrl.isEmpty {
                AsyncImage(url: URL(string: imageUrl)) { image in
                    image.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color.meTontRed.opacity(0.08)
                }
                .frame(width: 56, height: 56)
                .clipped()
                .cornerRadius(10)
            } else {
                ZStack {
                    RoundedRectangle(cornerRadius: 10).fill(Color.meTontRed.opacity(0.08))
                    Image(systemName: "calendar").foregroundColor(.meTontRed.opacity(0.5))
                }
                .frame(width: 56, height: 56)
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(event.title)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(.black)
                    .lineLimit(1)
                Text(Self.formattedDate(event.date))
                    .font(.caption)
                    .foregroundColor(.meTontGrey)
                HStack(spacing: 2) {
                    Image(systemName: "mappin.circle.fill")
                        .font(.system(size: 12))
                        .foregroundColor(.meTontGrey)
                    Text(event.locationName)
                        .font(.caption)
                        .foregroundColor(.meTontGrey)
                        .lineLimit(1)
                }
            }

            Spacer()

            Button(action: onDeleteClick) {
                Image(systemName: "trash").foregroundColor(.meTontRed)
            }
        }
        .padding(10)
        .background(Color.white)
        .cornerRadius(14)
        .shadow(color: .black.opacity(0.05), radius: 2, y: 1)
        .padding(.horizontal, 16)
    }

    private static func formattedDate(_ millis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(millis) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "EEE, MMM dd, yyyy 'at' h:mm a"
        return formatter.string(from: date)
    }
}

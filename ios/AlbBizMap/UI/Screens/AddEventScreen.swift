// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct AddEventScreen: View {
    @Environment(\.appStrings) private var strings
    
    public let onBackClick: () -> Void
    public let onEventAdded: () -> Void
    
    @State private var title: String = ""
    @State private var description: String = ""
    @State private var locationName: String = ""
    @State private var websiteUrl: String = ""
    @State private var category: String = "Cultural"
    @State private var isSubmitting: Bool = false
    
    public init(onBackClick: @escaping () -> Void, onEventAdded: @escaping () -> Void) {
        self.onBackClick = onBackClick
        self.onEventAdded = onEventAdded
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.submitEvent)
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            Form {
                Section(header: Text(strings.eventDetailsSection)) {
                    TextField(strings.eventTitle, text: $title)
                    TextField(strings.eventDescription, text: $description)
                    TextField(strings.eventLocation, text: $locationName)
                    TextField(strings.eventWebsite, text: $websiteUrl)
                }
                
                Button(action: {
                    let evt = Event(
                        title: title,
                        description: description,
                        locationName: locationName,
                        category: category,
                        organizerId: AuthManager.shared.currentUser?.uid ?? "",
                        websiteUrl: websiteUrl
                    )
                    isSubmitting = true
                    Task {
                        _ = await EventsRepository.shared.addEvent(evt)
                        await MainActor.run {
                            isSubmitting = false
                            onEventAdded()
                        }
                    }
                }) {
                    Text(isSubmitting ? strings.submitting : strings.submitEventButton)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .foregroundColor(.white)
                        .background(Color.meTontRed)
                        .cornerRadius(10)
                }
            }
        }
    }
}

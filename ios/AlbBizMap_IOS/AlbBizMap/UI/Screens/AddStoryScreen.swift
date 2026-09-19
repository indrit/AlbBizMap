// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct AddStoryScreen: View {
    @Environment(\.appStrings) private var strings
    
    public let onBackClick: () -> Void
    public let onStoryPosted: () -> Void
    @ObservedObject public var mapViewModel: MapViewModel
    @ObservedObject public var storiesViewModel: StoriesViewModel
    
    @State private var text: String = ""
    @State private var location: String = ""
    @State private var isSubmitting: Bool = false
    
    public init(onBackClick: @escaping () -> Void, onStoryPosted: @escaping () -> Void, mapViewModel: MapViewModel, storiesViewModel: StoriesViewModel) {
        self.onBackClick = onBackClick
        self.onStoryPosted = onStoryPosted
        self.mapViewModel = mapViewModel
        self.storiesViewModel = storiesViewModel
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text("Post a Story")
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            Form {
                Section(header: Text("Story Text")) {
                    TextEditor(text: $text).frame(height: 100)
                }
                
                Section(header: Text("Location")) {
                    TextField("Location (e.g., Tirana, NYC)", text: $location)
                }
                
                Button(action: {
                    let user = AuthManager.shared.currentUser
                    let story = Story(
                        userId: user?.uid ?? "",
                        userName: "\(user?.firstName ?? "User") \(user?.lastName ?? "")".trimmingCharacters(in: .whitespaces),
                        location: location,
                        text: text
                    )
                    isSubmitting = true
                    Task {
                        _ = await storiesViewModel.postStory(story: story)
                        await MainActor.run {
                            isSubmitting = false
                            onStoryPosted()
                        }
                    }
                }) {
                    Text(isSubmitting ? strings.submitting : "Post Story")
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .foregroundColor(.white)
                        .background(Color.meTontRed)
                        .cornerRadius(10)
                }
            }
        }
        .background(Color.meTontBackground)
    }
}

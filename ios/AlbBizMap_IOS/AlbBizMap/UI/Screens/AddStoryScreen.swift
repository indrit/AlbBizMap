// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import PhotosUI

// Mirrors Android's AddStoryScreen.kt: a multi-photo grid (up to 10, with
// per-photo remove), a story-type chip row (user/business/community), a
// business picker for the "business" type, caption and location fields, and
// a Post button that's disabled until at least one photo is picked.
public struct AddStoryScreen: View {
    @Environment(\.appStrings) private var strings

    public let onBackClick: () -> Void
    public let onStoryPosted: () -> Void
    @ObservedObject public var mapViewModel: MapViewModel
    @ObservedObject public var storiesViewModel: StoriesViewModel

    @State private var selectedPhotosData: [Data] = []
    @State private var pickerItems: [PhotosPickerItem] = []
    @State private var text: String = ""
    @State private var location: String = ""
    @State private var selectedType: String = "user"
    @State private var selectedBusiness: Business? = nil
    @State private var isSubmitting: Bool = false
    @State private var errorMessage: String? = nil

    public init(onBackClick: @escaping () -> Void, onStoryPosted: @escaping () -> Void, mapViewModel: MapViewModel, storiesViewModel: StoriesViewModel) {
        self.onBackClick = onBackClick
        self.onStoryPosted = onStoryPosted
        self.mapViewModel = mapViewModel
        self.storiesViewModel = storiesViewModel
    }

    private let storyTypes: [(id: String, label: String)] = [
        ("user", "👤 User"),
        ("business", "🏢 Business"),
        ("community", "📢 Community")
    ]

    private var myBusinesses: [Business] {
        let uid = AuthManager.shared.currentUser?.uid ?? ""
        return mapViewModel.businesses.filter { $0.ownerId == uid }
    }

    private var canPost: Bool { !selectedPhotosData.isEmpty && !isSubmitting }

    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.white)
                }
                Text(strings.addStoryTitle)
                    .font(.title3).fontWeight(.bold).foregroundColor(.white)
                Spacer()
                Button(action: submit) {
                    Text(strings.postStory)
                        .fontWeight(.bold)
                        .foregroundColor(canPost ? .white : .white.opacity(0.5))
                }
                .disabled(!canPost)
            }
            .padding()
            .background(Color.meTontRed)

            ScrollView {
                VStack(spacing: 12) {
                    photosCard
                    storyTypeCard

                    if selectedType == "business" {
                        businessCard
                    }

                    captionCard
                    locationCard

                    Button(action: submit) {
                        HStack {
                            if isSubmitting {
                                ProgressView().tint(.white)
                            } else {
                                Image(systemName: "paperplane.fill")
                                Text(strings.postStory).fontWeight(.bold)
                            }
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(Color.meTontRed)
                        .foregroundColor(.white)
                        .cornerRadius(16)
                    }
                    .disabled(!canPost)
                    .padding(.horizontal, 16)

                    if let err = errorMessage {
                        Text(err)
                            .font(.caption)
                            .foregroundColor(.red)
                            .padding(.horizontal, 16)
                    }

                    Spacer().frame(height: 16)
                }
                .padding(.top, 16)
            }
        }
        .background(Color.meTontBackground)
    }

    // MARK: - Photos

    private var photosCard: some View {
        cardContainer {
            HStack {
                Text("Photos").font(.subheadline).fontWeight(.bold).foregroundColor(.black)
                Spacer()
                Text("\(selectedPhotosData.count)/10").font(.caption2).foregroundColor(.secondary)
            }

            if !selectedPhotosData.isEmpty {
                LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())], spacing: 4) {
                    ForEach(Array(selectedPhotosData.enumerated()), id: \.offset) { index, data in
                        ZStack(alignment: .topTrailing) {
                            if let uiImage = UIImage(data: data) {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(height: 64)
                                    .frame(maxWidth: .infinity)
                                    .clipped()
                                    .cornerRadius(8)
                            }
                            Button(action: { selectedPhotosData.remove(at: index) }) {
                                Image(systemName: "xmark")
                                    .font(.system(size: 9, weight: .bold))
                                    .foregroundColor(.white)
                                    .padding(4)
                                    .background(Circle().fill(Color.black.opacity(0.5)))
                            }
                            .padding(2)
                        }
                    }
                }
            }

            if selectedPhotosData.count < 10 {
                PhotosPicker(selection: $pickerItems, maxSelectionCount: 10 - selectedPhotosData.count, matching: .images) {
                    HStack {
                        Image(systemName: "photo.badge.plus")
                        Text(strings.addPhotosButton).fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                    .foregroundColor(.meTontRed)
                }
                .onChange(of: pickerItems) { _, newItems in
                    guard !newItems.isEmpty else { return }
                    Task {
                        var newData: [Data] = []
                        for item in newItems {
                            if let data = try? await item.loadTransferable(type: Data.self) {
                                newData.append(data)
                            }
                        }
                        await MainActor.run {
                            let remaining = 10 - selectedPhotosData.count
                            selectedPhotosData.append(contentsOf: newData.prefix(remaining))
                            pickerItems = []
                        }
                    }
                }
            }
        }
    }

    // MARK: - Story type

    private var storyTypeCard: some View {
        cardContainer {
            Text("Story Type").font(.subheadline).fontWeight(.bold).foregroundColor(.black)
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(storyTypes, id: \.id) { type in
                        Button(action: {
                            selectedType = type.id
                            if type.id != "business" { selectedBusiness = nil }
                        }) {
                            Text(type.label)
                                .font(.caption)
                                .padding(.horizontal, 12)
                                .padding(.vertical, 8)
                                .background(selectedType == type.id ? Color.meTontRed : Color.gray.opacity(0.1))
                                .foregroundColor(selectedType == type.id ? .white : .meTontBlack)
                                .cornerRadius(16)
                        }
                    }
                }
            }
        }
    }

    // MARK: - Business picker

    private var businessCard: some View {
        cardContainer {
            Text("Select Business").font(.subheadline).fontWeight(.bold).foregroundColor(.black)
            if myBusinesses.isEmpty {
                Text("You don't own any businesses yet")
                    .font(.subheadline)
                    .foregroundColor(.secondary)
            } else {
                Menu {
                    ForEach(myBusinesses) { business in
                        Button(action: { selectedBusiness = business }) {
                            Text("\(business.name) — \(BusinessCategory.displayName(for: business.category))")
                        }
                    }
                } label: {
                    HStack {
                        Text(selectedBusiness?.name ?? "Select a business")
                            .foregroundColor(selectedBusiness == nil ? .secondary : .meTontBlack)
                        Spacer()
                        Image(systemName: "chevron.down").foregroundColor(.secondary).font(.caption)
                    }
                    .padding(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.gray.opacity(0.3)))
                }
            }
        }
    }

    // MARK: - Caption & location

    private var captionCard: some View {
        cardContainer {
            Text("Caption").font(.subheadline).fontWeight(.bold).foregroundColor(.black)
            TextEditor(text: $text)
                .foregroundColor(.black)
                .scrollContentBackground(.hidden)
                .background(Color.white)
                .frame(height: 90)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.gray.opacity(0.3)))
        }
    }

    private var locationCard: some View {
        cardContainer {
            Text("Location").font(.subheadline).fontWeight(.bold).foregroundColor(.black)
            HStack(spacing: 8) {
                Image(systemName: "mappin.circle.fill").foregroundColor(.meTontRed)
                TextField(strings.storyLocationPlaceholder, text: $location)
            }
            .padding(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.gray.opacity(0.3)))
        }
    }

    private func cardContainer<Content: View>(@ViewBuilder content: () -> Content) -> some View {
        VStack(alignment: .leading, spacing: 10) {
            content()
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
        .padding(.horizontal, 16)
    }

    // MARK: - Submit

    private func submit() {
        guard canPost else { return }
        let user = AuthManager.shared.currentUser
        let userName = "\(user?.firstName ?? "User") \(user?.lastName ?? "")".trimmingCharacters(in: .whitespaces)
        let story = Story(
            userId: user?.uid ?? "",
            userName: userName,
            businessId: selectedBusiness?.id,
            businessName: selectedBusiness?.name,
            type: selectedType,
            category: selectedBusiness?.category ?? selectedType,
            location: location,
            text: text
        )
        isSubmitting = true
        errorMessage = nil
        Task {
            let ok = await storiesViewModel.postStory(story: story, photoData: selectedPhotosData)
            await MainActor.run {
                isSubmitting = false
                if ok {
                    onStoryPosted()
                } else {
                    errorMessage = "Failed to post story. Please try again."
                }
            }
        }
    }
}

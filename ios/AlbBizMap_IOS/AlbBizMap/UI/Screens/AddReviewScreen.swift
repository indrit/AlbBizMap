// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import PhotosUI

// Mirrors Android's AddReviewScreen.kt: a full-screen form (not the earlier
// small centered card) with a star-rating card, a comment card, and an
// optional up-to-5-photos card, matching Android's MAX_REVIEW_PHOTOS.
public struct AddReviewScreen: View {
    @Environment(\.appStrings) private var strings

    public let businessId: String
    public let onReviewSubmitted: () -> Void

    @StateObject private var viewModel = ReviewViewModel()
    @State private var selectedPhotosData: [Data] = []
    @State private var pickerItems: [PhotosPickerItem] = []

    private let maxReviewPhotos = 5

    public init(businessId: String, onReviewSubmitted: @escaping () -> Void) {
        self.businessId = businessId
        self.onReviewSubmitted = onReviewSubmitted
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            ScrollView {
                VStack(spacing: 12) {
                    ratingCard
                    commentCard
                    photosCard
                    submitButton

                    if let err = viewModel.errorMessage {
                        Text(err)
                            .font(.caption)
                            .foregroundColor(.red)
                            .padding(12)
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .background(Color.red.opacity(0.08))
                            .cornerRadius(12)
                            .padding(.horizontal, 16)
                    }

                    Spacer().frame(height: 32)
                }
                .padding(.top, 12)
            }
        }
        .background(Color.meTontBackground)
    }

    // MARK: - Top bar

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onReviewSubmitted) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.writeReview)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    // MARK: - Rating

    private var ratingCard: some View {
        VStack(spacing: 12) {
            Text(strings.rateThisBusiness)
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(.black)

            HStack(spacing: 8) {
                ForEach(1...5, id: \.self) { star in
                    Button(action: { viewModel.rating = star }) {
                        Image(systemName: star <= viewModel.rating ? "star.fill" : "star")
                            .font(.system(size: 30))
                            .foregroundColor(star <= viewModel.rating ? Color(red: 1, green: 0.757, blue: 0.027) : .meTontGrey)
                    }
                }
            }

            Text(ratingLabel)
                .font(.system(size: 13, weight: .medium))
                .foregroundColor(viewModel.rating > 0 ? .meTontRed : .meTontGrey)
                .padding(.horizontal, 16)
                .padding(.vertical, 8)
                .background(viewModel.rating > 0 ? Color.meTontRed.opacity(0.1) : Color(red: 0.96, green: 0.96, blue: 0.96))
                .cornerRadius(8)
        }
        .frame(maxWidth: .infinity)
        .padding(20)
        .background(Color.white)
        .cornerRadius(16)
        .padding(.horizontal, 16)
    }

    private var ratingLabel: String {
        switch viewModel.rating {
        case 0: return strings.tapStarToRate
        case 1: return "⭐ Poor"
        case 2: return "⭐⭐ Fair"
        case 3: return "⭐⭐⭐ Good"
        case 4: return "⭐⭐⭐⭐ Very Good"
        default: return "⭐⭐⭐⭐⭐ Excellent!"
        }
    }

    // MARK: - Comment

    private var commentCard: some View {
        SectionCard(title: strings.writeReviewLabel) {
            TextEditor(text: $viewModel.comment)
                .frame(minHeight: 140)
                .padding(8)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                .overlay(alignment: .topLeading) {
                    if viewModel.comment.isEmpty {
                        Text(strings.shareExperience)
                            .foregroundColor(.meTontGrey)
                            .padding(.horizontal, 13)
                            .padding(.vertical, 16)
                            .allowsHitTesting(false)
                    }
                }

            Text("\(viewModel.comment.count) characters")
                .font(.system(size: 11))
                .foregroundColor(.meTontGrey)
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Photos

    private var photosCard: some View {
        SectionCard(title: strings.photos) {
            Text("\(selectedPhotosData.count)/\(maxReviewPhotos)")
                .font(.system(size: 11))
                .foregroundColor(.meTontGrey)
                .frame(maxWidth: .infinity, alignment: .trailing)

            if !selectedPhotosData.isEmpty {
                LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 8), count: 4), spacing: 8) {
                    ForEach(Array(selectedPhotosData.enumerated()), id: \.offset) { index, data in
                        if let uiImage = UIImage(data: data) {
                            ZStack(alignment: .topTrailing) {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .aspectRatio(1, contentMode: .fill)
                                    .clipped()
                                    .cornerRadius(10)
                                Button(action: { selectedPhotosData.remove(at: index) }) {
                                    Image(systemName: "xmark")
                                        .font(.system(size: 10, weight: .bold))
                                        .foregroundColor(.white)
                                        .padding(4)
                                        .background(Circle().fill(Color.black.opacity(0.5)))
                                }
                                .padding(2)
                            }
                        }
                    }
                }
            }

            if selectedPhotosData.count < maxReviewPhotos {
                PhotosPicker(selection: $pickerItems, maxSelectionCount: maxReviewPhotos - selectedPhotosData.count, matching: .images) {
                    HStack {
                        Image(systemName: "camera.fill.badge.ellipsis")
                        Text(strings.addPhotosButton).fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                    .foregroundColor(.meTontRed)
                }
                .onChange(of: pickerItems) { _, newItems in
                    guard !newItems.isEmpty else { return }
                    let remaining = maxReviewPhotos - selectedPhotosData.count
                    if newItems.count > remaining {
                        viewModel.errorMessage = strings.maxPhotosPerReview
                    }
                    Task {
                        var newData: [Data] = []
                        for item in newItems.prefix(remaining) {
                            if let data = try? await item.loadTransferable(type: Data.self) {
                                newData.append(data)
                            }
                        }
                        await MainActor.run {
                            selectedPhotosData.append(contentsOf: newData)
                            pickerItems = []
                        }
                    }
                }
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Submit

    private var canSubmit: Bool {
        !viewModel.isSubmitting && viewModel.rating > 0 && !viewModel.comment.trimmingCharacters(in: .whitespaces).isEmpty
    }

    private var submitButton: some View {
        Button(action: {
            Task {
                let user = AuthManager.shared.currentUser
                let userName = "\(user?.firstName ?? "User") \(user?.lastName ?? "")".trimmingCharacters(in: .whitespaces)
                let success = await viewModel.submitReview(
                    businessId: businessId,
                    userId: user?.uid ?? "",
                    userName: userName,
                    photoData: selectedPhotosData,
                    strings: strings
                )
                if success { onReviewSubmitted() }
            }
        }) {
            HStack {
                if viewModel.isSubmitting {
                    ProgressView().tint(.white)
                    Text(strings.submitting)
                } else {
                    Image(systemName: "star.fill")
                    Text(strings.submitReview).fontWeight(.bold)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(Color.meTontRed)
            .foregroundColor(.white)
            .cornerRadius(12)
        }
        .disabled(!canSubmit)
        .padding(.horizontal, 16)
    }
}

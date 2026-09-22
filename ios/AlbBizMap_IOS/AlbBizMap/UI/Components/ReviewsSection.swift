// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Mirrors Android's "Recent Reviews" section in BusinessDetailsScreen.kt —
// a header, then a card per review (rating, comment, photos, like/reply
// actions, and an expandable reply thread), all backed by the already-real
// ReviewRepository.
public struct ReviewsSection: View {
    public let businessId: String
    public let currentUserId: String
    public let currentUserName: String
    public let onNavigateToAuth: (@escaping () -> Void) -> Void

    @Environment(\.appStrings) private var strings
    @ObservedObject private var reviewRepository = ReviewRepository.shared

    public init(
        businessId: String,
        currentUserId: String,
        currentUserName: String,
        onNavigateToAuth: @escaping (@escaping () -> Void) -> Void
    ) {
        self.businessId = businessId
        self.currentUserId = currentUserId
        self.currentUserName = currentUserName
        self.onNavigateToAuth = onNavigateToAuth
    }

    public var body: some View {
        let reviews = reviewRepository.getReviews(businessId: businessId)

        VStack(alignment: .leading, spacing: 12) {
            Text(strings.recentReviews)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.meTontBlack)

            if reviews.isEmpty {
                HStack {
                    Spacer()
                    Text(strings.noReviewsYet)
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Spacer()
                }
                .padding(32)
                .background(Color.white)
                .cornerRadius(16)
                .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
            } else {
                VStack(spacing: 12) {
                    ForEach(reviews) { review in
                        ReviewItemView(
                            review: review,
                            businessId: businessId,
                            currentUserId: currentUserId,
                            currentUserName: currentUserName,
                            onNavigateToAuth: onNavigateToAuth
                        )
                    }
                }
            }
        }
    }
}

// MARK: - One review card (header, comment, photos, like/reply, replies)

private struct ReviewItemView: View {
    let review: Review
    let businessId: String
    let currentUserId: String
    let currentUserName: String
    let onNavigateToAuth: (@escaping () -> Void) -> Void

    @Environment(\.appStrings) private var strings
    @ObservedObject private var reviewRepository = ReviewRepository.shared

    @State private var showReplies = false
    @State private var showReplyInput = false
    @State private var replyText = ""
    @State private var fullScreenPhotoIndex: Int? = nil

    @State private var showEditSheet = false
    @State private var editRating = 0
    @State private var editComment = ""
    @State private var isSubmittingEdit = false

    @State private var showDeleteConfirm = false
    @State private var isDeleting = false

    @State private var replyPendingEdit: Reply? = nil
    @State private var editReplyComment = ""
    @State private var isSubmittingReplyEdit = false

    @State private var replyPendingDelete: Reply? = nil
    @State private var isDeletingReply = false

    private var isLiked: Bool { !currentUserId.isEmpty && review.likedBy.contains(currentUserId) }
    private var isOwnReview: Bool { !currentUserId.isEmpty && currentUserId == review.userId }
    // Only reflects what's already cached — a live listener for this review's
    // replies is started (via onChange below) the first time showReplies flips
    // true, same lazy pattern as Android's LaunchedEffect(showReplies).
    private var cachedReplies: [Reply] { reviewRepository.repliesByReview[review.id] ?? [] }

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            // ── Reviewer header ──────────────────────────────────
            HStack(alignment: .center, spacing: 10) {
                Circle()
                    .fill(Color.meTontRed.opacity(0.1))
                    .frame(width: 36, height: 36)
                    .overlay(
                        Text(String((review.userName.first ?? "U")).uppercased())
                            .foregroundColor(.meTontRed)
                            .fontWeight(.bold)
                    )
                VStack(alignment: .leading, spacing: 2) {
                    Text(review.userName)
                        .fontWeight(.bold)
                        .foregroundColor(.meTontBlack)
                    Text(Self.dateFormatter.string(from: Date(timeIntervalSince1970: Double(review.createdAt) / 1000)))
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                Spacer()
                HStack(spacing: 1) {
                    ForEach(0..<max(0, review.rating), id: \.self) { _ in
                        Image(systemName: "star.fill")
                            .font(.system(size: 11))
                            .foregroundColor(Color(red: 1.0, green: 0.757, blue: 0.027))
                    }
                }
            }

            // ── Comment ───────────────────────────────────────────
            Text(review.comment)
                .font(.subheadline)
                .foregroundColor(.meTontBlack.opacity(0.8))
                .padding(.top, 8)

            // ── Photos (thumbnails, tap for full screen) ─────────
            if !review.photos.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 6) {
                        ForEach(review.photos.indices, id: \.self) { index in
                            reviewPhotoThumbnail(review.photos[index])
                                .onTapGesture { fullScreenPhotoIndex = index }
                        }
                    }
                }
                .padding(.top, 8)
            }

            // ── Action row (like, reply, view replies, edit/delete) ─
            HStack(spacing: 8) {
                Button(action: performLikeAction) {
                    Image(systemName: isLiked ? "hand.thumbsup.fill" : "hand.thumbsup")
                        .font(.system(size: 16))
                        .foregroundColor(isLiked ? .meTontRed : .secondary)
                }
                Text("\(review.likedBy.count)")
                    .font(.caption2)
                    .foregroundColor(.secondary)

                Button(action: performReplyToggleAction) {
                    HStack(spacing: 4) {
                        Image(systemName: "arrowshape.turn.up.left")
                            .font(.system(size: 13))
                        Text(strings.reply)
                            .font(.caption2)
                    }
                    .foregroundColor(.secondary)
                }

                if !cachedReplies.isEmpty || showReplies {
                    Button(action: { showReplies.toggle() }) {
                        Text(showReplies ? strings.hideReplies : "\(strings.viewReplies) (\(cachedReplies.count))")
                            .font(.caption2)
                            .foregroundColor(.meTontRed)
                    }
                }

                if isOwnReview {
                    Spacer()
                    Button(action: {
                        editRating = review.rating
                        editComment = review.comment
                        showEditSheet = true
                    }) {
                        Image(systemName: "pencil")
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                    }
                    Button(action: { showDeleteConfirm = true }) {
                        Image(systemName: "trash")
                            .font(.system(size: 14))
                            .foregroundColor(.secondary)
                    }
                }
            }
            .padding(.top, 8)

            // ── Reply input ───────────────────────────────────────
            if showReplyInput {
                HStack(spacing: 8) {
                    TextField(strings.writeReplyPlaceholder, text: $replyText)
                        .textFieldStyle(.roundedBorder)
                    Button(action: submitReply) {
                        Image(systemName: "paperplane.fill")
                            .foregroundColor(replyIsBlank ? .secondary : .meTontRed)
                    }
                    .disabled(replyIsBlank)
                }
                .padding(.top, 8)
            }

            // ── Replies list ──────────────────────────────────────
            if showReplies && !cachedReplies.isEmpty {
                Divider().padding(.vertical, 8)
                VStack(alignment: .leading, spacing: 10) {
                    ForEach(cachedReplies) { reply in
                        replyRow(reply)
                    }
                }
            }
        }
        .padding(14)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: .black.opacity(0.05), radius: 4, y: 2)
        .onChange(of: showReplies) { _, newValue in
            if newValue {
                _ = reviewRepository.getReplies(businessId: businessId, reviewId: review.id)
            }
        }
        .fullScreenCover(isPresented: Binding(
            get: { fullScreenPhotoIndex != nil },
            set: { if !$0 { fullScreenPhotoIndex = nil } }
        )) {
            FullScreenPhotoViewer(photos: review.photos, startIndex: fullScreenPhotoIndex ?? 0)
        }
        .sheet(isPresented: $showEditSheet) { editReviewSheet }
        .alert(strings.deleteReviewConfirmTitle, isPresented: $showDeleteConfirm) {
            Button(strings.cancel, role: .cancel) {}
            Button(strings.deleteReviewButton, role: .destructive, action: deleteReview)
        } message: {
            Text(strings.deleteReviewConfirmMessage)
        }
        .sheet(item: $replyPendingEdit) { reply in
            editReplySheet(reply)
        }
        .alert(
            strings.deleteReplyConfirmTitle,
            isPresented: Binding(
                get: { replyPendingDelete != nil },
                set: { if !$0 { replyPendingDelete = nil } }
            )
        ) {
            Button(strings.cancel, role: .cancel) { replyPendingDelete = nil }
            Button(strings.deleteReplyButton, role: .destructive) {
                if let reply = replyPendingDelete {
                    deleteReply(reply)
                }
            }
        } message: {
            Text(strings.deleteReplyConfirmMessage)
        }
    }

    private var replyIsBlank: Bool {
        replyText.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
    }

    private func reviewPhotoThumbnail(_ urlString: String) -> some View {
        Group {
            if let url = URL(string: urlString) {
                AsyncImage(url: url) { img in
                    img.resizable().aspectRatio(contentMode: .fill)
                } placeholder: {
                    Color.meTontRed.opacity(0.1)
                }
            } else {
                Color.meTontRed.opacity(0.1)
            }
        }
        .frame(width: 64, height: 64)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .clipped()
    }

    private func replyRow(_ reply: Reply) -> some View {
        let isReplyLiked = !currentUserId.isEmpty && reply.likedBy.contains(currentUserId)
        let isOwnReply = !currentUserId.isEmpty && currentUserId == reply.userId

        return HStack(alignment: .top, spacing: 8) {
            Circle()
                .fill(Color.gray.opacity(0.15))
                .frame(width: 28, height: 28)
                .overlay(
                    Text(String((reply.userName.first ?? "U")).uppercased())
                        .font(.system(size: 11))
                        .fontWeight(.bold)
                        .foregroundColor(.secondary)
                )
            VStack(alignment: .leading, spacing: 2) {
                HStack(spacing: 6) {
                    Text(reply.userName)
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundColor(.meTontBlack)
                    Text(Self.shortDateFormatter.string(from: Date(timeIntervalSince1970: Double(reply.createdAt) / 1000)))
                        .font(.caption2)
                        .foregroundColor(.secondary)
                }
                Text(reply.comment)
                    .font(.subheadline)
                    .foregroundColor(.meTontBlack.opacity(0.8))

                HStack(spacing: 4) {
                    Button(action: { performReplyLikeAction(reply) }) {
                        Image(systemName: isReplyLiked ? "hand.thumbsup.fill" : "hand.thumbsup")
                            .font(.system(size: 13))
                            .foregroundColor(isReplyLiked ? .meTontRed : .secondary)
                    }
                    Text("\(reply.likedBy.count)")
                        .font(.caption2)
                        .foregroundColor(.secondary)

                    if isOwnReply {
                        Button(action: {
                            replyPendingEdit = reply
                            editReplyComment = reply.comment
                        }) {
                            Image(systemName: "pencil")
                                .font(.system(size: 12))
                                .foregroundColor(.secondary)
                        }
                        .padding(.leading, 4)
                        Button(action: { replyPendingDelete = reply }) {
                            Image(systemName: "trash")
                                .font(.system(size: 12))
                                .foregroundColor(.secondary)
                        }
                    }
                }
            }
        }
        .padding(.leading, 16)
    }

    // ── Edit review sheet ────────────────────────────────────────
    private var editReviewSheet: some View {
        NavigationView {
            VStack(alignment: .leading, spacing: 16) {
                HStack(spacing: 4) {
                    ForEach(1...5, id: \.self) { star in
                        Image(systemName: star <= editRating ? "star.fill" : "star")
                            .foregroundColor(Color(red: 1.0, green: 0.757, blue: 0.027))
                            .font(.title2)
                            .onTapGesture { editRating = star }
                    }
                }
                TextEditor(text: $editComment)
                    .foregroundColor(.meTontBlack)
                    .scrollContentBackground(.hidden)
                    .background(Color.white)
                    .frame(minHeight: 120)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.gray.opacity(0.3)))
                Spacer()
            }
            .padding(16)
            .navigationTitle(strings.editReviewTitle)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(strings.cancel) { showEditSheet = false }
                        .disabled(isSubmittingEdit)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(strings.save) { submitEditReview() }
                        .disabled(isSubmittingEdit || editComment.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty || editRating == 0)
                }
            }
        }
    }

    // ── Edit reply sheet ─────────────────────────────────────────
    private func editReplySheet(_ reply: Reply) -> some View {
        NavigationView {
            VStack(alignment: .leading, spacing: 16) {
                TextEditor(text: $editReplyComment)
                    .foregroundColor(.meTontBlack)
                    .scrollContentBackground(.hidden)
                    .background(Color.white)
                    .frame(minHeight: 100)
                    .overlay(RoundedRectangle(cornerRadius: 8).stroke(Color.gray.opacity(0.3)))
                Spacer()
            }
            .padding(16)
            .navigationTitle(strings.editReplyTitle)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button(strings.cancel) { replyPendingEdit = nil }
                        .disabled(isSubmittingReplyEdit)
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(strings.save) { submitEditReply(reply) }
                        .disabled(isSubmittingReplyEdit || editReplyComment.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty)
                }
            }
        }
    }

    // ── Actions ───────────────────────────────────────────────────

    private func performLikeAction() {
        let action: () -> Void = {
            guard !currentUserId.isEmpty else { return }
            Task { _ = await ReviewRepository.shared.toggleLike(businessId: businessId, reviewId: review.id, userId: currentUserId) }
        }
        if AuthManager.shared.isLoggedIn {
            action()
        } else {
            onNavigateToAuth(action)
        }
    }

    private func performReplyToggleAction() {
        let action: () -> Void = { showReplyInput.toggle() }
        if AuthManager.shared.isLoggedIn {
            action()
        } else {
            onNavigateToAuth(action)
        }
    }

    private func performReplyLikeAction(_ reply: Reply) {
        let action: () -> Void = {
            guard !currentUserId.isEmpty else { return }
            Task { _ = await ReviewRepository.shared.toggleReplyLike(businessId: businessId, reviewId: review.id, replyId: reply.id, userId: currentUserId) }
        }
        if AuthManager.shared.isLoggedIn {
            action()
        } else {
            onNavigateToAuth(action)
        }
    }

    private func submitReply() {
        guard !replyIsBlank, !currentUserId.isEmpty else { return }
        let comment = replyText.trimmingCharacters(in: .whitespacesAndNewlines)
        let reply = Reply(reviewId: review.id, userId: currentUserId, userName: currentUserName, comment: comment)
        Task {
            let result = await ReviewRepository.shared.addReply(businessId: businessId, reviewId: review.id, reply: reply)
            if case .success = result {
                await MainActor.run {
                    replyText = ""
                    showReplyInput = false
                    showReplies = true
                }
            }
        }
    }

    private func deleteReview() {
        guard !isDeleting else { return }
        isDeleting = true
        Task {
            _ = await ReviewRepository.shared.deleteReview(businessId: businessId, reviewId: review.id)
            await MainActor.run { isDeleting = false }
        }
    }

    private func submitEditReview() {
        guard !isSubmittingEdit else { return }
        isSubmittingEdit = true
        let rating = editRating
        let comment = editComment.trimmingCharacters(in: .whitespacesAndNewlines)
        Task {
            _ = await ReviewRepository.shared.updateReview(businessId: businessId, reviewId: review.id, rating: rating, comment: comment)
            await MainActor.run {
                isSubmittingEdit = false
                showEditSheet = false
            }
        }
    }

    private func submitEditReply(_ reply: Reply) {
        guard !isSubmittingReplyEdit else { return }
        isSubmittingReplyEdit = true
        let comment = editReplyComment.trimmingCharacters(in: .whitespacesAndNewlines)
        Task {
            _ = await ReviewRepository.shared.updateReply(businessId: businessId, reviewId: review.id, replyId: reply.id, comment: comment)
            await MainActor.run {
                isSubmittingReplyEdit = false
                replyPendingEdit = nil
            }
        }
    }

    private func deleteReply(_ reply: Reply) {
        guard !isDeletingReply else { return }
        isDeletingReply = true
        Task {
            _ = await ReviewRepository.shared.deleteReply(businessId: businessId, reviewId: review.id, replyId: reply.id)
            await MainActor.run {
                isDeletingReply = false
                replyPendingDelete = nil
            }
        }
    }

    private static let dateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "MMM dd, yyyy"
        return f
    }()

    private static let shortDateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "MMM dd"
        return f
    }()
}

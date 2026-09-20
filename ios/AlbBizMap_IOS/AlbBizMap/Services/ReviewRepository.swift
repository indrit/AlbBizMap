// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import FirebaseFirestore
import FirebaseStorage

public class ReviewRepository: ObservableObject {
    public static let shared = ReviewRepository()

    @Published public var reviewsByBusiness: [String: [Review]] = [:]
    @Published public var repliesByReview: [String: [Reply]] = [:]

    private let db = Firestore.firestore()
    private let storage = Storage.storage()
    private let reportThreshold = 3 // auto-hide after 3 reports, mirrors Android

    private var reviewListeners: [String: ListenerRegistration] = [:]
    private var replyListeners: [String: ListenerRegistration] = [:]

    private init() {}

    private func reviewsRef(_ businessId: String) -> CollectionReference {
        db.collection("businesses").document(businessId).collection("reviews")
    }

    private func repliesRef(_ businessId: String, _ reviewId: String) -> CollectionReference {
        reviewsRef(businessId).document(reviewId).collection("replies")
    }

    // Starts (once per business) a live Firestore listener and returns the
    // currently cached, visible (not yet hidden by reports), newest-first
    // list — mirrors Android's getReviews Flow, adapted to this app's
    // existing @Published-dictionary + synchronous-read call shape.
    public func getReviews(businessId: String) -> [Review] {
        startListeningReviews(businessId: businessId)
        let list = reviewsByBusiness[businessId] ?? []
        return list.filter { $0.reportCount < reportThreshold }.sorted(by: { $0.createdAt > $1.createdAt })
    }

    private func startListeningReviews(businessId: String) {
        guard reviewListeners[businessId] == nil else { return }
        let listener = reviewsRef(businessId).addSnapshotListener { [weak self] snapshot, error in
            guard let self else { return }
            if let error {
                print("ReviewRepository: error listening for reviews: \(error)")
                return
            }
            let reviews: [Review] = snapshot?.documents.compactMap { doc in
                Review.fromMap(id: doc.documentID, map: doc.data())
            } ?? []
            DispatchQueue.main.async {
                self.reviewsByBusiness[businessId] = reviews
            }
        }
        reviewListeners[businessId] = listener
    }

    // Same upload-then-write shape as StoriesRepository.addStory — the doc ID
    // is generated up front so uploaded photos live under a path keyed by
    // the review's real ID rather than a temp one.
    public func addReview(businessId: String, review: Review, photoData: [Data] = []) async -> Result<String, Error> {
        let ref = reviewsRef(businessId).document()
        do {
            var photoUrls: [String] = []
            for (index, data) in photoData.enumerated() {
                let filename = "reviews/\(businessId)/\(ref.documentID)/photo_\(index).jpg"
                let storageRef = storage.reference().child(filename)
                let metadata = StorageMetadata()
                metadata.contentType = "image/jpeg"
                _ = try await storageRef.putDataAsync(data, metadata: metadata)
                let url = try await storageRef.downloadURL()
                photoUrls.append(url.absoluteString)
            }
            var finalReview = review
            finalReview.id = ref.documentID
            finalReview.photos = photoUrls
            try await ref.setData(finalReview.toMap().compactMapValues { $0 })
            await updateBusinessStats(businessId: businessId)
            return .success(ref.documentID)
        } catch {
            return .failure(error)
        }
    }

    // Firestore rules already let a review's own author update any field on
    // it, matching Android — no separate ownership check needed here.
    public func updateReview(businessId: String, reviewId: String, rating: Int, comment: String) async -> Result<Void, Error> {
        do {
            try await reviewsRef(businessId).document(reviewId).updateData([
                "rating": rating,
                "comment": comment
            ])
            await updateBusinessStats(businessId: businessId) // rating changed, average needs recomputing
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    public func deleteReview(businessId: String, reviewId: String) async -> Result<Void, Error> {
        let reviewRef = reviewsRef(businessId).document(reviewId)
        do {
            let snapshot = try await reviewRef.getDocument()
            let review = snapshot.data().map { Review.fromMap(id: reviewId, map: $0) }

            try await reviewRef.delete()

            // Best-effort cleanup of the review's own uploaded photos, same
            // non-fatal pattern as EventsRepository.deleteEvent below —
            // replies other users left under this review are intentionally
            // NOT cascade-deleted, matching Android.
            if let photos = review?.photos {
                for url in photos {
                    do {
                        try await storage.reference(forURL: url).delete()
                    } catch {
                        print("ReviewRepository: error deleting review photo: \(error)")
                    }
                }
            }

            await updateBusinessStats(businessId: businessId)
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    // The read (checking reportedBy) and the write (increment + arrayUnion)
    // happen atomically in a transaction — otherwise two near-simultaneous
    // reports could both read "not yet reported" before either write lands,
    // double-counting a single user's report.
    public func reportReview(businessId: String, reviewId: String, userId: String) async -> Result<Void, Error> {
        let reviewRef = reviewsRef(businessId).document(reviewId)
        do {
            _ = try await db.runTransaction { transaction, errorPointer in
                let snapshot: DocumentSnapshot
                do {
                    snapshot = try transaction.getDocument(reviewRef)
                } catch let fetchError as NSError {
                    errorPointer?.pointee = fetchError
                    return nil
                }
                guard let data = snapshot.data() else {
                    errorPointer?.pointee = NSError(domain: "ReviewRepository", code: -1, userInfo: [NSLocalizedDescriptionKey: "Review not found"])
                    return nil
                }
                let review = Review.fromMap(id: reviewId, map: data)
                if review.reportedBy.contains(userId) {
                    errorPointer?.pointee = NSError(domain: "ReviewRepository", code: -2, userInfo: [NSLocalizedDescriptionKey: "Already reported"])
                    return nil
                }
                transaction.updateData([
                    "reportCount": FieldValue.increment(Int64(1)),
                    "reportedBy": FieldValue.arrayUnion([userId])
                ], forDocument: reviewRef)
                return nil
            }
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    // Same TOCTOU concern as reportReview — the transaction makes the
    // read-then-decide-then-write atomic against a rapid double-tap.
    public func toggleLike(businessId: String, reviewId: String, userId: String) async -> Result<Void, Error> {
        let reviewRef = reviewsRef(businessId).document(reviewId)
        do {
            _ = try await db.runTransaction { transaction, errorPointer in
                let snapshot: DocumentSnapshot
                do {
                    snapshot = try transaction.getDocument(reviewRef)
                } catch let fetchError as NSError {
                    errorPointer?.pointee = fetchError
                    return nil
                }
                guard let data = snapshot.data() else {
                    errorPointer?.pointee = NSError(domain: "ReviewRepository", code: -1, userInfo: [NSLocalizedDescriptionKey: "Review not found"])
                    return nil
                }
                let review = Review.fromMap(id: reviewId, map: data)
                if review.likedBy.contains(userId) {
                    transaction.updateData(["likedBy": FieldValue.arrayRemove([userId])], forDocument: reviewRef)
                } else {
                    transaction.updateData(["likedBy": FieldValue.arrayUnion([userId])], forDocument: reviewRef)
                }
                return nil
            }
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    private func updateBusinessStats(businessId: String) async {
        do {
            let snapshot = try await reviewsRef(businessId).getDocuments()
            let reviews = snapshot.documents.compactMap { doc in
                Review.fromMap(id: doc.documentID, map: doc.data())
            }.filter { $0.reportCount < reportThreshold } // only count visible reviews

            let avgRating = reviews.isEmpty
                ? 0.0
                : Double(reviews.reduce(0) { $0 + $1.rating }) / Double(reviews.count)

            try await db.collection("businesses").document(businessId).updateData([
                "rating": avgRating,
                "reviewCount": reviews.count
            ])
        } catch {
            print("ReviewRepository: error updating business stats: \(error)")
        }
    }

    public func getReplies(businessId: String, reviewId: String) -> [Reply] {
        startListeningReplies(businessId: businessId, reviewId: reviewId)
        return repliesByReview[reviewId] ?? []
    }

    private func startListeningReplies(businessId: String, reviewId: String) {
        guard replyListeners[reviewId] == nil else { return }
        let listener = repliesRef(businessId, reviewId).addSnapshotListener { [weak self] snapshot, error in
            guard let self else { return }
            if let error {
                print("ReviewRepository: error listening for replies: \(error)")
                return
            }
            let replies: [Reply] = (snapshot?.documents.compactMap { doc in
                Reply.fromMap(id: doc.documentID, map: doc.data())
            } ?? []).sorted(by: { $0.createdAt < $1.createdAt })
            DispatchQueue.main.async {
                self.repliesByReview[reviewId] = replies
            }
        }
        replyListeners[reviewId] = listener
    }

    public func addReply(businessId: String, reviewId: String, reply: Reply) async -> Result<String, Error> {
        let ref = repliesRef(businessId, reviewId).document()
        do {
            var finalReply = reply
            finalReply.id = ref.documentID
            try await ref.setData(finalReply.toMap().compactMapValues { $0 })
            return .success(ref.documentID)
        } catch {
            return .failure(error)
        }
    }

    // Author-only edit, enforced by Firestore rules.
    public func updateReply(businessId: String, reviewId: String, replyId: String, comment: String) async -> Result<Void, Error> {
        do {
            try await repliesRef(businessId, reviewId).document(replyId).updateData(["comment": comment])
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    // Author-only delete, enforced by Firestore rules. No photos or business
    // stats involved here, unlike deleteReview — replies don't carry either.
    public func deleteReply(businessId: String, reviewId: String, replyId: String) async -> Result<Void, Error> {
        do {
            try await repliesRef(businessId, reviewId).document(replyId).delete()
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    public func toggleReplyLike(businessId: String, reviewId: String, replyId: String, userId: String) async -> Result<Void, Error> {
        let replyRef = repliesRef(businessId, reviewId).document(replyId)
        do {
            _ = try await db.runTransaction { transaction, errorPointer in
                let snapshot: DocumentSnapshot
                do {
                    snapshot = try transaction.getDocument(replyRef)
                } catch let fetchError as NSError {
                    errorPointer?.pointee = fetchError
                    return nil
                }
                guard let data = snapshot.data() else {
                    errorPointer?.pointee = NSError(domain: "ReviewRepository", code: -1, userInfo: [NSLocalizedDescriptionKey: "Reply not found"])
                    return nil
                }
                let reply = Reply.fromMap(id: replyId, map: data)
                if reply.likedBy.contains(userId) {
                    transaction.updateData(["likedBy": FieldValue.arrayRemove([userId])], forDocument: replyRef)
                } else {
                    transaction.updateData(["likedBy": FieldValue.arrayUnion([userId])], forDocument: replyRef)
                }
                return nil
            }
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}

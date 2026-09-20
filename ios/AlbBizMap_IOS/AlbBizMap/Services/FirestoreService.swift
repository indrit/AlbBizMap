// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import FirebaseFirestore
import FirebaseStorage
import FirebaseAuth

public class FirestoreService: ObservableObject {
    public static let shared = FirestoreService()

    @Published public var businesses: [Business] = []
    @Published public var claimRequests: [ClaimRequest] = []
    @Published public var favoriteIds: Set<String> = []

    private let db = Firestore.firestore()
    private lazy var businessesRef = db.collection("businesses")
    private lazy var usersRef = db.collection("users")
    private lazy var claimRequestsRef = db.collection("claim_requests")
    private let storage = Storage.storage()

    private var businessesListener: ListenerRegistration?
    private var claimsListener: ListenerRegistration?

    private init() {
        startListening()
    }

    private func startListening() {
        businessesListener = businessesRef
            .whereField("isActive", isEqualTo: true)
            .addSnapshotListener { [weak self] snapshot, error in
                if let error = error {
                    print("Firestore: Error listening for businesses: \(error)")
                    return
                }
                guard let snapshot = snapshot else { return }
                let parsed = snapshot.documents.compactMap { doc -> Business? in
                    Business.fromMap(id: doc.documentID, map: doc.data())
                }
                // Same precedence as Android's getActiveBusinesses: sponsored first, then rating.
                let sorted = parsed.sorted { a, b in
                    if a.isSponsored != b.isSponsored { return a.isSponsored && !b.isSponsored }
                    return a.rating > b.rating
                }
                DispatchQueue.main.async { self?.businesses = sorted }
            }

        claimsListener = claimRequestsRef
            .whereField("status", isEqualTo: "pending")
            .addSnapshotListener { [weak self] snapshot, error in
                if let error = error {
                    print("Firestore: Error listening for claim requests: \(error)")
                    return
                }
                guard let snapshot = snapshot else { return }
                let parsed = snapshot.documents.compactMap { doc -> ClaimRequest? in
                    ClaimRequest.fromMap(id: doc.documentID, map: doc.data())
                }
                DispatchQueue.main.async { self?.claimRequests = parsed }
            }
    }

    // MARK: - Businesses

    public func addBusiness(_ business: Business) async -> Result<String, Error> {
        // Mirrors Android's FirestoreService.addBusiness: requires a real signed-in
        // user (Add Business is gated behind login at the UI level already), and the
        // resulting document is always owned by that user regardless of whatever
        // ownerId the caller passed in.
        guard let currentUser = Auth.auth().currentUser else {
            // FirestoreService is a plain singleton with no SwiftUI environment access,
            // so (unlike view-level strings) this technical fallback message is not
            // localized — Add Business is already gated behind login at the UI level,
            // so this path should only ever be hit defensively.
            return .failure(NSError(domain: "FirestoreService", code: -1, userInfo: [
                NSLocalizedDescriptionKey: "You must be logged in to add a business"
            ]))
        }
        let docRef = business.id.isEmpty ? businessesRef.document() : businessesRef.document(business.id)
        var finalBusiness = business
        finalBusiness.id = docRef.documentID
        finalBusiness.ownerId = currentUser.uid
        do {
            try await docRef.setData(finalBusiness.toMap().compactMapValues { $0 })
            return .success(docRef.documentID)
        } catch {
            return .failure(error)
        }
    }

    public func updateBusiness(_ business: Business) async -> Result<String, Error> {
        do {
            try await businessesRef.document(business.id).setData(business.toMap().compactMapValues { $0 })
            return .success(business.id)
        } catch {
            return .failure(error)
        }
    }

    public func updateBusinessPhotos(businessId: String, photos: [String]) async -> Result<Void, Error> {
        do {
            try await businessesRef.document(businessId).updateData(["photos": photos])
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    // MARK: - Storage (photo upload)

    // Mirrors Android's FirestoreService.uploadImage: same storage path shape
    // (businesses/{businessId}/image_{index}.jpg) so photos stay compatible across
    // both platforms' buckets.
    public func uploadImage(businessId: String, data: Data, index: Int) async throws -> String {
        let filename = "businesses/\(businessId)/image_\(index).jpg"
        let ref = storage.reference().child(filename)
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"
        _ = try await ref.putDataAsync(data, metadata: metadata)
        let url = try await ref.downloadURL()
        return url.absoluteString
    }

    public func testConnection() async -> Bool {
        do {
            _ = try await businessesRef.limit(to: 1).getDocuments()
            return true
        } catch {
            return false
        }
    }

    // MARK: - Favorites
    // Mirrors Android's BusinessRepository (not FirestoreService's own dead
    // subcollection variant): a single "favorites" array field on users/{uid},
    // updated with arrayUnion/arrayRemove and falling back to creating the user
    // document if it doesn't exist yet.

    public func toggleFavorite(userId: String, businessId: String) {
        guard !userId.isEmpty else { return }
        let willFavorite = !favoriteIds.contains(businessId)
        // Optimistic local update so the heart icon responds instantly.
        if willFavorite {
            favoriteIds.insert(businessId)
        } else {
            favoriteIds.remove(businessId)
        }
        let userRef = usersRef.document(userId)
        Task {
            do {
                if willFavorite {
                    try await userRef.updateData(["favorites": FieldValue.arrayUnion([businessId])])
                } else {
                    try await userRef.updateData(["favorites": FieldValue.arrayRemove([businessId])])
                }
            } catch {
                do {
                    try await userRef.setData([
                        "favorites": willFavorite ? [businessId] : [],
                        "isAdmin": false
                    ], merge: true)
                } catch {
                    print("Firestore: Error toggling favorite: \(error)")
                }
            }
        }
    }

    // One-shot fetch (matches Android's suspend getFavoriteIds — not a live
    // listener), called when a user logs in so their existing favorites show up.
    public func loadFavorites(userId: String) async {
        guard !userId.isEmpty else {
            await MainActor.run { self.favoriteIds = [] }
            return
        }
        do {
            let doc = try await usersRef.document(userId).getDocument()
            let favorites = (doc.data()?["favorites"] as? [String]) ?? []
            await MainActor.run { self.favoriteIds = Set(favorites) }
        } catch {
            print("Firestore: Error loading favorites: \(error)")
        }
    }

    // MARK: - Likes

    public func toggleBusinessLike(userId: String, businessId: String) {
        let businessRef = businessesRef.document(businessId)
        Task {
            do {
                _ = try await db.runTransaction { transaction, errorPointer in
                    let snapshot: DocumentSnapshot
                    do {
                        snapshot = try transaction.getDocument(businessRef)
                    } catch let fetchError as NSError {
                        errorPointer?.pointee = fetchError
                        return nil
                    }
                    let likedBy = (snapshot.get("likedBy") as? [String]) ?? []
                    if likedBy.contains(userId) {
                        transaction.updateData([
                            "likedBy": FieldValue.arrayRemove([userId]),
                            "likeCount": FieldValue.increment(Int64(-1))
                        ], forDocument: businessRef)
                    } else {
                        transaction.updateData([
                            "likedBy": FieldValue.arrayUnion([userId]),
                            "likeCount": FieldValue.increment(Int64(1))
                        ], forDocument: businessRef)
                    }
                    return nil
                }
            } catch {
                print("Firestore: Error toggling like: \(error)")
            }
        }
    }

    // MARK: - Claims / Admin

    public func submitClaimRequest(_ claim: ClaimRequest) async -> Result<String, Error> {
        let ref = claimRequestsRef.document()
        var finalClaim = claim
        finalClaim.id = ref.documentID
        do {
            try await ref.setData(finalClaim.toMap().compactMapValues { $0 })
            return .success(ref.documentID)
        } catch {
            return .failure(error)
        }
    }

    public func approveClaim(_ claim: ClaimRequest) async -> Result<Void, Error> {
        do {
            try await businessesRef.document(claim.businessId).updateData([
                "ownerId": claim.userId,
                "isVerified": true
            ])
            try await claimRequestsRef.document(claim.id).updateData(["status": "approved"])
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    public func rejectClaim(claimId: String) async -> Result<Void, Error> {
        do {
            try await claimRequestsRef.document(claimId).updateData(["status": "rejected"])
            return .success(())
        } catch {
            return .failure(error)
        }
    }

    public func isUserAdmin(userId: String) async -> Bool {
        guard !userId.isEmpty else { return false }
        do {
            let doc = try await usersRef.document(userId).getDocument()
            return doc.data()?["isAdmin"] as? Bool ?? false
        } catch {
            return false
        }
    }
}

// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import FirebaseFirestore
import FirebaseStorage

public class StoriesRepository: ObservableObject {
    public static let shared = StoriesRepository()

    @Published public var stories: [Story] = []

    private let db = Firestore.firestore()
    private lazy var storiesRef = db.collection("stories")
    private let storage = Storage.storage()

    private var storiesListener: ListenerRegistration?

    private init() {
        startListening()
    }

    // Mirrors Android's getActiveStories: only stories that haven't expired
    // yet, newest expiry first.
    private func startListening() {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        storiesListener = storiesRef
            .whereField("expiresAt", isGreaterThan: now)
            .order(by: "expiresAt", descending: true)
            .addSnapshotListener { [weak self] snapshot, error in
                guard let self else { return }
                if let error {
                    print("StoriesRepository: error listening for stories: \(error)")
                    return
                }
                let stories: [Story] = snapshot?.documents.compactMap { doc in
                    Story.fromMap(id: doc.documentID, map: doc.data())
                } ?? []
                DispatchQueue.main.async {
                    self.stories = stories
                }
            }
    }

    // Mirrors Android's addStory(story, photoUris): uploads local photo data
    // to Storage first, then writes the story with those URLs.
    public func addStory(_ story: Story, photoData: [Data] = []) async -> Result<String, Error> {
        let docRef = storiesRef.document()
        do {
            var photoUrls: [String] = []
            for (index, data) in photoData.enumerated() {
                let filename = "stories/\(docRef.documentID)/photo_\(index).jpg"
                let ref = storage.reference().child(filename)
                let metadata = StorageMetadata()
                metadata.contentType = "image/jpeg"
                _ = try await ref.putDataAsync(data, metadata: metadata)
                let url = try await ref.downloadURL()
                photoUrls.append(url.absoluteString)
            }
            var finalStory = story
            finalStory.id = docRef.documentID
            finalStory.photos = photoUrls
            try await docRef.setData(finalStory.toMap().compactMapValues { $0 })
            return .success(docRef.documentID)
        } catch {
            return .failure(error)
        }
    }

    // addStory() above expects raw photo data it still needs to upload — for
    // the auto-generated "Just opened" story created right after a business
    // is registered, the photos are the business's own photos, already
    // uploaded to Storage as URLs at that point. Mirrors Android's
    // addStoryWithHostedPhotos, which skips the upload step entirely so the
    // story keeps the photos it was already given instead of them being
    // overwritten by an empty upload result.
    public func addStoryWithHostedPhotos(_ story: Story) async -> Result<String, Error> {
        let docRef = storiesRef.document()
        do {
            var finalStory = story
            finalStory.id = docRef.documentID
            try await docRef.setData(finalStory.toMap().compactMapValues { $0 })
            return .success(docRef.documentID)
        } catch {
            return .failure(error)
        }
    }

    public func markStoryViewed(storyId: String, userId: String) {
        Task {
            do {
                try await storiesRef.document(storyId).updateData([
                    "viewedBy": FieldValue.arrayUnion([userId])
                ])
            } catch {
                print("StoriesRepository: error marking story viewed: \(error)")
            }
        }
    }

    public func deleteStory(storyId: String) {
        Task {
            do {
                try await storiesRef.document(storyId).delete()
            } catch {
                print("StoriesRepository: error deleting story: \(error)")
            }
        }
    }

    // Only ever deletes the current user's own expired stories — matches
    // Android's scoping, since Firestore security rules block deleting
    // someone else's document and a compound query keeps every deletion
    // this loop attempts one it's actually allowed to make.
    public func deleteExpiredStories(userId: String) async {
        guard !userId.isEmpty else { return }
        do {
            let now = Int64(Date().timeIntervalSince1970 * 1000)
            let snapshot = try await storiesRef
                .whereField("userId", isEqualTo: userId)
                .whereField("expiresAt", isLessThan: now)
                .getDocuments()
            for doc in snapshot.documents {
                try await doc.reference.delete()
            }
        } catch {
            print("StoriesRepository: error deleting expired stories: \(error)")
        }
    }
}

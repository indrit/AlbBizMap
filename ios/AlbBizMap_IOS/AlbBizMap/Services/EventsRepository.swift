// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import FirebaseFirestore
import FirebaseStorage

public class EventsRepository: ObservableObject {
    public static let shared = EventsRepository()

    @Published public var events: [Event] = []

    private let db = Firestore.firestore()
    private lazy var eventsRef = db.collection("events")
    private let storage = Storage.storage()

    private var eventsListener: ListenerRegistration?

    private init() {
        startListening()
    }

    private func startListening() {
        eventsListener = eventsRef
            .order(by: "date")
            .addSnapshotListener { [weak self] snapshot, error in
                guard let self else { return }
                if let error {
                    print("EventsRepository: error listening for events: \(error)")
                    return
                }
                let events: [Event] = snapshot?.documents.compactMap { doc in
                    Event.fromMap(id: doc.documentID, map: doc.data())
                } ?? []
                DispatchQueue.main.async {
                    self.events = events
                }
            }
    }

    // Uploads the event photo (if any) first, then writes the event with its
    // URL already set — mirrors Android's EventsRepository.addEvent.
    public func addEvent(_ event: Event, photoData: Data? = nil) async -> Result<String, Error> {
        let docRef = eventsRef.document()
        do {
            var imageUrl: String? = nil
            if let photoData {
                let filename = "events/\(docRef.documentID)/image.jpg"
                let ref = storage.reference().child(filename)
                let metadata = StorageMetadata()
                metadata.contentType = "image/jpeg"
                _ = try await ref.putDataAsync(photoData, metadata: metadata)
                imageUrl = try await ref.downloadURL().absoluteString
            }
            var finalEvent = event
            finalEvent.id = docRef.documentID
            finalEvent.imageUrl = imageUrl
            try await docRef.setData(finalEvent.toMap().compactMapValues { $0 })
            return .success(docRef.documentID)
        } catch {
            return .failure(error)
        }
    }

    // Events a given user submitted — powers "My Events" on the Profile
    // screen, mirroring FirestoreService's getBusinessesByOwner pattern.
    public func getEventsByOrganizer(organizerId: String) -> [Event] {
        events.filter { $0.organizerId == organizerId }
    }

    // Deletes the Firestore doc and, if the event had a photo, its Storage
    // file too. A Storage delete failure is logged but doesn't block the
    // Firestore delete from succeeding — an orphaned image is a much
    // smaller problem than a stuck event the owner can no longer remove.
    public func deleteEvent(_ event: Event) async -> Result<Void, Error> {
        do {
            if let imageUrl = event.imageUrl, !imageUrl.isEmpty {
                do {
                    try await storage.reference(forURL: imageUrl).delete()
                } catch {
                    print("EventsRepository: error deleting event image: \(error)")
                }
            }
            try await eventsRef.document(event.id).delete()
            return .success(())
        } catch {
            return .failure(error)
        }
    }
}

// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class EventsRepository: ObservableObject {
    public static let shared = EventsRepository()
    
    @Published public var events: [Event] = []
    
    public init() {
        self.events = FirestoreService.shared.events
    }
    
    public func addEvent(_ event: Event) async -> Result<String, Error> {
        var newEvt = event
        if newEvt.id.isEmpty {
            newEvt.id = "evt_" + UUID().uuidString.prefix(8)
        }
        await MainActor.run {
            self.events.append(newEvt)
            FirestoreService.shared.events.append(newEvt)
        }
        return .success(newEvt.id)
    }
}

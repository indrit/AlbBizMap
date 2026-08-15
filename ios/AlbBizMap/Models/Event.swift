// Bismillah Hir Rahman Nir Raheem
import Foundation

public struct Event: Identifiable, Codable, Hashable {
    public var id: String
    public var title: String
    public var description: String
    public var locationName: String
    public var date: Int64
    public var category: String // Cultural, Concert, Festival, Community
    public var imageUrl: String?
    public var organizerId: String
    public var isPromoted: Bool
    public var websiteUrl: String?
    
    public init(
        id: String = "",
        title: String = "",
        description: String = "",
        locationName: String = "",
        date: Int64 = 0,
        category: String = "Cultural",
        imageUrl: String? = nil,
        organizerId: String = "",
        isPromoted: Bool = false,
        websiteUrl: String? = nil
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.locationName = locationName
        self.date = date
        self.category = category
        self.imageUrl = imageUrl
        self.organizerId = organizerId
        self.isPromoted = isPromoted
        self.websiteUrl = websiteUrl
    }
    
    public func toMap() -> [String: Any?] {
        return [
            "id": id,
            "title": title,
            "description": description,
            "locationName": locationName,
            "date": date,
            "category": category,
            "imageUrl": imageUrl,
            "organizerId": organizerId,
            "isPromoted": isPromoted,
            "websiteUrl": websiteUrl
        ]
    }
    
    public static func fromMap(id: String, map: [String: Any?]) -> Event {
        return Event(
            id: id,
            title: map["title"] as? String ?? "",
            description: map["description"] as? String ?? "",
            locationName: map["locationName"] as? String ?? "",
            date: (map["date"] as? NSNumber)?.int64Value ?? 0,
            category: map["category"] as? String ?? "Cultural",
            imageUrl: map["imageUrl"] as? String,
            organizerId: map["organizerId"] as? String ?? "",
            isPromoted: map["isPromoted"] as? Bool ?? false,
            websiteUrl: map["websiteUrl"] as? String
        )
    }
}

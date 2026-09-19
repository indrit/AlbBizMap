// Bismillah Hir Rahman Nir Raheem
import Foundation

public struct Story: Identifiable, Codable, Hashable {
    public var id: String
    public var userId: String
    public var userName: String
    public var businessId: String?
    public var businessName: String?
    public var type: String // business, community, sponsored, user
    public var category: String
    public var location: String
    public var photos: [String]
    public var text: String
    public var createdAt: Int64
    public var expiresAt: Int64
    public var viewedBy: [String]
    public var isSponsored: Bool
    
    public init(
        id: String = "",
        userId: String = "",
        userName: String = "",
        businessId: String? = nil,
        businessName: String? = nil,
        type: String = "user",
        category: String = "",
        location: String = "",
        photos: [String] = [],
        text: String = "",
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        expiresAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000) + (24 * 60 * 60 * 1000),
        viewedBy: [String] = [],
        isSponsored: Bool = false
    ) {
        self.id = id
        self.userId = userId
        self.userName = userName
        self.businessId = businessId
        self.businessName = businessName
        self.type = type
        self.category = category
        self.location = location
        self.photos = photos
        self.text = text
        self.createdAt = createdAt
        self.expiresAt = expiresAt
        self.viewedBy = viewedBy
        self.isSponsored = isSponsored
    }
    
    public func toMap() -> [String: Any?] {
        return [
            "id": id,
            "userId": userId,
            "userName": userName,
            "businessId": businessId,
            "businessName": businessName,
            "type": type,
            "category": category,
            "location": location,
            "photos": photos,
            "text": text,
            "createdAt": createdAt,
            "expiresAt": expiresAt,
            "viewedBy": viewedBy,
            "isSponsored": isSponsored
        ]
    }
    
    public static func fromMap(id: String, map: [String: Any?]) -> Story {
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        return Story(
            id: id,
            userId: map["userId"] as? String ?? "",
            userName: map["userName"] as? String ?? "",
            businessId: map["businessId"] as? String,
            businessName: map["businessName"] as? String,
            type: map["type"] as? String ?? "user",
            category: map["category"] as? String ?? "",
            location: map["location"] as? String ?? "",
            photos: (map["photos"] as? [String]) ?? [],
            text: map["text"] as? String ?? "",
            createdAt: (map["createdAt"] as? NSNumber)?.int64Value ?? now,
            expiresAt: (map["expiresAt"] as? NSNumber)?.int64Value ?? (now + 24 * 60 * 60 * 1000),
            viewedBy: (map["viewedBy"] as? [String]) ?? [],
            isSponsored: map["isSponsored"] as? Bool ?? false
        )
    }
}

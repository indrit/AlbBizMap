// Bismillah Hir Rahman Nir Raheem
import Foundation

public struct Reply: Identifiable, Codable, Hashable {
    public var id: String
    public var reviewId: String
    public var userId: String
    public var userName: String
    public var comment: String
    public var createdAt: Int64
    public var likedBy: [String]
    
    public init(
        id: String = "",
        reviewId: String = "",
        userId: String = "",
        userName: String = "",
        comment: String = "",
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        likedBy: [String] = []
    ) {
        self.id = id
        self.reviewId = reviewId
        self.userId = userId
        self.userName = userName
        self.comment = comment
        self.createdAt = createdAt
        self.likedBy = likedBy
    }
    
    public func toMap() -> [String: Any?] {
        return [
            "id": id,
            "reviewId": reviewId,
            "userId": userId,
            "userName": userName,
            "comment": comment,
            "createdAt": createdAt,
            "likedBy": likedBy
        ]
    }
    
    public static func fromMap(id: String, map: [String: Any?]) -> Reply {
        return Reply(
            id: id,
            reviewId: map["reviewId"] as? String ?? "",
            userId: map["userId"] as? String ?? "",
            userName: map["userName"] as? String ?? "",
            comment: map["comment"] as? String ?? "",
            createdAt: (map["createdAt"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000),
            likedBy: (map["likedBy"] as? [String]) ?? []
        )
    }
}

public struct Review: Identifiable, Codable, Hashable {
    public var id: String
    public var businessId: String
    public var userId: String
    public var userName: String
    public var rating: Int
    public var comment: String
    public var photos: [String]
    public var createdAt: Int64
    public var ownerReply: String?
    public var ownerReplyAt: Int64?
    public var reportCount: Int
    public var reportedBy: [String]
    public var likedBy: [String]
    
    public init(
        id: String = "",
        businessId: String = "",
        userId: String = "",
        userName: String = "",
        rating: Int = 0,
        comment: String = "",
        photos: [String] = [],
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
        ownerReply: String? = nil,
        ownerReplyAt: Int64? = nil,
        reportCount: Int = 0,
        reportedBy: [String] = [],
        likedBy: [String] = []
    ) {
        self.id = id
        self.businessId = businessId
        self.userId = userId
        self.userName = userName
        self.rating = rating
        self.comment = comment
        self.photos = photos
        self.createdAt = createdAt
        self.ownerReply = ownerReply
        self.ownerReplyAt = ownerReplyAt
        self.reportCount = reportCount
        self.reportedBy = reportedBy
        self.likedBy = likedBy
    }
    
    public func toMap() -> [String: Any?] {
        return [
            "id": id,
            "businessId": businessId,
            "userId": userId,
            "userName": userName,
            "rating": rating,
            "comment": comment,
            "photos": photos,
            "createdAt": createdAt,
            "ownerReply": ownerReply,
            "ownerReplyAt": ownerReplyAt,
            "reportCount": reportCount,
            "reportedBy": reportedBy,
            "likedBy": likedBy
        ]
    }
    
    public static func fromMap(id: String, map: [String: Any?]) -> Review {
        return Review(
            id: id,
            businessId: map["businessId"] as? String ?? "",
            userId: map["userId"] as? String ?? "",
            userName: map["userName"] as? String ?? "",
            rating: (map["rating"] as? NSNumber)?.intValue ?? 0,
            comment: map["comment"] as? String ?? "",
            photos: (map["photos"] as? [String]) ?? [],
            createdAt: (map["createdAt"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000),
            ownerReply: map["ownerReply"] as? String,
            ownerReplyAt: (map["ownerReplyAt"] as? NSNumber)?.int64Value,
            reportCount: (map["reportCount"] as? NSNumber)?.intValue ?? 0,
            reportedBy: (map["reportedBy"] as? [String]) ?? [],
            likedBy: (map["likedBy"] as? [String]) ?? []
        )
    }
}

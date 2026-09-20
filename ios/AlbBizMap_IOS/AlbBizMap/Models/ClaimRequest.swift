// Bismillah Hir Rahman Nir Raheem
import Foundation

public struct ClaimRequest: Identifiable, Codable, Hashable {
    public var id: String
    public var businessId: String
    public var businessName: String
    public var userId: String
    public var userName: String
    public var userEmail: String
    public var reason: String
    public var status: String // pending, approved, rejected
    // "claim": user doesn't yet own this listing, wants ownerId reassigned to them.
    // "verification": user already owns this listing, just wants isVerified set —
    // approving still calls the same ownerId-reassign code, but it's a no-op since
    // userId already equals the business's current ownerId.
    public var type: String
    public var createdAt: Int64
    
    public init(
        id: String = "",
        businessId: String = "",
        businessName: String = "",
        userId: String = "",
        userName: String = "",
        userEmail: String = "",
        reason: String = "",
        status: String = "pending",
        type: String = "claim",
        createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)
    ) {
        self.id = id
        self.businessId = businessId
        self.businessName = businessName
        self.userId = userId
        self.userName = userName
        self.userEmail = userEmail
        self.reason = reason
        self.status = status
        self.type = type
        self.createdAt = createdAt
    }
    
    public func toMap() -> [String: Any?] {
        return [
            "id": id,
            "businessId": businessId,
            "businessName": businessName,
            "userId": userId,
            "userName": userName,
            "userEmail": userEmail,
            "reason": reason,
            "status": status,
            "type": type,
            "createdAt": createdAt
        ]
    }
    
    public static func fromMap(id: String, map: [String: Any?]) -> ClaimRequest {
        return ClaimRequest(
            id: id,
            businessId: map["businessId"] as? String ?? "",
            businessName: map["businessName"] as? String ?? "",
            userId: map["userId"] as? String ?? "",
            userName: map["userName"] as? String ?? "",
            userEmail: map["userEmail"] as? String ?? "",
            reason: map["reason"] as? String ?? "",
            status: map["status"] as? String ?? "pending",
            type: map["type"] as? String ?? "claim",
            createdAt: (map["createdAt"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000)
        )
    }
}

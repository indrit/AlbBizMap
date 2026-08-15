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
            createdAt: (map["createdAt"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000)
        )
    }
}

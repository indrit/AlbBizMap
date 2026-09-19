// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class FirestoreService: ObservableObject {
    public static let shared = FirestoreService()
    
    @Published public var businesses: [Business] = []
    @Published public var events: [Event] = []
    @Published public var claimRequests: [ClaimRequest] = []
    @Published public var favoriteIds: Set<String> = []
    
    public init() {
        loadSampleData()
    }
    
    private func loadSampleData() {
        // Default sample businesses matching Albanian Businesses sample seed
        self.businesses = [
            Business(
                id: "biz_1",
                name: "Sofra Shqiptare",
                category: "Restaurant",
                description: "Traditional Albanian cuisine, tavë kosi, and grilled meats in warm family setting.",
                longDescription: "Welcome to Sofra Shqiptare! We bring authentic Albanian traditional taste to your table with freshly prepared flia, tavë kosi, fergesë, and fresh salads.",
                address: "123 Main St",
                city: "New York",
                country: "USA",
                phone: "+1 212-555-0199",
                email: "info@sofrashqiptare.com",
                website: "https://sofrashqiptare.com",
                location: GeoPointLocation(latitude: 40.7128, longitude: -74.0060),
                photos: ["https://images.unsplash.com/photo-1555396273-367ea4eb4db5"],
                rating: 4.9,
                reviewCount: 28,
                isActive: true,
                isSponsored: true,
                isPremium: true,
                isVerified: true,
                isAlbanianOwned: true,
                isFeatured: true,
                likeCount: 45
            ),
            Business(
                id: "biz_2",
                name: "Peja Espresso Bar",
                category: "Cafe",
                description: "Authentic macchiato, Turkish coffee, and freshly baked pastries.",
                address: "456 Grand Ave",
                city: "Bronx",
                country: "USA",
                phone: "+1 718-555-0144",
                location: GeoPointLocation(latitude: 40.8448, longitude: -73.8648),
                photos: ["https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb"],
                rating: 4.8,
                reviewCount: 19,
                isActive: true,
                isSponsored: false,
                isPremium: true,
                isVerified: true,
                isAlbanianOwned: true,
                likeCount: 32
            ),
            Business(
                id: "biz_3",
                name: "Besa Construction & Remodeling",
                category: "Contractor",
                description: "General contractor specializing in modern home renovation, roofing, and tile work.",
                address: "789 Broadway",
                city: "Stamford",
                country: "USA",
                phone: "+1 203-555-0177",
                location: GeoPointLocation(latitude: 41.0534, longitude: -73.5387),
                photos: [],
                rating: 4.7,
                reviewCount: 12,
                isActive: true,
                isVerified: true,
                isAlbanianOwned: true,
                likeCount: 15
            )
        ]
        
        self.events = [
            Event(
                id: "evt_1",
                title: "Albanian Independence Day Celebration",
                description: "Join us for music, traditional dance, and Albanian food stall festival!",
                locationName: "Manhattan Center, NYC",
                date: Int64(Date().timeIntervalSince1970 * 1000 + 86400000 * 5),
                category: "Cultural",
                imageUrl: "https://images.unsplash.com/photo-1511578314322-379afb476865",
                isPromoted: true,
                websiteUrl: "https://albanianfestival.org"
            )
        ]
    }
    
    public func addBusiness(_ business: Business) async -> Result<String, Error> {
        var newBiz = business
        if newBiz.id.isEmpty {
            newBiz.id = "biz_" + UUID().uuidString.prefix(8)
        }
        await MainActor.run {
            self.businesses.append(newBiz)
        }
        return .success(newBiz.id)
    }
    
    public func updateBusiness(_ business: Business) async -> Result<String, Error> {
        await MainActor.run {
            if let idx = self.businesses.firstIndex(where: { $0.id == business.id }) {
                self.businesses[idx] = business
            }
        }
        return .success(business.id)
    }
    
    public func toggleFavorite(userId: String, businessId: String) {
        if favoriteIds.contains(businessId) {
            favoriteIds.remove(businessId)
        } else {
            favoriteIds.insert(businessId)
        }
    }
    
    public func toggleBusinessLike(userId: String, businessId: String) {
        if let idx = businesses.firstIndex(where: { $0.id == businessId }) {
            var biz = businesses[idx]
            if biz.likedBy.contains(userId) {
                biz.likedBy.removeAll(where: { $0 == userId })
                biz.likeCount = max(0, biz.likeCount - 1)
            } else {
                biz.likedBy.append(userId)
                biz.likeCount += 1
            }
            businesses[idx] = biz
        }
    }
    
    public func submitClaimRequest(_ claim: ClaimRequest) async -> Result<String, Error> {
        var newClaim = claim
        if newClaim.id.isEmpty {
            newClaim.id = "claim_" + UUID().uuidString.prefix(8)
        }
        await MainActor.run {
            self.claimRequests.append(newClaim)
        }
        return .success(newClaim.id)
    }
    
    public func approveClaim(_ claim: ClaimRequest) async -> Result<Void, Error> {
        await MainActor.run {
            if let idx = self.businesses.firstIndex(where: { $0.id == claim.businessId }) {
                self.businesses[idx].ownerId = claim.userId
                self.businesses[idx].isVerified = true
            }
            if let claimIdx = self.claimRequests.firstIndex(where: { $0.id == claim.id }) {
                self.claimRequests[claimIdx].status = "approved"
            }
        }
        return .success(())
    }
    
    public func rejectClaim(claimId: String) async -> Result<Void, Error> {
        await MainActor.run {
            if let claimIdx = self.claimRequests.firstIndex(where: { $0.id == claimId }) {
                self.claimRequests[claimIdx].status = "rejected"
            }
        }
        return .success(())
    }
}

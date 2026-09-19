// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import CoreLocation

public class MapViewModel: ObservableObject {
    @Published public var searchQuery: String = ""
    @Published public var selectedCategory: BusinessCategory? = nil
    @Published public var selectedBusinessId: String? = nil
    @Published public var favoriteIds: Set<String> = []
    @Published public var listSortBy: String = "default" // default, mostFavorited, topRated, recentlyAdded
    
    private var cancellables = Set<AnyCancellable>()
    
    public init() {
        FirestoreService.shared.$favoriteIds
            .assign(to: \.favoriteIds, on: self)
            .store(in: &cancellables)
    }
    
    public var businesses: [Business] {
        FirestoreService.shared.businesses
    }
    
    public var filteredBusinesses: [Business] {
        var result = businesses.filter { $0.isActive }
        
        if let category = selectedCategory {
            result = result.filter { $0.category.lowercased() == category.rawValue.lowercased() }
        }
        
        if !searchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
            let q = searchQuery.lowercased()
            result = result.filter {
                $0.name.lowercased().contains(q) ||
                $0.category.lowercased().contains(q) ||
                $0.city.lowercased().contains(q) ||
                $0.address.lowercased().contains(q) ||
                $0.description.lowercased().contains(q)
            }
        }
        
        switch listSortBy {
        case "mostFavorited":
            result.sort { $0.likeCount > $1.likeCount }
        case "topRated":
            result.sort { $0.rating > $1.rating }
        case "recentlyAdded":
            result.sort { $0.id > $1.id }
        default:
            result.sort { b1, b2 in
                if b1.isSponsored != b2.isSponsored { return b1.isSponsored }
                if b1.isFeatured != b2.isFeatured { return b1.isFeatured }
                return b1.rating > b2.rating
            }
        }
        
        return result
    }
    
    public func resetListFilters() {
        searchQuery = ""
        selectedCategory = nil
        listSortBy = "default"
    }
    
    public func toggleFavorite(businessId: String, userId: String) {
        FirestoreService.shared.toggleFavorite(userId: userId, businessId: businessId)
    }
    
    public func toggleLike(businessId: String, userId: String) {
        FirestoreService.shared.toggleBusinessLike(userId: userId, businessId: businessId)
    }
    // MARK: - Map bottom-sheet carousels (mirrors Android MapViewModel.kt)

    private func distanceKm(from user: CLLocationCoordinate2D, to location: GeoPointLocation?) -> Double? {
        guard let location = location else { return nil }
        let userLoc = CLLocation(latitude: user.latitude, longitude: user.longitude)
        let bizLoc = CLLocation(latitude: location.latitude, longitude: location.longitude)
        return userLoc.distance(from: bizLoc) / 1000.0
    }

    // "Most Favorited Worldwide" — sorted purely by like count, no distance involved.
    public var mostFavorited: [Business] {
        businesses
            .filter { $0.isActive }
            .sorted { $0.likeCount > $1.likeCount }
            .prefix(10)
            .map { $0 }
    }

    // "Near You" — businesses within 50km, sponsored/featured first, then by distance.
    public var nearMe: [Business] {
        guard let userLoc = LocationManager.shared.userLocation else { return [] }
        let withDistance: [(business: Business, distance: Double)] = businesses
            .filter { $0.isActive }
            .compactMap { biz in
                guard let d = distanceKm(from: userLoc, to: biz.location) else { return nil }
                return (biz, d)
            }
        return withDistance
            .filter { $0.distance <= 50.0 }
            .sorted { a, b in
                if a.business.isSponsored != b.business.isSponsored { return a.business.isSponsored }
                if a.business.isFeatured != b.business.isFeatured { return a.business.isFeatured }
                return a.distance < b.distance
            }
            .prefix(10)
            .map { $0.business }
    }

    // "Top Recommended" — Sponsored/Featured only, no distance cutoff (paid placement
    // should still show up even with no location fix or far away); distance only
    // breaks ties within a tier.
    public var topPicks: [Business] {
        let userLoc = LocationManager.shared.userLocation
        let withDistance: [(business: Business, distance: Double)] = businesses
            .filter { $0.isActive && ($0.isSponsored || $0.isFeatured) }
            .map { biz in
                let d = userLoc.flatMap { distanceKm(from: $0, to: biz.location) } ?? Double.greatestFiniteMagnitude
                return (biz, d)
            }
        return withDistance
            .sorted { a, b in
                if a.business.isSponsored != b.business.isSponsored { return a.business.isSponsored }
                if a.business.isFeatured != b.business.isFeatured { return a.business.isFeatured }
                return a.distance < b.distance
            }
            .prefix(10)
            .map { $0.business }
    }

}

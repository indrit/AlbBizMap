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
}

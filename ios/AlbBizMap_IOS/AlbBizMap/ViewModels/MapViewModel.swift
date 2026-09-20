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

    // List View's own independent copy of search/filter state, separate from the
    // map's searchQuery/selectedCategory above — mirrors Android's listSearchQuery
    // comment: sharing state with the map caused the two screens to stomp on each
    // other's filters.
    @Published public var listSearchQuery: String = ""
    @Published public var listSelectedCategories: Set<String> = []
    @Published public var listSelectedCountries: Set<String> = []
    @Published public var listSelectedCities: Set<String> = []

    // Mirrors favoriteIds below: forwarded from FirestoreService's own
    // @Published businesses via Combine so that any change to a business
    // document (including a like/unlike, which only touches Firestore
    // directly through a transaction) actually triggers objectWillChange on
    // this view model and re-renders whatever's observing it. Before this
    // was a plain computed property reading FirestoreService.shared.businesses
    // on each access — correct data, but nothing ever told SwiftUI to re-read
    // it, so a like's count/icon only ever caught up after an unrelated
    // re-render (e.g. leaving and reopening the screen).
    @Published public var businesses: [Business] = []
    
    private var cancellables = Set<AnyCancellable>()
    
    public init() {
        FirestoreService.shared.$favoriteIds
            .assign(to: \.favoriteIds, on: self)
            .store(in: &cancellables)
        FirestoreService.shared.$businesses
            .assign(to: \.businesses, on: self)
            .store(in: &cancellables)
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
                if b1.isEffectivelySponsored != b2.isEffectivelySponsored { return b1.isEffectivelySponsored }
                if b1.isEffectivelyFeatured != b2.isEffectivelyFeatured { return b1.isEffectivelyFeatured }
                return b1.rating > b2.rating
            }
        }
        
        return result
    }
    
    public var listFilteredBusinesses: [Business] {
        var result = businesses.filter { $0.isActive }

        if !listSearchQuery.trimmingCharacters(in: .whitespaces).isEmpty {
            let q = listSearchQuery.lowercased()
            result = result.filter {
                $0.name.lowercased().contains(q) ||
                $0.category.lowercased().contains(q) ||
                $0.address.lowercased().contains(q)
            }
        }

        if !listSelectedCategories.isEmpty {
            result = result.filter { biz in
                listSelectedCategories.contains { $0.lowercased() == BusinessCategory.displayName(for: biz.category).lowercased() }
            }
        }

        if !listSelectedCountries.isEmpty {
            result = result.filter { biz in
                listSelectedCountries.contains { $0.lowercased() == biz.country.lowercased() }
            }
        }

        if !listSelectedCities.isEmpty {
            result = result.filter { biz in
                listSelectedCities.contains { $0.lowercased() == biz.city.lowercased() }
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
                if b1.isEffectivelySponsored != b2.isEffectivelySponsored { return b1.isEffectivelySponsored }
                if b1.isEffectivelyFeatured != b2.isEffectivelyFeatured { return b1.isEffectivelyFeatured }
                return b1.rating > b2.rating
            }
        }

        return result
    }

    // Distinct, sorted country/city values available to filter by — city options
    // narrow to whichever countries are currently selected, same as Android.
    public var availableCountries: [String] {
        Array(Set(businesses.map { $0.country }.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty })).sorted()
    }

    public var availableCities: [String] {
        let scoped = businesses.filter { biz in
            listSelectedCountries.isEmpty || listSelectedCountries.contains { $0.lowercased() == biz.country.lowercased() }
        }
        return Array(Set(scoped.map { $0.city }.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty })).sorted()
    }

    public func onListCategoryToggle(_ category: String) {
        if listSelectedCategories.contains(where: { $0.lowercased() == category.lowercased() }) {
            listSelectedCategories = listSelectedCategories.filter { $0.lowercased() != category.lowercased() }
        } else {
            listSelectedCategories.insert(category)
        }
    }

    public func onListCategoryClearAll() {
        listSelectedCategories = []
    }

    public func onListCountryToggle(_ country: String) {
        if listSelectedCountries.contains(where: { $0.lowercased() == country.lowercased() }) {
            listSelectedCountries = listSelectedCountries.filter { $0.lowercased() != country.lowercased() }
        } else {
            listSelectedCountries.insert(country)
        }
    }

    public func onListCountryClearAll() {
        listSelectedCountries = []
        listSelectedCities = []
    }

    public func onListCityToggle(_ city: String) {
        if listSelectedCities.contains(where: { $0.lowercased() == city.lowercased() }) {
            listSelectedCities = listSelectedCities.filter { $0.lowercased() != city.lowercased() }
        } else {
            listSelectedCities.insert(city)
        }
    }

    public func onListCityClearAll() {
        listSelectedCities = []
    }

    public func resetListFilters() {
        listSearchQuery = ""
        listSelectedCategories = []
        listSelectedCountries = []
        listSelectedCities = []
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
                if a.business.isEffectivelySponsored != b.business.isEffectivelySponsored { return a.business.isEffectivelySponsored }
                if a.business.isEffectivelyFeatured != b.business.isEffectivelyFeatured { return a.business.isEffectivelyFeatured }
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
            .filter { $0.isActive && ($0.isEffectivelySponsored || $0.isEffectivelyFeatured) }
            .map { biz in
                let d = userLoc.flatMap { distanceKm(from: $0, to: biz.location) } ?? Double.greatestFiniteMagnitude
                return (biz, d)
            }
        return withDistance
            .sorted { a, b in
                if a.business.isEffectivelySponsored != b.business.isEffectivelySponsored { return a.business.isEffectivelySponsored }
                if a.business.isEffectivelyFeatured != b.business.isEffectivelyFeatured { return a.business.isEffectivelyFeatured }
                return a.distance < b.distance
            }
            .prefix(10)
            .map { $0.business }
    }

}

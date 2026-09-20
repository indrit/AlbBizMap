// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct BusinessListScreen: View {
    @Environment(\.appStrings) private var strings

    @ObservedObject public var viewModel: MapViewModel
    public let sortBy: String
    public let onBackClick: () -> Void
    public let onBusinessClick: (String) -> Void
    public let onNavigateToAuth: (@escaping () -> Void) -> Void

    @State private var showLocationFilters: Bool = false

    private let listCategories = ["Restaurant", "Cafe", "Market", "Lawyer", "Contractor", "Other"]

    public init(viewModel: MapViewModel, sortBy: String = "default", onBackClick: @escaping () -> Void, onBusinessClick: @escaping (String) -> Void, onNavigateToAuth: @escaping (@escaping () -> Void) -> Void) {
        self.viewModel = viewModel
        self.sortBy = sortBy
        self.onBackClick = onBackClick
        self.onBusinessClick = onBusinessClick
        self.onNavigateToAuth = onNavigateToAuth
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    if viewModel.listSearchQuery.isEmpty {
                        discoveryRow(title: strings.featured, businesses: viewModel.businesses.filter { $0.isEffectivelyFeatured || $0.isEffectivelySponsored })
                        discoveryRow(title: strings.recentlyAdded, businesses: Array(viewModel.businesses.sorted { $0.id > $1.id }.prefix(5)))
                        discoveryRow(title: strings.topRated, businesses: Array(viewModel.businesses.sorted { $0.rating > $1.rating }.prefix(5)))

                        allBusinessesHeader
                    }

                    searchBar

                    categoryFilterRow

                    if showLocationFilters && !viewModel.availableCountries.isEmpty {
                        locationFilters
                    }

                    if viewModel.listFilteredBusinesses.isEmpty {
                        emptyState
                    } else {
                        ForEach(viewModel.listFilteredBusinesses) { biz in
                            BusinessCardView(
                                business: biz,
                                isFavorite: viewModel.favoriteIds.contains(biz.id),
                                onFavoriteToggle: {
                                    if AuthManager.shared.isLoggedIn {
                                        viewModel.toggleFavorite(businessId: biz.id, userId: AuthManager.shared.currentUser?.uid ?? "")
                                    } else {
                                        onNavigateToAuth {
                                            viewModel.toggleFavorite(businessId: biz.id, userId: AuthManager.shared.currentUser?.uid ?? "")
                                        }
                                    }
                                },
                                onClick: { onBusinessClick(biz.id) }
                            )
                            .padding(.horizontal, 16)
                            .padding(.vertical, 6)
                        }
                    }

                    Spacer().frame(height: 16)
                }
            }

            if showLocationFilters {
                Button(action: { showLocationFilters = false }) {
                    Text(strings.applyFilters)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .frame(height: 48)
                        .background(Color.meTontRed)
                        .foregroundColor(.white)
                        .cornerRadius(12)
                }
                .padding(16)
                .background(Color.white)
                .shadow(color: .black.opacity(0.12), radius: 6, x: 0, y: -2)
            }
        }
        .background(Color.meTontBackground.edgesIgnoringSafeArea(.all))
        .onAppear {
            viewModel.listSortBy = sortBy
        }
    }

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.directory)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    // MARK: - Discovery rows

    private func discoveryRow(title: String, businesses: [Business]) -> some View {
        Group {
            if !businesses.isEmpty {
                VStack(alignment: .leading, spacing: 0) {
                    Text(title)
                        .font(.system(size: 17, weight: .bold))
                        .foregroundColor(.black)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 12) {
                            ForEach(businesses) { biz in
                                discoveryCard(biz)
                            }
                        }
                        .padding(.horizontal, 16)
                    }
                }
                .padding(.vertical, 8)
            }
        }
    }

    private func discoveryCard(_ biz: Business) -> some View {
        Button(action: { onBusinessClick(biz.id) }) {
            VStack(alignment: .leading, spacing: 0) {
                ZStack {
                    if let photoUrl = biz.photos.first, let url = URL(string: photoUrl) {
                        AsyncImage(url: url) { image in
                            image.resizable().aspectRatio(contentMode: .fill)
                        } placeholder: {
                            Color.gray.opacity(0.15)
                        }
                    } else {
                        Color.meTontLightRed
                        Image(systemName: "storefront.fill").foregroundColor(.meTontRed)
                    }
                }
                .frame(width: 160, height: 90)
                .clipped()

                VStack(alignment: .leading, spacing: 2) {
                    Text(biz.name)
                        .font(.system(size: 13, weight: .bold))
                        .foregroundColor(.black)
                        .lineLimit(1)
                    Text(BusinessCategory.displayName(for: biz.category))
                        .font(.system(size: 11))
                        .foregroundColor(.meTontRed)
                    HStack(spacing: 2) {
                        Image(systemName: "star.fill").font(.system(size: 10)).foregroundColor(.meTontRed)
                        Text(String(format: "%.1f", biz.rating))
                            .font(.system(size: 11))
                            .foregroundColor(.meTontGrey)
                    }
                }
                .padding(12)
            }
            .frame(width: 160)
            .background(Color.white)
            .cornerRadius(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color(red: 0xF5/255.0, green: 0xD9/255.0, blue: 0xD9/255.0), lineWidth: 1))
        }
        .buttonStyle(.plain)
    }

    // MARK: - All Businesses header

    private var allBusinessesHeader: some View {
        HStack {
            Text(strings.allBusinesses)
                .font(.system(size: 20, weight: .bold))
                .foregroundColor(.black)
            Spacer()
            if !viewModel.availableCountries.isEmpty {
                Button(action: { showLocationFilters.toggle() }) {
                    HStack(spacing: 4) {
                        Image(systemName: "line.3.horizontal.decrease.circle").font(.system(size: 14))
                        let activeCount = viewModel.listSelectedCountries.count + viewModel.listSelectedCities.count
                        Text(activeCount > 0 ? "Filters (\(activeCount))" : "Filters")
                            .font(.system(size: 13))
                    }
                    .foregroundColor(showLocationFilters ? .meTontRed : .black)
                    .padding(.horizontal, 14)
                    .padding(.vertical, 6)
                    .overlay(
                        Capsule().stroke(showLocationFilters ? Color.meTontRed : Color(red: 0xDA/255.0, green: 0xDC/255.0, blue: 0xE0/255.0), lineWidth: 1)
                    )
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 16)
        .padding(.bottom, 8)
    }

    // MARK: - Search

    private var searchBar: some View {
        HStack(spacing: 8) {
            Image(systemName: "magnifyingglass").foregroundColor(.meTontRed)
            TextField(strings.searchPlaceholder, text: $viewModel.listSearchQuery)
        }
        .padding(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }

    // MARK: - Category filter chips

    private var categoryFilterRow: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                filterChip(title: strings.allFilterOption, isSelected: viewModel.listSelectedCategories.isEmpty) {
                    viewModel.onListCategoryClearAll()
                }
                ForEach(listCategories, id: \.self) { category in
                    filterChip(title: category, isSelected: viewModel.listSelectedCategories.contains(where: { $0.lowercased() == category.lowercased() })) {
                        viewModel.onListCategoryToggle(category)
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 4)
        }
    }

    // MARK: - Location filters

    private var locationFilters: some View {
        VStack(alignment: .leading, spacing: 8) {
            VStack(alignment: .leading, spacing: 4) {
                Text(strings.countryLabel.replacingOccurrences(of: " *", with: ""))
                    .font(.system(size: 12, weight: .medium))
                    .foregroundColor(.meTontGrey)
                    .padding(.horizontal, 16)

                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        filterChip(title: strings.allFilterOption, isSelected: viewModel.listSelectedCountries.isEmpty) {
                            viewModel.onListCountryClearAll()
                        }
                        ForEach(viewModel.availableCountries, id: \.self) { country in
                            filterChip(title: country, isSelected: viewModel.listSelectedCountries.contains(where: { $0.lowercased() == country.lowercased() })) {
                                viewModel.onListCountryToggle(country)
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.vertical, 4)
                }
            }

            if !viewModel.availableCities.isEmpty {
                VStack(alignment: .leading, spacing: 4) {
                    Text(strings.cityLabel.replacingOccurrences(of: " *", with: ""))
                        .font(.system(size: 12, weight: .medium))
                        .foregroundColor(.meTontGrey)
                        .padding(.horizontal, 16)

                    ScrollView(.horizontal, showsIndicators: false) {
                        HStack(spacing: 8) {
                            filterChip(title: strings.allFilterOption, isSelected: viewModel.listSelectedCities.isEmpty) {
                                viewModel.onListCityClearAll()
                            }
                            ForEach(viewModel.availableCities, id: \.self) { city in
                                filterChip(title: city, isSelected: viewModel.listSelectedCities.contains(where: { $0.lowercased() == city.lowercased() })) {
                                    viewModel.onListCityToggle(city)
                                }
                            }
                        }
                        .padding(.horizontal, 16)
                        .padding(.vertical, 4)
                    }
                }
            }
        }
    }

    private func filterChip(title: String, isSelected: Bool, action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 12, weight: .medium))
                .padding(.horizontal, 14)
                .padding(.vertical, 8)
                .background(isSelected ? Color.meTontRed : Color.meTontLightGrey)
                .foregroundColor(isSelected ? .white : .meTontBlack)
                .cornerRadius(16)
        }
    }

    // MARK: - Empty state

    private var emptyState: some View {
        VStack(spacing: 8) {
            Image(systemName: "magnifyingglass")
                .font(.system(size: 48))
                .foregroundColor(.meTontRed.opacity(0.4))
            Text(strings.noResults)
                .font(.system(size: 16))
                .foregroundColor(.meTontGrey)
        }
        .frame(maxWidth: .infinity)
        .frame(height: 200)
    }
}

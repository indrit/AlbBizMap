// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import CoreLocation
import PhotosUI

public class AddBusinessViewModel: ObservableObject {
    @Published public var name: String = ""
    @Published public var category: BusinessCategory? = nil
    @Published public var description: String = ""
    @Published public var address: String = ""
    @Published public var city: String = ""
    @Published public var country: String = ""
    @Published public var phone: String = ""
    @Published public var email: String = ""
    @Published public var website: String = ""
    @Published public var isOpen24Hours: Bool = false
    @Published public var workingHours: [String: String] = [:]
    @Published public var isAlbanianOwned: Bool = false

    // A brand-new business always starts on the free tier (Business.maxPhotos
    // returns 1 until isPremium/isFeatured/isSponsored is set), same as Android's
    // AddBusinessScreen capping selectedImageUris to a single photo — so this is
    // a single optional photo, not a gallery.
    @Published public var selectedPhotoData: Data? = nil

    @Published public var isGeocoding: Bool = false
    @Published public var isUploadingPhoto: Bool = false
    @Published public var errorMessage: String? = nil
    @Published public var isSubmitting: Bool = false

    public init() {}

    // Mirrors Android's flow exactly: address/city/country aren't geocoded until
    // Save is tapped — no separate "pick location" step, and no raw lat/long
    // shown to the user.
    public func submit(ownerId: String, strings: AppStrings) async -> Bool {
        if name.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = strings.businessNameRequired
            return false
        }
        guard let category else {
            errorMessage = strings.selectCategory
            return false
        }
        if description.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = strings.descriptionRequired
            return false
        }
        if address.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = strings.addressRequired
            return false
        }
        if city.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = strings.cityRequired
            return false
        }
        if phone.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = strings.phoneRequired
            return false
        }

        errorMessage = nil
        await MainActor.run { self.isGeocoding = true }
        let fullAddress = "\(address.trimmingCharacters(in: .whitespaces)), \(city.trimmingCharacters(in: .whitespaces)), \(country.trimmingCharacters(in: .whitespaces))"
        let coord = await LocationManager.geocodeAddress(fullAddress)
        await MainActor.run { self.isGeocoding = false }

        guard let coord else {
            await MainActor.run { self.errorMessage = strings.geocodeFailed }
            return false
        }

        // The doc id has to be known before the photo upload (Storage path is
        // businesses/{id}/image_0.jpg), so it's generated client-side up front —
        // same order as Android's AddBusinessViewModel: upload the photo first,
        // then create the business document with its URL already in `photos`.
        let businessId = UUID().uuidString
        var photoUrls: [String] = []
        if let data = selectedPhotoData {
            await MainActor.run { self.isUploadingPhoto = true }
            do {
                let url = try await FirestoreService.shared.uploadImage(businessId: businessId, data: data, index: 0)
                photoUrls = [url]
            } catch {
                await MainActor.run {
                    self.isUploadingPhoto = false
                    self.errorMessage = strings.failedToUploadPhoto
                }
                return false
            }
            await MainActor.run { self.isUploadingPhoto = false }
        }

        let biz = Business(
            id: businessId,
            name: name.trimmingCharacters(in: .whitespaces),
            category: category.storageKey,
            description: description.trimmingCharacters(in: .whitespaces),
            address: address.trimmingCharacters(in: .whitespaces),
            city: city.trimmingCharacters(in: .whitespaces),
            country: country.trimmingCharacters(in: .whitespaces),
            phone: phone.trimmingCharacters(in: .whitespaces),
            email: email.trimmingCharacters(in: .whitespaces),
            website: website.trimmingCharacters(in: .whitespaces),
            isOpen24Hours: isOpen24Hours,
            workingHours: isOpen24Hours ? [:] : workingHours,
            location: GeoPointLocation(latitude: coord.latitude, longitude: coord.longitude),
            photos: photoUrls,
            ownerId: ownerId,
            isAlbanianOwned: isAlbanianOwned
        )

        await MainActor.run { self.isSubmitting = true }
        let result = await FirestoreService.shared.addBusiness(biz)
        await MainActor.run { self.isSubmitting = false }

        switch result {
        case .success:
            // Auto-create a "Just opened" story, mirroring Android's
            // AddBusinessViewModel — fire-and-forget, since a story-creation
            // hiccup shouldn't block or fail the business submission itself.
            // The photos are already-hosted Storage URLs from the upload
            // above, so this goes through addStoryWithHostedPhotos, not
            // addStory (which would try to re-upload local data).
            Task {
                let newBusinessStory = Story(
                    userId: biz.ownerId,
                    userName: biz.name,
                    businessId: biz.id,
                    businessName: biz.name,
                    type: "new_business",
                    category: biz.category,
                    location: biz.address,
                    photos: biz.photos,
                    text: "🆕 Just opened in \(biz.address)! Come visit us.",
                    isSponsored: false
                )
                _ = await StoriesRepository.shared.addStoryWithHostedPhotos(newBusinessStory)
            }
            return true
        case .failure(let err):
            await MainActor.run { self.errorMessage = err.localizedDescription }
            return false
        }
    }
}

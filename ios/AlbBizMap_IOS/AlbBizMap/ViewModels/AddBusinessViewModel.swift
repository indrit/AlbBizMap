// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine
import CoreLocation

public class AddBusinessViewModel: ObservableObject {
    @Published public var name: String = ""
    @Published public var category: BusinessCategory = .restaurant
    @Published public var description: String = ""
    @Published public var address: String = ""
    @Published public var city: String = ""
    @Published public var country: String = ""
    @Published public var phone: String = ""
    @Published public var email: String = ""
    @Published public var website: String = ""
    @Published public var isOpen24Hours: Bool = false
    @Published public var isAlbanianOwned: Bool = true
    @Published public var latitudeString: String = ""
    @Published public var longitudeString: String = ""
    
    @Published public var isLocating: Bool = false
    @Published public var errorMessage: String? = nil
    @Published public var isSubmitting: Bool = false
    
    public init() {}
    
    public func geocodeCurrentAddress() async {
        let fullAddress = "\(address), \(city), \(country)".trimmingCharacters(in: .whitespacesAndNewlines)
        guard !fullAddress.isEmpty else { return }
        await MainActor.run { self.isLocating = true; self.errorMessage = nil }
        
        if let coord = await LocationManager.geocodeAddress(fullAddress) {
            await MainActor.run {
                self.latitudeString = "\(coord.latitude)"
                self.longitudeString = "\(coord.longitude)"
                self.isLocating = false
            }
        } else {
            await MainActor.run {
                self.errorMessage = "Couldn't find that address — please check it's correct"
                self.isLocating = false
            }
        }
    }
    
    public func submit(ownerId: String) async -> Bool {
        if name.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "Business name is required"
            return false
        }
        if address.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "Address is required"
            return false
        }
        if city.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "City is required"
            return false
        }
        if phone.trimmingCharacters(in: .whitespaces).isEmpty {
            errorMessage = "Phone number is required"
            return false
        }
        
        let lat = Double(latitudeString) ?? 40.7128
        let lng = Double(longitudeString) ?? -74.0060
        
        let biz = Business(
            name: name,
            category: category.rawValue,
            description: description,
            address: address,
            city: city,
            country: country,
            phone: phone,
            email: email,
            website: website,
            isOpen24Hours: isOpen24Hours,
            location: GeoPointLocation(latitude: lat, longitude: lng),
            ownerId: ownerId,
            isAlbanianOwned: isAlbanianOwned
        )
        
        await MainActor.run { self.isSubmitting = true }
        let result = await FirestoreService.shared.addBusiness(biz)
        await MainActor.run { self.isSubmitting = false }
        
        switch result {
        case .success: return true
        case .failure(let err):
            await MainActor.run { self.errorMessage = err.localizedDescription }
            return false
        }
    }
}

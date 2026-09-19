// Bismillah Hir Rahman Nir Raheem
import Foundation
import CoreLocation
import Combine

public class LocationManager: NSObject, ObservableObject, CLLocationManagerDelegate {
    public static let shared = LocationManager()
    
    private let manager = CLLocationManager()
    
    @Published public var userLocation: CLLocationCoordinate2D? = nil
    @Published public var authorizationStatus: CLAuthorizationStatus = .notDetermined
    
    override private init() {
        super.init()
        manager.delegate = self
        manager.desiredAccuracy = kCLLocationAccuracyBest
        manager.requestWhenInUseAuthorization()
        manager.startUpdatingLocation()
    }
    
    public func requestLocation() {
        manager.requestLocation()
    }
    
    public func locationManager(_ manager: CLLocationManager, didUpdateLocations locations: [CLLocation]) {
        guard let loc = locations.last else { return }
        DispatchQueue.main.async {
            self.userLocation = loc.coordinate
        }
    }
    
    public func locationManager(_ manager: CLLocationManager, didFailWithError error: Error) {
        print("LocationManager error: \(error.localizedDescription)")
    }
    
    public func locationManagerDidChangeAuthorization(_ manager: CLLocationManager) {
        DispatchQueue.main.async {
            self.authorizationStatus = manager.authorizationStatus
            if manager.authorizationStatus == .authorizedWhenInUse || manager.authorizationStatus == .authorizedAlways {
                manager.startUpdatingLocation()
            }
        }
    }
    
    public static func geocodeAddress(_ addressString: String) async -> CLLocationCoordinate2D? {
        let geocoder = CLGeocoder()
        do {
            let placemarks = try await geocoder.geocodeAddressString(addressString)
            return placemarks.first?.location?.coordinate
        } catch {
            print("Geocoding failed: \(error.localizedDescription)")
            return nil
        }
    }
}

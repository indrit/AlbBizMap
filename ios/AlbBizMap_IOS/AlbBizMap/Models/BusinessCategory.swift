// Bismillah Hir Rahman Nir Raheem
import Foundation
import SwiftUI

public enum BusinessCategory: String, CaseIterable, Identifiable, Codable {
    case restaurant = "Restaurant"
    case cafe = "Cafe"
    case market = "Market"
    case contractor = "Contractor"
    case lawyer = "Lawyer"
    case dentist = "Dentist"
    case barber = "Barber"
    case beautySalon = "Beauty Salon"
    case autoShop = "Auto Shop"
    case other = "Other"
    
    public var id: String { rawValue }
    
    public var displayName: String { rawValue }
    
    public var iconName: String {
        switch self {
        case .restaurant: return "fork.knife"
        case .cafe: return "cup.and.saucer.fill"
        case .market: return "cart.fill"
        case .contractor: return "hammer.fill"
        case .lawyer: return "building.columns.fill"
        case .dentist: return "cross.case.fill"
        case .barber: return "scissors"
        case .beautySalon: return "sparkles"
        case .autoShop: return "car.fill"
        case .other: return "briefcase.fill"
        }
    }
}

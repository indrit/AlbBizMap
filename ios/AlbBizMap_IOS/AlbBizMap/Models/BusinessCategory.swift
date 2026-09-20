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
    
    // Matches Android's Kotlin enum constant name exactly (e.g. BEAUTY_SALON,
    // AUTO_SHOP) — that's what Android's Business.category field actually
    // stores (`selectedCategory!!.name`, not its displayName), so this is what
    // iOS needs to write for a business to parse correctly back on Android,
    // and what `match(_:)` below checks first when reading data either
    // platform created.
    public var storageKey: String {
        switch self {
        case .restaurant: return "RESTAURANT"
        case .cafe: return "CAFE"
        case .market: return "MARKET"
        case .contractor: return "CONTRACTOR"
        case .lawyer: return "LAWYER"
        case .dentist: return "DENTIST"
        case .barber: return "BARBER"
        case .beautySalon: return "BEAUTY_SALON"
        case .autoShop: return "AUTO_SHOP"
        case .other: return "OTHER"
        }
    }
    
    // Resolves a stored Business.category string to its case, regardless of
    // which platform wrote it: Android's own storageKey format ("BEAUTY_SALON"),
    // or the display-string format ("Beauty Salon") that iOS wrote before this
    // fix. Case-insensitive so stray casing differences in old data still match.
    public static func match(_ stored: String) -> BusinessCategory? {
        if let byKey = allCases.first(where: { $0.storageKey == stored }) { return byKey }
        if let byName = allCases.first(where: { $0.rawValue.lowercased() == stored.lowercased() }) { return byName }
        return nil
    }
    
    // For plain-text display (list rows, detail screen, category filter chips):
    // resolves to the human-readable label regardless of storage format, and
    // falls back to the raw stored string for anything unrecognized rather
    // than showing nothing.
    public static func displayName(for stored: String) -> String {
        match(stored)?.displayName ?? stored
    }
    
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

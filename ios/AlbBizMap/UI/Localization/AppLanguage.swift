// Bismillah Hir Rahman Nir Raheem
import Foundation

public enum AppLanguage: String, CaseIterable, Identifiable, Codable {
    case en = "EN"
    case sq = "SQ"
    
    public var id: String { rawValue }
    
    public var displayName: String {
        switch self {
        case .en: return "English"
        case .sq: return "Shqip"
        }
    }
}

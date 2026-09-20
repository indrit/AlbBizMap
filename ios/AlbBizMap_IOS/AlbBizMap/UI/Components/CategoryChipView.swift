// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct CategoryChipView: View {
    public let category: BusinessCategory
    public let isSelected: Bool
    public let action: () -> Void
    
    public init(category: BusinessCategory, isSelected: Bool, action: @escaping () -> Void) {
        self.category = category
        self.isSelected = isSelected
        self.action = action
    }
    
    public var body: some View {
        Button(action: action) {
            HStack(spacing: 6) {
                Image(systemName: category.iconName)
                    .font(.caption)
                Text(category.displayName)
                    .font(.subheadline)
                    .fontWeight(.medium)
            }
            .padding(.horizontal, 14)
            .padding(.vertical, 8)
            .background(isSelected ? Color.meTontRed : Color.white)
            .foregroundColor(isSelected ? .white : .meTontBlack)
            .cornerRadius(20)
            .shadow(color: Color.black.opacity(0.1), radius: 3, x: 0, y: 1)
        }
    }
}

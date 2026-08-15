// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct AuthGatePrompt: View {
    @Environment(\.appStrings) private var strings
    public let isPresented: Bool
    public let onConfirm: () -> Void
    public let onDismiss: () -> Void
    
    public init(isPresented: Bool, onConfirm: @escaping () -> Void, onDismiss: @escaping () -> Void) {
        self.isPresented = isPresented
        self.onConfirm = onConfirm
        self.onDismiss = onDismiss
    }
    
    public var body: some View {
        if isPresented {
            ZStack {
                Color.black.opacity(0.4)
                    .edgesIgnoringSafeArea(.all)
                    .onTapGesture { onDismiss() }
                
                VStack(spacing: 16) {
                    Image(systemName: "lock.shield.fill")
                        .font(.system(size: 44))
                        .foregroundColor(.meTontRed)
                    
                    Text(strings.signIn)
                        .font(.title3)
                        .fontWeight(.bold)
                    
                    Text(strings.signInToContinue)
                        .font(.subheadline)
                        .foregroundColor(.gray)
                        .multilineTextAlignment(.center)
                    
                    HStack(spacing: 12) {
                        Button(action: onDismiss) {
                            Text(strings.notNow)
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(Color.gray.opacity(0.15))
                                .foregroundColor(.meTontBlack)
                                .cornerRadius(10)
                        }
                        
                        Button(action: onConfirm) {
                            Text(strings.signIn)
                                .fontWeight(.semibold)
                                .frame(maxWidth: .infinity)
                                .padding(.vertical, 12)
                                .background(Color.meTontRed)
                                .foregroundColor(.white)
                                .cornerRadius(10)
                        }
                    }
                    .padding(.top, 8)
                }
                .padding(24)
                .background(Color.white)
                .cornerRadius(20)
                .shadow(radius: 10)
                .padding(.horizontal, 40)
            }
        }
    }
}

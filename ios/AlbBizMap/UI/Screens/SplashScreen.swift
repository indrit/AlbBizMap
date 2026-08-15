// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct SplashScreen: View {
    @Environment(\.appStrings) private var strings
    public let onFinished: () -> Void
    
    @State private var opacity: Double = 0.0
    @State private var scale: CGFloat = 0.8
    
    public init(onFinished: @escaping () -> Void) {
        self.onFinished = onFinished
    }
    
    public var body: some View {
        ZStack {
            Color.meTontRed.edgesIgnoringSafeArea(.all)
            
            VStack(spacing: 20) {
                Image(systemName: "map.fill")
                    .font(.system(size: 80))
                    .foregroundColor(.white)
                
                Text(strings.appName)
                    .font(.system(size: 40, weight: .bold))
                    .foregroundColor(.white)
                
                Text("Albanian Business Map")
                    .font(.subheadline)
                    .foregroundColor(.white.opacity(0.9))
            }
            .scaleEffect(scale)
            .opacity(opacity)
        }
        .onAppear {
            withAnimation(.easeOut(duration: 1.0)) {
                self.opacity = 1.0
                self.scale = 1.0
            }
            DispatchQueue.main.asyncAfter(deadline: .now() + 1.8) {
                onFinished()
            }
        }
    }
}

// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import FirebaseCore
import GoogleSignIn

@main
struct AlbBizMapApp: App {
    init() {
        FirebaseApp.configure()

        // GIDSignIn needs an explicit client ID before it can present anything —
        // without this it silently does nothing when signIn(withPresenting:) is
        // called instead of showing the account picker. Firebase already has the
        // same client ID from GoogleService-Info.plist, so we just hand it over.
        if let clientID = FirebaseApp.app()?.options.clientID {
            GIDSignIn.sharedInstance.configuration = GIDConfiguration(clientID: clientID)
        }
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
                .onOpenURL { url in
                    // Completes the Google Sign-In flow after the system browser/
                    // Google account picker redirects back into the app.
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}

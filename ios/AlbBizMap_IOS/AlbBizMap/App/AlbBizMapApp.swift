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
                // Every color in this app (ThemeColors.swift, and the many
                // hardcoded Color.white/.black used directly in screens like
                // MapScreen's search bar) is a fixed brand value -- none of it
                // adapts for Dark Mode. Text fields in particular never set an
                // explicit foregroundColor, so they were falling back to the
                // system's default text color, which turns white in Dark Mode
                // -- invisible against the app's hardcoded-white field
                // backgrounds (this is what was reported as "white text in
                // the search bar"). Rather than hand-patching every text field
                // across ~9 screens (and re-introducing the bug on the next
                // new one), pin the whole app to light appearance so none of
                // the fixed brand colors ever get paired against a system
                // color that shifted out from under them.
                .preferredColorScheme(.light)
        }
    }
}

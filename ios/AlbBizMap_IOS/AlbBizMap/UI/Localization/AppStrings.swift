// Bismillah Hir Rahman Nir Raheem
import Foundation
import SwiftUI

public struct AppStrings {
    // General
    public var appName: String
    public var save: String
    public var cancel: String
    public var back: String
    public var loading: String
    public var noResults: String
    public var welcomeUser: String
    
    // Auth
    public var welcomeTitle: String
    public var welcomeDesc: String
    public var getStarted: String
    public var signIn: String
    public var signUp: String
    public var email: String
    public var password: String
    public var confirmPassword: String
    public var logout: String
    public var noAccount: String
    public var haveAccount: String
    public var forgotPassword: String
    public var resetPasswordTitle: String
    public var resetPasswordDescription: String
    public var sendResetLink: String
    public var resetEmailSent: String
    public var continueWithGoogle: String
    public var continueWithApple: String
    
    // Map
    public var searchPlaceholder: String
    public var addBusiness: String
    public var listView: String
    public var favorites: String
    public var profile: String
    public var communityEvents: String
    public var viewDetailsAndRate: String
    
    // Business List
    public var directory: String
    public var featured: String
    public var recentlyAdded: String
    public var topRated: String
    public var allBusinesses: String
    public var getDirections: String
    public var nearMe: String
    
    // Business Detail
    public var recentReviews: String
    public var noReviewsYet: String
    public var writeReview: String
    public var editBusiness: String
    public var upgradePremium: String
    public var viewPlans: String
    public var photos: String
    public var workingHours: String
    public var promotions: String
    public var jobs: String
    public var readMore: String
    public var readLess: String
    public var category: String
    public var verified: String
    public var albanianOwned: String
    public var premium: String
    public var featured2: String
    public var sponsored: String
    
    // Profile
    public var myProfile: String
    public var personalInformation: String
    public var firstName: String
    public var lastName: String
    public var saveProfile: String
    public var upgradeToPremium: String
    
    // Events
    public var communityEventsTitle: String
    public var noEventsFound: String
    public var submitEvent: String
    public var eventTitle: String
    public var eventDescription: String
    public var eventDate: String
    public var eventLocation: String
    public var eventWebsite: String
    public var eventCategory: String
    public var addPhoto: String
    public var changePhoto: String
    public var promoted: String
    public var viewEventWebsite: String
    
    // Reviews
    public var rateThisBusiness: String
    public var tapStarToRate: String
    public var shareExperience: String
    public var submitReview: String
    public var submitting: String
    
    // Subscription
    public var choosePlan: String
    public var upgradeYourListing: String
    public var currentPlan: String
    public var requestUpgrade: String
    public var requestSponsorship: String
    public var requestFeatured: String
    public var manualPaymentNote: String
    
    // Favorites
    public var myFavorites: String
    public var noFavoritesYet: String
    public var myBusinesses: String
    public var noBusinessesYet: String
    public var noBusinessesYetSubtitle: String
    public var myBusinessesSubtitle: String
    public var noSearchResults: String
    public var upgradePremiumTitle: String
    
    public var firstNameRequired: String
    public var profileSaved: String
    public var profileSaveFailed: String
    
    public var eventPhotoSection: String
    public var eventDetailsSection: String
    public var eventDateSection: String
    public var eventLocationSection: String
    
    public var eventTitleRequired: String
    public var eventDescriptionRequired: String
    public var eventLocationRequired: String
    public var eventInvalidDate: String
    public var eventSubmitSuccess: String
    public var eventSubmitFailed: String
    public var submitEventButton: String
    
    public var writeReviewLabel: String
    public var pleaseSelectRating: String
    public var pleaseWriteReview: String
    public var reviewSubmitted: String
    
    public var welcomeBack: String
    public var signInToContinue: String
    public var signUpToGetStarted: String
    public var notNow: String
    
    public var emailRequired: String
    public var passwordRequired: String
    public var passwordsDoNotMatch: String
    public var passwordTooShort: String
    
    public var registerBusiness: String
    public var requiredInformation: String
    public var businessName: String
    public var locationSection: String
    public var fullAddress: String
    public var cityLabel: String
    public var countryLabel: String
    public var cityRequired: String
    public var locatingAddress: String
    public var geocodeFailed: String
    public var latitude: String
    public var longitude: String
    public var pickLocationFromMap: String
    public var contactInformation: String
    public var phoneNumber: String
    public var emailOptional: String
    public var websiteOptional: String
    public var workingHoursSection: String
    public var open247: String
    public var photoOptional: String
    public var photoAdded: String
    public var registering: String
    public var registerBusinessButton: String
    public var businessNameRequired: String
    public var selectCategory: String
    public var descriptionRequired: String
    public var addressRequired: String
    public var phoneRequired: String
    public var validCoordinates: String
    public var businessRegistered: String
    public var cameraPermissionRequired: String
    public var gallery: String
    public var camera: String
    public var choosePhotoSource: String
    
    public static let english = AppStrings(
        appName: "MeTont",
        save: "Save",
        cancel: "Cancel",
        back: "Back",
        loading: "Loading...",
        noResults: "No businesses found",
        welcomeUser: "Welcome",
        welcomeTitle: "Welcome to Albanian Business App",
        welcomeDesc: "Register your business, or explore the Albanian business map in your area and beyond.",
        getStarted: "Get Started",
        signIn: "Sign In",
        signUp: "Create Account",
        email: "Email",
        password: "Password",
        confirmPassword: "Confirm Password",
        logout: "Logout",
        noAccount: "Don't have an account? Sign Up",
        haveAccount: "Already have an account? Sign In",
        forgotPassword: "Forgot password?",
        resetPasswordTitle: "Reset Password",
        resetPasswordDescription: "Enter your email and we'll send you a link to reset your password.",
        sendResetLink: "Send Reset Link",
        resetEmailSent: "Password reset email sent! Check your inbox.",
        continueWithGoogle: "Continue with Google",
        continueWithApple: "Continue with Apple",
        searchPlaceholder: "Search businesses...",
        addBusiness: "Add My Business",
        listView: "List View",
        favorites: "My Favorites",
        profile: "Profile",
        communityEvents: "Community Events",
        viewDetailsAndRate: "View Details & Rate",
        directory: "Directory",
        featured: "Featured Businesses",
        recentlyAdded: "Recently Added",
        topRated: "Top Rated",
        allBusinesses: "All Businesses",
        getDirections: "Get Directions",
        nearMe: "Near Me",
        recentReviews: "Recent Reviews",
        noReviewsYet: "No reviews yet. Be the first!",
        writeReview: "Review",
        editBusiness: "Edit",
        upgradePremium: "Upgrade to Premium to unlock contact info, website, photos and more for just $2.99/month",
        viewPlans: "View Plans",
        photos: "Photos",
        workingHours: "Working Hours",
        promotions: "Promotions & Deals",
        jobs: "Job Postings",
        readMore: "Read more",
        readLess: "Read less",
        category: "Category",
        verified: "Verified",
        albanianOwned: "Albanian Owned",
        premium: "Premium",
        featured2: "Featured",
        sponsored: "Sponsored",
        myProfile: "My Profile",
        personalInformation: "Personal Information",
        firstName: "First Name",
        lastName: "Last Name",
        saveProfile: "Save Profile",
        upgradeToPremium: "Upgrade to Premium",
        communityEventsTitle: "Community Events",
        noEventsFound: "No upcoming events found.",
        submitEvent: "Submit Event",
        eventTitle: "Event Title",
        eventDescription: "Description",
        eventDate: "Event Date",
        eventLocation: "Location Name",
        eventWebsite: "Website URL (Optional)",
        eventCategory: "Category",
        addPhoto: "Add Photo",
        changePhoto: "Change Photo",
        promoted: "PROMOTED",
        viewEventWebsite: "View Event Website",
        rateThisBusiness: "Rate this business",
        tapStarToRate: "Tap a star to select a rating",
        shareExperience: "Share your experience...",
        submitReview: "Submit Review",
        submitting: "Submitting...",
        choosePlan: "Choose Your Plan",
        upgradeYourListing: "Upgrade Your Listing",
        currentPlan: "CURRENT",
        requestUpgrade: "Request Upgrade",
        requestSponsorship: "Request Sponsorship",
        requestFeatured: "Request Featured",
        manualPaymentNote: "Payments are currently processed manually. We will contact you within 24 hours of your request.",
        myFavorites: "My Favorites",
        noFavoritesYet: "You haven't saved any businesses yet.",
        myBusinesses: "My Businesses",
        noBusinessesYet: "You don't have any businesses yet",
        noBusinessesYetSubtitle: "Add your business to reach more customers",
        myBusinessesSubtitle: "Manage your business listings",
        noSearchResults: "No results found",
        upgradePremiumTitle: "Upgrade to Premium",
        firstNameRequired: "First name is required",
        profileSaved: "Profile saved!",
        profileSaveFailed: "Failed to save profile",
        eventPhotoSection: "Event Photo (Optional)",
        eventDetailsSection: "Event Details",
        eventDateSection: "Event Date *",
        eventLocationSection: "Location & Contact",
        eventTitleRequired: "Event title is required",
        eventDescriptionRequired: "Description is required",
        eventLocationRequired: "Location is required",
        eventInvalidDate: "Please enter a valid date",
        eventSubmitSuccess: "Event submitted successfully!",
        eventSubmitFailed: "Failed to submit event",
        submitEventButton: "Submit Event",
        writeReviewLabel: "Write your review",
        pleaseSelectRating: "Please select a rating",
        pleaseWriteReview: "Please write a review",
        reviewSubmitted: "Review submitted!",
        welcomeBack: "Welcome back!",
        signInToContinue: "Sign in to continue",
        signUpToGetStarted: "Sign up to get started",
        notNow: "Not now",
        emailRequired: "Email is required",
        passwordRequired: "Password is required",
        passwordsDoNotMatch: "Passwords don't match",
        passwordTooShort: "Password must be at least 8 characters",
        registerBusiness: "Register Business",
        requiredInformation: "Required Information",
        businessName: "Business Name *",
        locationSection: "Location *",
        fullAddress: "Full Address *",
        cityLabel: "City *",
        countryLabel: "Country *",
        cityRequired: "City is required",
        locatingAddress: "Finding coordinates…",
        geocodeFailed: "Couldn't find that address — please check it's correct",
        latitude: "Latitude *",
        longitude: "Longitude *",
        pickLocationFromMap: "Pick Location from Map",
        contactInformation: "Contact Information",
        phoneNumber: "Phone Number *",
        emailOptional: "Email (Optional)",
        websiteOptional: "Website (Optional)",
        workingHoursSection: "Working Hours",
        open247: "Open 24/7",
        photoOptional: "Photo (Optional)",
        photoAdded: "Photo Added (1/1)",
        registering: "Registering...",
        registerBusinessButton: "Register Business",
        businessNameRequired: "Business name is required",
        selectCategory: "Please select a category",
        descriptionRequired: "Description is required",
        addressRequired: "Address is required",
        phoneRequired: "Phone number is required",
        validCoordinates: "Please enter valid coordinates",
        businessRegistered: "Business registered successfully!",
        cameraPermissionRequired: "Camera permission required",
        gallery: "Gallery",
        camera: "Camera",
        choosePhotoSource: "Choose photo source"
    )
    
    public static let albanian = AppStrings(
        appName: "MeTont",
        save: "Ruaj",
        cancel: "Anulo",
        back: "Kthehu",
        loading: "Duke ngarkuar...",
        noResults: "Nuk u gjet asnjë biznes",
        welcomeUser: "Mirë se vini",
        welcomeTitle: "Mirësevini në MeTont",
        welcomeDesc: "Regjistroni biznesin tuaj, ose eksploroni hartën e bizneseve shqiptare në zonën tuaj dhe më gjerë.",
        getStarted: "Fillo Tani",
        signIn: "Hyr",
        signUp: "Krijo Llogari",
        email: "Email",
        password: "Fjalëkalimi",
        confirmPassword: "Konfirmo Fjalëkalimin",
        logout: "Dil",
        noAccount: "Nuk keni llogari? Regjistrohu",
        haveAccount: "Keni llogari? Hyr",
        forgotPassword: "Keni harruar fjalëkalimin?",
        resetPasswordTitle: "Rivendos Fjalëkalimin",
        resetPasswordDescription: "Shkruani email-in tuaj dhe ne do t'ju dërgojmë një lidhje për të rivendosur fjalëkalimin.",
        sendResetLink: "Dërgo Lidhjen",
        resetEmailSent: "Email-i për rivendosjen e fjalëkalimit u dërgua! Kontrolloni inbox-in tuaj.",
        continueWithGoogle: "Vazhdo me Google",
        continueWithApple: "Vazhdo me Apple",
        searchPlaceholder: "Kërko biznese...",
        addBusiness: "Shto Biznesin Tim",
        listView: "Lista",
        favorites: "Të Preferuarat",
        profile: "Profili",
        communityEvents: "Ngjarjet e Komunitetit",
        viewDetailsAndRate: "Shiko Detajet & Vlerëso",
        directory: "Drejtori",
        featured: "Bizneset e Rëndësishme",
        recentlyAdded: "Shtuar Së Fundmi",
        topRated: "Më të Vlerësuarit",
        allBusinesses: "Të Gjitha Bizneset",
        getDirections: "Drejtimet",
        nearMe: "Pranë Meje",
        recentReviews: "Vlerësimet e Fundit",
        noReviewsYet: "Asnjë vlerësim ende. Bëhu i pari!",
        writeReview: "Vlerëso",
        editBusiness: "Ndrysho",
        upgradePremium: "Kaloni në Premium për të zhbllokuar kontaktet, faqen dhe fotot për vetëm $2.99/muaj",
        viewPlans: "Shiko Planet",
        photos: "Foto",
        workingHours: "Orari i Punës",
        promotions: "Promocione dhe Oferta",
        jobs: "Oferta Pune",
        readMore: "Lexo më shumë",
        readLess: "Lexo më pak",
        category: "Kategoria",
        verified: "Verifikuar",
        albanianOwned: "Pronë Shqiptare",
        premium: "Premium",
        featured2: "I Veçuar",
        sponsored: "Sponsorizuar",
        myProfile: "Profili Im",
        personalInformation: "Informacioni Personal",
        firstName: "Emri",
        lastName: "Mbiemri",
        saveProfile: "Ruaj Profilin",
        upgradeToPremium: "Kalo në Premium",
        communityEventsTitle: "Ngjarjet e Komunitetit",
        noEventsFound: "Nuk u gjetën ngjarje të ardhshme.",
        submitEvent: "Dërgo Ngjarjen",
        eventTitle: "Titulli i Ngjarjes",
        eventDescription: "Përshkrimi",
        eventDate: "Data e Ngjarjes",
        eventLocation: "Emri i Vendit",
        eventWebsite: "URL e Faqes (Opsionale)",
        eventCategory: "Kategoria",
        addPhoto: "Shto Foto",
        changePhoto: "Ndrysho Foton",
        promoted: "PROMOVUAR",
        viewEventWebsite: "Shiko Faqen e Ngjarjes",
        rateThisBusiness: "Vlerëso këtë biznes",
        tapStarToRate: "Trokitni një yll për të vlerësuar",
        shareExperience: "Ndani përvojën tuaj...",
        submitReview: "Dërgo Vlerësimin",
        submitting: "Duke dërguar...",
        choosePlan: "Zgjidhni Planin Tuaj",
        upgradeYourListing: "Përmirësoni Listimin Tuaj",
        currentPlan: "AKTUAL",
        requestUpgrade: "Kërkoni Përmirësim",
        requestSponsorship: "Kërkoni Sponsorizim",
        requestFeatured: "Kërkoni të Veçohet",
        manualPaymentNote: "Pagesat aktualisht procesohen manualisht. Do t'ju kontaktojmë brenda 24 orëve.",
        myFavorites: "Të Preferuarat e Mia",
        noFavoritesYet: "Nuk keni ruajtur asnjë biznes ende.",
        myBusinesses: "Bizneset e Mia",
        noBusinessesYet: "Ende nuk keni asnjë biznes",
        noBusinessesYetSubtitle: "Shtoni biznesin tuaj për të arritur më shumë klientë",
        myBusinessesSubtitle: "Menaxhoni listimet e biznesit tuaj",
        noSearchResults: "Nuk u gjetën rezultate",
        upgradePremiumTitle: "Kalo në Premium",
        firstNameRequired: "Emri është i detyrueshëm",
        profileSaved: "Profili u ruajt!",
        profileSaveFailed: "Dështoi ruajtja e profilit",
        eventPhotoSection: "Foto e Ngjarjes (Opsionale)",
        eventDetailsSection: "Detajet e Ngjarjes",
        eventDateSection: "Data e Ngjarjes *",
        eventLocationSection: "Vendndodhja & Kontakti",
        eventTitleRequired: "Titulli i ngjarjes është i detyrueshëm",
        eventDescriptionRequired: "Përshkrimi është i detyrueshëm",
        eventLocationRequired: "Vendndodhja është e detyrueshme",
        eventInvalidDate: "Ju lutemi vendosni një datë të vlefshme",
        eventSubmitSuccess: "Ngjarja u dërgua me sukses!",
        eventSubmitFailed: "Dështoi dërgimi i ngjarjes",
        submitEventButton: "Dërgo Ngjarjen",
        writeReviewLabel: "Shkruaj vlerësimin tuaj",
        pleaseSelectRating: "Ju lutemi zgjidhni një vlerësim",
        pleaseWriteReview: "Ju lutemi shkruani një vlerësim",
        reviewSubmitted: "Vlerësimi u dërgua!",
        welcomeBack: "Mirësevini!",
        signInToContinue: "Hyni për të vazhduar",
        signUpToGetStarted: "Regjistrohu për të filluar",
        notNow: "Jo tani",
        emailRequired: "Email është i detyrueshëm",
        passwordRequired: "Fjalëkalimi është i detyrueshëm",
        passwordsDoNotMatch: "Fjalëkalimet nuk përputhen",
        passwordTooShort: "Fjalëkalimi duhet të ketë të paktën 8 karaktere",
        registerBusiness: "Regjistro Biznesin",
        requiredInformation: "Informacioni i Detyrueshëm",
        businessName: "Emri i Biznesit *",
        locationSection: "Vendndodhja *",
        fullAddress: "Adresa e Plotë *",
        cityLabel: "Qyteti *",
        countryLabel: "Shteti *",
        cityRequired: "Qyteti është i detyrueshëm",
        locatingAddress: "Duke gjetur koordinatat…",
        geocodeFailed: "Nuk u gjet ky adresë — ju lutem kontrolloni nëse është e saktë",
        latitude: "Gjerësia *",
        longitude: "Gjatësia *",
        pickLocationFromMap: "Zgjidhni Vendndodhjen nga Harta",
        contactInformation: "Informacioni i Kontaktit",
        phoneNumber: "Numri i Telefonit *",
        emailOptional: "Email (Opsional)",
        websiteOptional: "Faqja Web (Opsionale)",
        workingHoursSection: "Orari i Punës",
        open247: "Hapur 24/7",
        photoOptional: "Foto (Opsionale)",
        photoAdded: "Foto u Shtua (1/1)",
        registering: "Duke Regjistruar...",
        registerBusinessButton: "Regjistro Biznesin",
        businessNameRequired: "Emri i biznesit është i detyrueshëm",
        selectCategory: "Ju lutemi zgjidhni një kategori",
        descriptionRequired: "Përshkrimi është i detyrueshëm",
        addressRequired: "Adresa është e detyrueshme",
        phoneRequired: "Numri i telefonit është i detyrueshëm",
        validCoordinates: "Ju lutemi vendosni koordinata të vlefshme",
        businessRegistered: "Biznesi u regjistrua me sukses!",
        cameraPermissionRequired: "Leja e kamerës është e nevojshme",
        gallery: "Galeria",
        camera: "Kamera",
        choosePhotoSource: "Zgjidhni burimin e fotos"
    )
    
    public static func forLanguage(_ lang: AppLanguage) -> AppStrings {
        switch lang {
        case .en: return .english
        case .sq: return .albanian
        }
    }
}

public struct AppStringsKey: EnvironmentKey {
    public static let defaultValue: AppStrings = .english
}

extension EnvironmentValues {
    public var appStrings: AppStrings {
        get { self[AppStringsKey.self] }
        set { self[AppStringsKey.self] = newValue }
    }
}

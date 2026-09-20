// Bismillah Hir Rahman Nir Raheem
import Foundation
import SwiftUI

public struct AppStrings {
    // General
    public var appName: String
    public var all: String
    public var topRecommended: String
    public var nearYou: String
    public var communityAnnouncements: String
    public var mostFavoritedWorldwide: String
    public var noUpcomingEventsShort: String
    public var noBusinessesYetHome: String
    public var noBusinessesNearYou: String
    public var appGrowingMessage: String
    public var seeMore: String
    public var seeLess: String
    public var save: String
    public var cancel: String
    public var back: String
    public var loading: String
    public var noResults: String
    public var welcomeUser: String
    
    // Auth
    public var welcomeTitle: String
    public var welcomeDesc: String
    public var appTagline: String
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
    public var allFilterOption: String
    public var applyFilters: String
    public var getDirections: String
    public var nearMe: String
    
    // Business Detail
    public var recentReviews: String
    public var noReviewsYet: String
    public var writeReview: String
    public var writeReplyPlaceholder: String
    public var reply: String
    public var viewReplies: String
    public var hideReplies: String
    public var editReviewTitle: String
    public var deleteReviewConfirmTitle: String
    public var deleteReviewConfirmMessage: String
    public var deleteReviewButton: String
    public var reviewUpdated: String
    public var reviewUpdateFailed: String
    public var reviewDeleted: String
    public var reviewDeleteFailed: String
    public var editReplyTitle: String
    public var replyUpdated: String
    public var replyUpdateFailed: String
    public var deleteReplyConfirmTitle: String
    public var deleteReplyConfirmMessage: String
    public var deleteReplyButton: String
    public var replyDeleted: String
    public var replyDeleteFailed: String
    public var editBusiness: String
    public var upgradePremium: String
    public var viewPlans: String
    public var photos: String
    public var workingHours: String
    public var promotions: String
    public var jobs: String
    public var jobsEmptyTitle: String
    public var jobsEmptySubtitle: String
    public var viewProfile: String
    public var readMore: String
    public var readLess: String
    public var category: String
    public var verified: String
    public var albanianOwned: String
    public var albanianOwnedQuestion: String
    public var claimThisBusiness: String
    public var claimBusinessDialogDescription: String
    public var claimReasonLabel: String
    public var claimReasonPlaceholder: String
    public var submitClaimButton: String
    public var claimSubmittedSuccess: String
    public var claimSubmitFailed: String
    public var claimReasonRequired: String
    public var claimAlreadySubmitted: String
    public var getVerifiedTitle: String
    public var requestVerification: String
    public var requestVerificationDescription: String
    public var requestVerificationDialogDescription: String
    public var verificationRequestSubmitted: String
    public var verificationAlreadySubmitted: String
    public var adminBusinessClaims: String
    public var adminVerificationRequests: String
    public var adminBusinessesAndPlans: String
    public var adminNoPendingVerification: String
    public var adminAllVerificationProcessed: String
    public var adminNoActivePlans: String
    public var adminTableBusinessColumn: String
    public var adminTableTierColumn: String
    public var adminTableExpiresColumn: String
    public var adminNoExpiry: String
    public var businessActiveStatus: String
    public var businessActiveDescription: String
    public var businessInactiveDescription: String
    public var inactiveLabel: String
    public var clearExpiredPlansButton: String
    public var clearExpiredPlansNoneFound: String
    public var clearExpiredPlansSuccessTemplate: String
    public var clearExpiredPlansFailedPrefix: String
    public var expiredLabel: String
    public var importSampleBusinesses: String
    public var noPendingClaims: String
    public var allClaimsProcessed: String
    public var approveClaimTitle: String
    public var approveClaimMessage: String
    public var approve: String
    public var rejectClaimTitle: String
    public var rejectClaimMessage: String
    public var reject: String
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
    public var couldntOpenLink: String
    
    // Reviews
    public var rateThisBusiness: String
    public var tapStarToRate: String
    public var shareExperience: String
    public var submitReview: String
    public var submitting: String
    public var addStoryTitle: String
    public var addPhotosButton: String
    public var captionPlaceholder: String
    public var storyLocationPlaceholder: String
    public var postStory: String
    
    // Subscription
    public var choosePlan: String
    public var upgradeYourListing: String
    public var currentPlan: String
    public var requestUpgrade: String
    public var requestSponsorship: String
    public var requestFeatured: String
    public var manualPaymentNote: String
    public var freeTierName: String
    public var forever: String
    public var perMonth: String
    public var currentPlanButton: String
    public var notAvailableDash: String
    public var upgradeTitleTemplate: String
    public var planFeatureNameCategory: String
    public var planFeatureLocationOnMap: String
    public var planFeature100CharDesc: String
    public var planFeature1Photo: String
    public var planFeatureEverythingPremium: String
    public var planFeatureUp6Photos: String
    public var planFeaturePhoneNumber: String
    public var planFeatureEmailWebsite: String
    public var planFeaturePremiumBadge: String
    public var planFeatureUp10Photos: String
    public var planFeatureFeaturedBadge: String
    public var planFeatureFeaturedDiscoveryRow: String
    public var planFeatureUp14Photos: String
    public var planFeatureHighlightedMapPin: String
    public var planFeatureTopSearchResults: String
    public var planFeatureSponsoredBadge: String
    
    // Favorites
    public var myFavorites: String
    public var noFavoritesYet: String
    public var myBusinesses: String
    public var noBusinessesYet: String
    public var noBusinessesYetSubtitle: String
    public var myBusinessesSubtitle: String
    public var myEvents: String
    public var myEventsSubtitle: String
    public var openButton: String
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
    public var failedToUploadPhoto: String
    public var latitude: String
    public var longitude: String
    public var pickLocationFromMap: String
    public var contactInformation: String
    public var phoneNumber: String
    public var emailOptional: String
    public var websiteOptional: String
    public var workingHoursSection: String
    public var open247: String
    public var closedLabel: String
    public var hoursOpenLabel: String
    public var hoursCloseLabel: String
    public var ok: String
    public var photoOptional: String
    public var photoAdded: String
    public var uploadingPhoto: String
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
    public var editBusinessTitle: String
    public var basicInformationSection: String
    public var locationSectionShort: String
    public var saveChanges: String
    public var businessUpdatedSuccess: String
    public var categoryRequiredLabel: String
    public var descriptionRequiredLabel: String
    public var photoLimitReached: String
    public var addJobPostingTitle: String
    public var jobTitleLabel: String
    public var jobTypeLabel: String
    public var jobSalaryLabel: String
    public var jobSalaryPlaceholder: String
    public var addJobButton: String
    public var jobTitleDescRequired: String
    public var addPromotionTitle: String
    public var promotionTitleLabel: String
    public var savingLabel: String
    public var promotionsEmptyTitle: String
    public var unexpectedErrorPrefix: String
    public var failedToUploadPhotos: String
    public var failedToUpdateBusiness: String
    public var promotionDiscountCodeLabel: String
    public var promotionExpirySection: String
    public var promotionInvalidExpiry: String
    public var promotionCodePrefix: String
    public var promotionExpiresPrefix: String
    public var dayLabel: String
    public var dayPlaceholder: String
    public var monthLabel: String
    public var monthPlaceholder: String
    public var yearLabel: String
    public var yearPlaceholder: String
    public var noEventsYet: String
    public var noEventsYetSubtitle: String
    public var deleteEvent: String
    public var deleteEventConfirmTitle: String
    public var deleteEventConfirmMessage: String
    public var eventDeleted: String
    public var eventDeleteFailed: String
    public var maxPhotosPerReview: String
    public var loginRequiredForReview: String
    
    public static let english = AppStrings(
        appName: "MeTont",
        all: "All",
        topRecommended: "Top Recommended",
        nearYou: "Near You",
        communityAnnouncements: "Community Announcements",
        mostFavoritedWorldwide: "Most Favorited Worldwide",
        noUpcomingEventsShort: "No upcoming events",
        noBusinessesYetHome: "No businesses yet",
        noBusinessesNearYou: "No businesses near you",
        appGrowingMessage: "The app is still growing in your area",
        seeMore: "See more",
        seeLess: "See less",
        save: "Save",
        cancel: "Cancel",
        back: "Back",
        loading: "Loading...",
        noResults: "No businesses found",
        welcomeUser: "Welcome",
        welcomeTitle: "Welcome to Albanian Business App",
        welcomeDesc: "Register your business, or explore the Albanian business map in your area and beyond.",
        appTagline: "Albanian Business Directory",
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
        allFilterOption: "All",
        applyFilters: "Apply Filters",
        getDirections: "Get Directions",
        nearMe: "Near Me",
        recentReviews: "Recent Reviews",
        noReviewsYet: "No reviews yet. Be the first!",
        writeReview: "Review",
        writeReplyPlaceholder: "Write a reply...",
        reply: "Reply",
        viewReplies: "View replies",
        hideReplies: "Hide replies",
        editReviewTitle: "Edit Review",
        deleteReviewConfirmTitle: "Delete Review",
        deleteReviewConfirmMessage: "Are you sure you want to delete this review? This can't be undone.",
        deleteReviewButton: "Delete",
        reviewUpdated: "Review updated",
        reviewUpdateFailed: "Failed to update review",
        reviewDeleted: "Review deleted",
        reviewDeleteFailed: "Failed to delete review",
        editReplyTitle: "Edit Reply",
        replyUpdated: "Reply updated",
        replyUpdateFailed: "Failed to update reply",
        deleteReplyConfirmTitle: "Delete Reply",
        deleteReplyConfirmMessage: "Are you sure you want to delete this reply? This can't be undone.",
        deleteReplyButton: "Delete",
        replyDeleted: "Reply deleted",
        replyDeleteFailed: "Failed to delete reply",
        editBusiness: "Edit",
        upgradePremium: "Upgrade to Premium to unlock contact info, website, photos and more for just $2.99/month",
        viewPlans: "View Plans",
        photos: "Photos",
        workingHours: "Working Hours",
        promotions: "Promotions & Deals",
        jobs: "Job Postings",
        jobsEmptyTitle: "No job postings yet",
        jobsEmptySubtitle: "Check back soon for new openings",
        viewProfile: "View Profile",
        readMore: "Read more",
        readLess: "Read less",
        category: "Category",
        verified: "Verified",
        albanianOwned: "Albanian Owned",
        albanianOwnedQuestion: "Is this business Albanian-owned?",
        claimThisBusiness: "Claim This Business",
        claimBusinessDialogDescription: "Tell us why you're the owner of this business. Our team will review your request.",
        claimReasonLabel: "Reason",
        claimReasonPlaceholder: "e.g. I run this business, here's how you can verify it...",
        submitClaimButton: "Submit Claim",
        claimSubmittedSuccess: "Claim request submitted! We'll review it soon.",
        claimSubmitFailed: "Failed to submit claim",
        claimReasonRequired: "Please enter a reason",
        claimAlreadySubmitted: "Claim submitted — pending review",
        getVerifiedTitle: "Get Verified",
        requestVerification: "Request Verification",
        requestVerificationDescription: "Get a Verified badge on your business so customers know it's the real deal.",
        requestVerificationDialogDescription: "Tell us a bit about your business — this helps our team verify it faster.",
        verificationRequestSubmitted: "Verification request submitted! We'll review it soon.",
        verificationAlreadySubmitted: "Verification requested — pending review",
        adminBusinessClaims: "Business Claims",
        adminVerificationRequests: "Verification Requests",
        adminBusinessesAndPlans: "Businesses & Plans",
        adminNoPendingVerification: "No pending verification requests",
        adminAllVerificationProcessed: "All verification requests processed",
        adminNoActivePlans: "No businesses on a paid plan yet",
        adminTableBusinessColumn: "Business",
        adminTableTierColumn: "Tier",
        adminTableExpiresColumn: "Expires",
        adminNoExpiry: "—",
        businessActiveStatus: "Business is Active",
        businessActiveDescription: "Your business is visible on the map and in search.",
        businessInactiveDescription: "Your business is hidden from the map and search. Turn this back on anytime.",
        inactiveLabel: "Inactive",
        clearExpiredPlansButton: "Clear Expired Plans",
        clearExpiredPlansNoneFound: "No expired plans to clear.",
        clearExpiredPlansSuccessTemplate: "Cleared %d expired plan(s).",
        clearExpiredPlansFailedPrefix: "Failed to clear expired plans",
        expiredLabel: "Expired",
        importSampleBusinesses: "Import Sample Businesses",
        noPendingClaims: "No pending claims!",
        allClaimsProcessed: "All claim requests have been processed.",
        approveClaimTitle: "Approve Claim",
        approveClaimMessage: "Are you sure you want to approve %@'s claim for \"%@\"? This will transfer ownership and verify the business.",
        approve: "Approve",
        rejectClaimTitle: "Reject Claim",
        rejectClaimMessage: "Are you sure you want to reject this claim request from %@?",
        reject: "Reject",
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
        couldntOpenLink: "Couldn't open link",
        rateThisBusiness: "Rate this business",
        tapStarToRate: "Tap a star to select a rating",
        shareExperience: "Share your experience...",
        submitReview: "Submit Review",
        submitting: "Submitting...",
        addStoryTitle: "Add Story",
        addPhotosButton: "Add Photos",
        captionPlaceholder: "Write a caption...",
        storyLocationPlaceholder: "e.g. Tirana, Albania",
        postStory: "Post Story",
        choosePlan: "Choose Your Plan",
        upgradeYourListing: "Upgrade Your Listing",
        currentPlan: "CURRENT",
        requestUpgrade: "Request Upgrade",
        requestSponsorship: "Request Sponsorship",
        requestFeatured: "Request Featured",
        manualPaymentNote: "Payments are currently processed manually. We will contact you within 24 hours of your request.",
        freeTierName: "Free",
        forever: "forever",
        perMonth: "per month",
        currentPlanButton: "Current Plan",
        notAvailableDash: "—",
        upgradeTitleTemplate: "Upgrade \"%@\"",
        planFeatureNameCategory: "Business name & category",
        planFeatureLocationOnMap: "Location on map",
        planFeature100CharDesc: "100 character description",
        planFeature1Photo: "1 photo",
        planFeatureEverythingPremium: "Everything in Premium",
        planFeatureUp6Photos: "Up to 6 photos",
        planFeaturePhoneNumber: "Phone number",
        planFeatureEmailWebsite: "Email & website",
        planFeaturePremiumBadge: "Premium badge",
        planFeatureUp10Photos: "Up to 10 photos",
        planFeatureFeaturedBadge: "Featured badge",
        planFeatureFeaturedDiscoveryRow: "Featured in discovery row",
        planFeatureUp14Photos: "Up to 14 photos",
        planFeatureHighlightedMapPin: "Highlighted map pin",
        planFeatureTopSearchResults: "Top of search results",
        planFeatureSponsoredBadge: "Sponsored badge",
        myFavorites: "My Favorites",
        noFavoritesYet: "You haven't saved any businesses yet.",
        myBusinesses: "My Businesses",
        noBusinessesYet: "You don't have any businesses yet",
        noBusinessesYetSubtitle: "Add your business to reach more customers",
        myBusinessesSubtitle: "Manage your business listings",
        myEvents: "My Events",
        myEventsSubtitle: "Manage your events",
        openButton: "Open",
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
        failedToUploadPhoto: "Couldn't upload photo — please try again",
        latitude: "Latitude *",
        longitude: "Longitude *",
        pickLocationFromMap: "Pick Location from Map",
        contactInformation: "Contact Information",
        phoneNumber: "Phone Number *",
        emailOptional: "Email (Optional)",
        websiteOptional: "Website (Optional)",
        workingHoursSection: "Working Hours",
        open247: "Open 24/7",
        closedLabel: "Closed",
        hoursOpenLabel: "Open",
        hoursCloseLabel: "Close",
        ok: "OK",
        photoOptional: "Photo (Optional)",
        photoAdded: "Photo Added (1/1)",
        uploadingPhoto: "Uploading photo...",
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
        choosePhotoSource: "Choose photo source",
        editBusinessTitle: "Edit Business",
        basicInformationSection: "Basic Information",
        locationSectionShort: "Location",
        saveChanges: "Save Changes",
        businessUpdatedSuccess: "Business updated successfully!",
        categoryRequiredLabel: "Category *",
        descriptionRequiredLabel: "Description *",
        photoLimitReached: "Photo limit reached for your plan",
        addJobPostingTitle: "Add Job Posting",
        jobTitleLabel: "Job Title *",
        jobTypeLabel: "Job Type *",
        jobSalaryLabel: "Salary (Optional)",
        jobSalaryPlaceholder: "e.g. $1,500/month",
        addJobButton: "Add Job",
        jobTitleDescRequired: "Title and description are required",
        addPromotionTitle: "Add Promotion",
        promotionTitleLabel: "Promotion Title *",
        savingLabel: "Saving...",
        promotionsEmptyTitle: "No promotions yet.",
        unexpectedErrorPrefix: "Unexpected error",
        failedToUploadPhotos: "Failed to upload photos",
        failedToUpdateBusiness: "Failed to update business",
        promotionDiscountCodeLabel: "Discount Code (Optional)",
        promotionExpirySection: "Expiry Date (Optional)",
        promotionInvalidExpiry: "Please enter a valid expiry date",
        promotionCodePrefix: "Code: ",
        promotionExpiresPrefix: "Expires: ",
        dayLabel: "Day",
        dayPlaceholder: "DD",
        monthLabel: "Month",
        monthPlaceholder: "MM",
        yearLabel: "Year",
        yearPlaceholder: "YYYY",
        noEventsYet: "You don't have any events yet",
        noEventsYetSubtitle: "Submit an event to reach more people",
        deleteEvent: "Delete Event",
        deleteEventConfirmTitle: "Delete this event?",
        deleteEventConfirmMessage: "This will permanently remove the event. This action cannot be undone.",
        eventDeleted: "Event deleted",
        eventDeleteFailed: "Failed to delete event",
        maxPhotosPerReview: "Maximum 5 photos per review",
        loginRequiredForReview: "You must be logged in to submit a review"
    )
    
    public static let albanian = AppStrings(
        appName: "MeTont",
        all: "Të gjitha",
        topRecommended: "Më të Rekomanduarat",
        nearYou: "Afër Jush",
        communityAnnouncements: "Njoftime nga Komuniteti",
        mostFavoritedWorldwide: "Më të Preferuarat Botërisht",
        noUpcomingEventsShort: "Nuk ka evente të ardhshme",
        noBusinessesYetHome: "Ende nuk ka biznese",
        noBusinessesNearYou: "Nuk ka biznese afër jush",
        appGrowingMessage: "Aplikacioni po rritet ende në zonën tuaj",
        seeMore: "Më shumë",
        seeLess: "Më pak",
        save: "Ruaj",
        cancel: "Anulo",
        back: "Kthehu",
        loading: "Duke ngarkuar...",
        noResults: "Nuk u gjet asnjë biznes",
        welcomeUser: "Mirë se vini",
        welcomeTitle: "Mirësevini në MeTont",
        welcomeDesc: "Regjistroni biznesin tuaj, ose eksploroni hartën e bizneseve shqiptare në zonën tuaj dhe më gjerë.",
        appTagline: "Direktoria e Bizneseve Shqiptare",
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
        allFilterOption: "Të gjitha",
        applyFilters: "Apliko Filtrat",
        getDirections: "Drejtimet",
        nearMe: "Pranë Meje",
        recentReviews: "Vlerësimet e Fundit",
        noReviewsYet: "Asnjë vlerësim ende. Bëhu i pari!",
        writeReview: "Vlerëso",
        writeReplyPlaceholder: "Shkruaj një përgjigje...",
        reply: "Përgjigju",
        viewReplies: "Shiko përgjigjet",
        hideReplies: "Fshih përgjigjet",
        editReviewTitle: "Ndrysho Vlerësimin",
        deleteReviewConfirmTitle: "Fshi Vlerësimin",
        deleteReviewConfirmMessage: "A jeni i sigurt që doni ta fshini këtë vlerësim? Kjo nuk mund të zhbëhet.",
        deleteReviewButton: "Fshij",
        reviewUpdated: "Vlerësimi u përditësua",
        reviewUpdateFailed: "Përditësimi i vlerësimit dështoi",
        reviewDeleted: "Vlerësimi u fshi",
        reviewDeleteFailed: "Fshirja e vlerësimit dështoi",
        editReplyTitle: "Ndrysho Përgjigjen",
        replyUpdated: "Përgjigja u përditësua",
        replyUpdateFailed: "Përditësimi i përgjigjes dështoi",
        deleteReplyConfirmTitle: "Fshi Përgjigjen",
        deleteReplyConfirmMessage: "A jeni i sigurt që doni ta fshini këtë përgjigje? Kjo nuk mund të zhbëhet.",
        deleteReplyButton: "Fshij",
        replyDeleted: "Përgjigja u fshi",
        replyDeleteFailed: "Fshirja e përgjigjes dështoi",
        editBusiness: "Ndrysho",
        upgradePremium: "Kaloni në Premium për të zhbllokuar kontaktet, faqen dhe fotot për vetëm $2.99/muaj",
        viewPlans: "Shiko Planet",
        photos: "Foto",
        workingHours: "Orari i Punës",
        promotions: "Promocione dhe Oferta",
        jobs: "Oferta Pune",
        jobsEmptyTitle: "Ende nuk ka oferta pune",
        jobsEmptySubtitle: "Kontrolloni së shpejti për vende të reja pune",
        viewProfile: "Shiko Profilin",
        readMore: "Lexo më shumë",
        readLess: "Lexo më pak",
        category: "Kategoria",
        verified: "Verifikuar",
        albanianOwned: "Pronë Shqiptare",
        albanianOwnedQuestion: "A është ky biznes në pronësi shqiptare?",
        claimThisBusiness: "Kërko Pronësinë e Biznesit",
        claimBusinessDialogDescription: "Na tregoni pse jeni pronari i këtij biznesi. Ekipi ynë do ta shqyrtojë kërkesën tuaj.",
        claimReasonLabel: "Arsyeja",
        claimReasonPlaceholder: "p.sh. unë e drejtoj këtë biznes, ja si mund ta verifikoni...",
        submitClaimButton: "Dërgo Kërkesën",
        claimSubmittedSuccess: "Kërkesa u dërgua! Do ta shqyrtojmë së shpejti.",
        claimSubmitFailed: "Dërgimi i kërkesës dështoi",
        claimReasonRequired: "Ju lutemi shkruani një arsye",
        claimAlreadySubmitted: "Kërkesa u dërgua — në pritje të shqyrtimit",
        getVerifiedTitle: "Bëhu i Verifikuar",
        requestVerification: "Kërko Verifikim",
        requestVerificationDescription: "Merr distinktivin Verifikuar për biznesin tënd që klientët ta dinë se është i vërtetë.",
        requestVerificationDialogDescription: "Na tregoni pak për biznesin tuaj — kjo na ndihmon ta verifikojmë më shpejt.",
        verificationRequestSubmitted: "Kërkesa për verifikim u dërgua! Do ta shqyrtojmë së shpejti.",
        verificationAlreadySubmitted: "Verifikimi u kërkua — në pritje të shqyrtimit",
        adminBusinessClaims: "Kërkesa për Pronësi",
        adminVerificationRequests: "Kërkesa për Verifikim",
        adminBusinessesAndPlans: "Bizneset & Planet",
        adminNoPendingVerification: "Nuk ka kërkesa verifikimi në pritje",
        adminAllVerificationProcessed: "Të gjitha kërkesat për verifikim janë shqyrtuar",
        adminNoActivePlans: "Ende asnjë biznes me plan me pagesë",
        adminTableBusinessColumn: "Biznesi",
        adminTableTierColumn: "Plani",
        adminTableExpiresColumn: "Skadon",
        adminNoExpiry: "—",
        businessActiveStatus: "Biznesi Aktiv",
        businessActiveDescription: "Biznesi juaj është i dukshëm në hartë dhe në kërkim.",
        businessInactiveDescription: "Biznesi juaj është i fshehur nga harta dhe kërkimi. Mund ta riaktivizoni kur të doni.",
        inactiveLabel: "Joaktiv",
        clearExpiredPlansButton: "Pastro Planet e Skaduara",
        clearExpiredPlansNoneFound: "Nuk ka plane të skaduara për të pastruar.",
        clearExpiredPlansSuccessTemplate: "U pastruan %d plan(e) të skaduara.",
        clearExpiredPlansFailedPrefix: "Pastrimi i planeve të skaduara dështoi",
        expiredLabel: "Skaduar",
        importSampleBusinesses: "Importo Biznese Shembull",
        noPendingClaims: "Nuk ka kërkesa në pritje!",
        allClaimsProcessed: "Të gjitha kërkesat janë përpunuar.",
        approveClaimTitle: "Mirato Kërkesën",
        approveClaimMessage: "Jeni i sigurt që doni të miratoni kërkesën e %@ për \"%@\"? Kjo do të transferojë pronësinë dhe do të verifikojë biznesin.",
        approve: "Mirato",
        rejectClaimTitle: "Refuzo Kërkesën",
        rejectClaimMessage: "Jeni i sigurt që doni të refuzoni këtë kërkesë nga %@?",
        reject: "Refuzo",
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
        couldntOpenLink: "Nuk mund të hapej lidhja",
        rateThisBusiness: "Vlerëso këtë biznes",
        tapStarToRate: "Trokitni një yll për të vlerësuar",
        shareExperience: "Ndani përvojën tuaj...",
        submitReview: "Dërgo Vlerësimin",
        submitting: "Duke dërguar...",
        addStoryTitle: "Shto Histori",
        addPhotosButton: "Shto Foto",
        captionPlaceholder: "Shkruani një përshkrim...",
        storyLocationPlaceholder: "p.sh. Tiranë, Shqipëri",
        postStory: "Posto Historinë",
        choosePlan: "Zgjidhni Planin Tuaj",
        upgradeYourListing: "Përmirësoni Listimin Tuaj",
        currentPlan: "AKTUAL",
        requestUpgrade: "Kërkoni Përmirësim",
        requestSponsorship: "Kërkoni Sponsorizim",
        requestFeatured: "Kërkoni të Veçohet",
        manualPaymentNote: "Pagesat aktualisht procesohen manualisht. Do t'ju kontaktojmë brenda 24 orëve.",
        freeTierName: "Falas",
        forever: "përgjithmonë",
        perMonth: "në muaj",
        currentPlanButton: "Plani Aktual",
        notAvailableDash: "—",
        upgradeTitleTemplate: "Përmirëso \"%@\"",
        planFeatureNameCategory: "Emri dhe kategoria e biznesit",
        planFeatureLocationOnMap: "Vendndodhja në hartë",
        planFeature100CharDesc: "Përshkrim 100 karaktere",
        planFeature1Photo: "1 foto",
        planFeatureEverythingPremium: "Gjithçka në Premium",
        planFeatureUp6Photos: "Deri në 6 foto",
        planFeaturePhoneNumber: "Numri i telefonit",
        planFeatureEmailWebsite: "Email dhe faqja web",
        planFeaturePremiumBadge: "Distinktivi Premium",
        planFeatureUp10Photos: "Deri në 10 foto",
        planFeatureFeaturedBadge: "Distinktivi i Veçuar",
        planFeatureFeaturedDiscoveryRow: "I veçuar në rreshtin e zbulimit",
        planFeatureUp14Photos: "Deri në 14 foto",
        planFeatureHighlightedMapPin: "Shenjë e theksuar në hartë",
        planFeatureTopSearchResults: "Në krye të rezultateve të kërkimit",
        planFeatureSponsoredBadge: "Distinktivi Sponsorizuar",
        myFavorites: "Të Preferuarat e Mia",
        noFavoritesYet: "Nuk keni ruajtur asnjë biznes ende.",
        myBusinesses: "Bizneset e Mia",
        noBusinessesYet: "Ende nuk keni asnjë biznes",
        noBusinessesYetSubtitle: "Shtoni biznesin tuaj për të arritur më shumë klientë",
        myBusinessesSubtitle: "Menaxhoni listimet e biznesit tuaj",
        myEvents: "Ngjarjet e Mia",
        myEventsSubtitle: "Menaxhoni ngjarjet tuaja",
        openButton: "Hap",
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
        failedToUploadPhoto: "Nuk u ngarkua dot fotoja — provoni përsëri",
        latitude: "Gjerësia *",
        longitude: "Gjatësia *",
        pickLocationFromMap: "Zgjidhni Vendndodhjen nga Harta",
        contactInformation: "Informacioni i Kontaktit",
        phoneNumber: "Numri i Telefonit *",
        emailOptional: "Email (Opsional)",
        websiteOptional: "Faqja Web (Opsionale)",
        workingHoursSection: "Orari i Punës",
        open247: "Hapur 24/7",
        closedLabel: "Mbyllur",
        hoursOpenLabel: "Hapet",
        hoursCloseLabel: "Mbyllet",
        ok: "Në rregull",
        photoOptional: "Foto (Opsionale)",
        photoAdded: "Foto u Shtua (1/1)",
        uploadingPhoto: "Duke ngarkuar foton...",
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
        choosePhotoSource: "Zgjidhni burimin e fotos",
        editBusinessTitle: "Ndrysho Biznesin",
        basicInformationSection: "Informacion Bazë",
        locationSectionShort: "Vendndodhja",
        saveChanges: "Ruaj Ndryshimet",
        businessUpdatedSuccess: "Biznesi u përditësua me sukses!",
        categoryRequiredLabel: "Kategoria *",
        descriptionRequiredLabel: "Përshkrimi *",
        photoLimitReached: "Limiti i fotove u arrit për planin tuaj",
        addJobPostingTitle: "Shto Vend Pune",
        jobTitleLabel: "Titulli i Punës *",
        jobTypeLabel: "Lloji i Punës *",
        jobSalaryLabel: "Paga (Opsionale)",
        jobSalaryPlaceholder: "p.sh. 1,500$/muaj",
        addJobButton: "Shto Punë",
        jobTitleDescRequired: "Titulli dhe përshkrimi janë të detyrueshëm",
        addPromotionTitle: "Shto Promocion",
        promotionTitleLabel: "Titulli i Promocionit *",
        savingLabel: "Duke ruajtur...",
        promotionsEmptyTitle: "Ende nuk ka promocione.",
        unexpectedErrorPrefix: "Gabim i papritur",
        failedToUploadPhotos: "Ngarkimi i fotove dështoi",
        failedToUpdateBusiness: "Përditësimi i biznesit dështoi",
        promotionDiscountCodeLabel: "Kodi i Zbritjes (Opsionale)",
        promotionExpirySection: "Data e Skadimit (Opsionale)",
        promotionInvalidExpiry: "Ju lutemi vendosni një datë skadimi të vlefshme",
        promotionCodePrefix: "Kodi: ",
        promotionExpiresPrefix: "Skadon: ",
        dayLabel: "Dita",
        dayPlaceholder: "DD",
        monthLabel: "Muaji",
        monthPlaceholder: "MM",
        yearLabel: "Viti",
        yearPlaceholder: "VVVV",
        noEventsYet: "Ende nuk keni asnjë event",
        noEventsYetSubtitle: "Shtoni një event për të arritur më shumë njerëz",
        deleteEvent: "Fshi Eventin",
        deleteEventConfirmTitle: "Fshi këtë event?",
        deleteEventConfirmMessage: "Kjo do ta heqë përgjithmonë eventin. Ky veprim nuk mund të zhbëhet.",
        eventDeleted: "Eventi u fshi",
        eventDeleteFailed: "Fshirja e eventit dështoi",
        maxPhotosPerReview: "Maksimumi 5 foto për vlerësim",
        loginRequiredForReview: "Duhet të jeni të kyçur për të dërguar një vlerësim"
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

// Bismillah Hir Rahman Nir Raheem
package com.albbiz.map.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf

enum class AppLanguage { EN, SQ }

interface AppStrings {
    // General
    val appName: String
    val save: String
    val cancel: String
    val back: String
    val loading: String
    val noResults: String
    val welcomeUser: String

    // Auth
    val welcomeTitle: String
    val welcomeDesc: String
    val getStarted: String
    val signIn: String
    val signUp: String
    val email: String
    val password: String
    val confirmPassword: String
    val logout: String
    val noAccount: String
    val haveAccount: String
    val forgotPassword: String
    val resetPasswordTitle: String
    val resetPasswordDescription: String
    val sendResetLink: String
    val resetEmailSent: String
    val continueWithGoogle: String
    val continueWithApple: String
    // Map
    val searchPlaceholder: String
    val addBusiness: String
    val listView: String
    val favorites: String
    val profile: String
    val communityEvents: String
    val viewDetailsAndRate: String
    // Business List
    val directory: String
    val featured: String
    val recentlyAdded: String
    val topRated: String
    val allBusinesses: String
    val getDirections: String
    val nearMe: String
    // Business Detail
    val recentReviews: String
    val noReviewsYet: String
    val writeReview: String
    val editBusiness: String
    val upgradePremium: String
    val viewPlans: String
    val photos: String
    val workingHours: String
    val promotions: String
    val jobs: String
    val readMore: String
    val readLess: String
    val category: String
    val verified: String
    val albanianOwned: String
    val premium: String
    val featured2: String
    val sponsored: String
    // Profile
    val myProfile: String
    val personalInformation: String
    val firstName: String
    val lastName: String
    val saveProfile: String
    val upgradeToPremium: String
    // Events
    val communityEventsTitle: String
    val noEventsFound: String
    val submitEvent: String
    val eventTitle: String
    val eventDescription: String
    val eventDate: String
    val eventLocation: String
    val eventWebsite: String
    val eventCategory: String
    val addPhoto: String
    val changePhoto: String
    val promoted: String
    val viewEventWebsite: String
    // Reviews
    val rateThisBusiness: String
    val tapStarToRate: String
    val shareExperience: String
    val submitReview: String
    val submitting: String
    // Subscription
    val choosePlan: String
    val upgradeYourListing: String
    val currentPlan: String
    val requestUpgrade: String
    val requestSponsorship: String
    val requestFeatured: String
    val manualPaymentNote: String
    // Favorites
    val myFavorites: String
    val noFavoritesYet: String
    val myBusinesses: String
    val noBusinessesYet: String
    val noBusinessesYetSubtitle: String
    val myBusinessesSubtitle: String
    val myEvents: String
    val noEventsYet: String
    val noEventsYetSubtitle: String
    val myEventsSubtitle: String
    val deleteEvent: String
    val deleteEventConfirmTitle: String
    val deleteEventConfirmMessage: String
    val eventDeleted: String
    val eventDeleteFailed: String
    val noSearchResults: String
    val upgradePremiumTitle: String

    val firstNameRequired: String
    val profileSaved: String
    val profileSaveFailed: String

    // In AppStrings
    val eventPhotoSection: String
    val eventDetailsSection: String
    val eventDateSection: String
    val eventLocationSection: String

    val eventTitleRequired: String
    val eventDescriptionRequired: String
    val eventLocationRequired: String
    val eventInvalidDate: String
    val eventSubmitSuccess: String
    val eventSubmitFailed: String
    val submitEventButton: String

    val writeReviewLabel: String
    val pleaseSelectRating: String
    val pleaseWriteReview: String
    val reviewSubmitted: String

    val welcomeBack: String
    val signInToContinue: String
    val signUpToGetStarted: String
    val notNow: String

    val emailRequired: String
    val passwordRequired: String
    val passwordsDoNotMatch: String
    val passwordTooShort: String

    val registerBusiness: String
    val requiredInformation: String
    val businessName: String
    val locationSection: String
    val fullAddress: String
    val cityLabel: String
    val countryLabel: String
    val cityRequired: String
    val locatingAddress: String
    val geocodeFailed: String
    val latitude: String
    val longitude: String
    val pickLocationFromMap: String
    val contactInformation: String
    val phoneNumber: String
    val emailOptional: String
    val websiteOptional: String
    val workingHoursSection: String
    val open247: String
    val photoOptional: String
    val photoAdded: String
    val registering: String
    val registerBusinessButton: String
    val businessNameRequired: String
    val selectCategory: String
    val descriptionRequired: String
    val addressRequired: String
    val phoneRequired: String
    val validCoordinates: String
    val businessRegistered: String
    val cameraPermissionRequired: String
    val gallery: String
    val camera: String
    val choosePhotoSource: String

    // Generic dialog buttons
    val ok: String
    val confirm: String
    val openButton: String

    // Time picker / working hours
    val hoursOpenLabel: String
    val hoursCloseLabel: String

    // Add Event date fields
    val dayLabel: String
    val dayPlaceholder: String
    val monthLabel: String
    val monthPlaceholder: String
    val yearLabel: String
    val yearPlaceholder: String
    val eventStartTimeLabel: String

    // Edit Business screen
    val basicInformationSection: String
    val locationSectionShort: String
    val saveChanges: String
    val businessUpdatedSuccess: String
    val categoryRequiredLabel: String
    val descriptionRequiredLabel: String
    val photoLimitReached: String
    val addJobPostingTitle: String
    val jobTitleLabel: String
    val jobTypeLabel: String
    val jobSalaryLabel: String
    val jobSalaryPlaceholder: String
    val addJobButton: String
    val jobTitleDescRequired: String
    val addPromotionTitle: String
    val promotionTitleLabel: String

    // Add Story screen
    val addStoryTitle: String
    val addPhotosButton: String
    val captionPlaceholder: String
    val storyLocationPlaceholder: String
    val postStory: String

    // Admin screen
    val importSampleBusinesses: String
    val approveClaimTitle: String
    val approveClaimMessage: String
    val approve: String
    val rejectClaimTitle: String
    val rejectClaimMessage: String
    val reject: String

    // Business detail screen
    val writeReplyPlaceholder: String
    val noLocationSetToast: String
    val googleMapsNotInstalled: String

    // Business list screen
    val applyFilters: String
    val allFilterOption: String

    // Jobs screen
    val viewProfile: String
    val jobsEmptyTitle: String
    val jobsEmptySubtitle: String

    // Map / home screen
    val appTagline: String
    val viewFullProfile: String
    val noBusinessesNearYou: String
    val appGrowingMessage: String
    val communityAnnouncements: String
    val noUpcomingEventsShort: String
    val mostFavoritedWorldwide: String
    val noBusinessesYetHome: String

    // Story viewer
    val viewBusiness: String
    val couldntOpenMaps: String

    // Events screen
    val couldntOpenLink: String

    // Reviews
    val loginRequiredForReview: String

    // Subscription
    val freeTierName: String

    // Edit Business screen (additional)
    val editBusinessTitle: String
    val savingLabel: String
    val promotionsEmptyTitle: String
    val closedLabel: String

    // Subscription screen (plan details)
    val upgradeTitleTemplate: String
    val subscriptionHeaderSubtitle: String
    val perMonth: String
    val forever: String
    val currentPlanButton: String
    val notAvailableDash: String
    val sendUpgradeRequest: String
    val sendFeaturedRequest: String
    val sendSponsorshipRequest: String
    val planFeatureNameCategory: String
    val planFeatureLocationOnMap: String
    val planFeature100CharDesc: String
    val planFeature1Photo: String
    val planFeaturePhoneNumber: String
    val planFeatureEmailWebsite: String
    val planFeatureExtendedDesc: String
    val planFeatureHoursOfOperation: String
    val planFeaturePremiumBadge: String
    val planFeatureUp6Photos: String
    val planFeatureEverythingPremium: String
    val planFeatureUp10Photos: String
    val planFeatureFeaturedBadge: String
    val planFeatureFeaturedDiscoveryRow: String
    val planFeatureHighlightedListView: String
    val planFeatureUp14Photos: String
    val planFeatureHighlightedMapPin: String
    val planFeatureTopSearchResults: String
    val planFeatureSponsoredBadge: String
    val planFeatureFeaturedDiscoverySection: String
    val planFeaturePriorityCustomerSupport: String

    // Admin screen (additional)
    val pendingClaimRequests: String
    val noPendingClaims: String
    val allClaimsProcessed: String
    val pendingCountLabel: String

    // Exception/error messages surfaced from ViewModels and Repositories (no
    // Compose context there, so these are looked up via CurrentLanguage.strings()
    // instead of LocalAppStrings). The raw SDK/Firebase exception text appended
    // after some of these (e.message) can't be localized — it comes from Google's
    // libraries in English regardless of app language.
    val mustBeLoggedInToAddBusiness: String
    val reviewNotFound: String
    val alreadyReportedReview: String
    val replyNotFound: String
    val reviewReportedSuccess: String
    val failedToAddBusinessPrefix: String
    val failedToUploadImagesPrefix: String
    val unexpectedErrorPrefix: String
    val failedToUploadPhotos: String
    val failedToUpdateBusiness: String
    val errorLoadingClaimsPrefix: String
    val failedToApprovePrefix: String
    val failedToRejectPrefix: String
    val importFailedPrefix: String
    val claimApprovedTemplate: String
    val claimRejectedMsg: String
    val importSuccessTemplate: String
    val mustBeLoggedInToPostStory: String
    val addAtLeastOnePhoto: String
    val maxPhotosPerStory: String
    val failedToPostStoryFallback: String

    // Promotion discount code / expiry date
    val promotionDiscountCodeLabel: String
    val promotionExpirySection: String
    val promotionInvalidExpiry: String
    val promotionCodePrefix: String
    val promotionExpiresPrefix: String

    val maxPhotosPerReview: String

    // Edit / delete own review
    val editReviewTitle: String
    val deleteReviewConfirmTitle: String
    val deleteReviewConfirmMessage: String
    val deleteReviewButton: String
    val reviewUpdated: String
    val reviewUpdateFailed: String
    val reviewDeleted: String
    val reviewDeleteFailed: String

    // Edit / delete own reply
    val editReplyTitle: String
    val deleteReplyConfirmTitle: String
    val deleteReplyConfirmMessage: String
    val deleteReplyButton: String
    val replyUpdated: String
    val replyUpdateFailed: String
    val replyDeleted: String
    val replyDeleteFailed: String
}

object EnglishStrings : AppStrings {
    // General
    override val appName = "MeTont"
    override val save = "Save"
    override val cancel = "Cancel"
    override val back = "Back"
    override val loading = "Loading..."
    override val noResults = "No businesses found"
    override val welcomeUser = "Welcome"
    // Auth
    override val welcomeTitle = "Welcome to Albanian Business App"
    override val welcomeDesc = "Register your business, or explore the Albanian business map in your area and beyond."
    override val getStarted = "Get Started"
    override val signIn = "Sign In"
    override val signUp = "Create Account"
    override val email = "Email"
    override val password = "Password"
    override val confirmPassword = "Confirm Password"
    override val logout = "Logout"
    override val noAccount = "Don't have an account? Sign Up"
    override val haveAccount = "Already have an account? Sign In"
    override val forgotPassword = "Forgot password?"
    override val resetPasswordTitle = "Reset Password"
    override val resetPasswordDescription = "Enter your email and we'll send you a link to reset your password."
    override val sendResetLink = "Send Reset Link"
    override val resetEmailSent = "Password reset email sent! Check your inbox."
    override val continueWithGoogle = "Continue with Google"
    override val continueWithApple = "Continue with Apple"
    // Map
    override val searchPlaceholder = "Search businesses..."
    override val addBusiness = "Add My Business"
    override val listView = "List View"
    override val favorites = "My Favorites"
    override val profile = "Profile"
    override val communityEvents = "Community Events"
    override val viewDetailsAndRate = "View Details & Rate"
    // Business List
    override val directory = "Directory"
    override val featured = "Featured Businesses"
    override val recentlyAdded = "Recently Added"
    override val topRated = "Top Rated"
    override val allBusinesses = "All Businesses"
    override val getDirections = "Get Directions"
    override val nearMe = "Near Me"
    // Business Detail
    override val recentReviews = "Recent Reviews"
    override val noReviewsYet = "No reviews yet. Be the first!"
    override val writeReview = "Review"
    override val editBusiness = "Edit"
    override val upgradePremium = "Upgrade to Premium to unlock contact info, website, photos and more for just \$2.99/month"
    override val viewPlans = "View Plans"
    override val photos = "Photos"
    override val workingHours = "Working Hours"
    override val promotions = "Promotions & Deals"
    override val jobs = "Job Postings"
    override val readMore = "Read more"
    override val readLess = "Read less"
    override val category = "Category"
    override val verified = "Verified"
    override val albanianOwned = "Albanian Owned"
    override val premium = "Premium"
    override val featured2 = "Featured"
    override val sponsored = "Sponsored"
    // Profile
    override val myProfile = "My Profile"
    override val personalInformation = "Personal Information"
    override val firstName = "First Name"
    override val lastName = "Last Name"
    override val saveProfile = "Save Profile"
    override val upgradeToPremium = "Upgrade to Premium"
    // Events
    override val communityEventsTitle = "Community Events"
    override val noEventsFound = "No upcoming events found."
    override val submitEvent = "Submit Event"
    override val eventTitle = "Event Title"
    override val eventDescription = "Description"
    override val eventDate = "Event Date"
    override val eventLocation = "Location Name"
    override val eventWebsite = "Website URL (Optional)"
    override val eventCategory = "Category"
    override val addPhoto = "Add Photo"
    override val changePhoto = "Change Photo"
    override val promoted = "PROMOTED"
    override val viewEventWebsite = "View Event Website"
    // Reviews
    override val rateThisBusiness = "Rate this business"
    override val tapStarToRate = "Tap a star to select a rating"
    override val shareExperience = "Share your experience..."
    override val submitReview = "Submit Review"
    override val submitting = "Submitting..."
    // Subscription
    override val choosePlan = "Choose Your Plan"
    override val upgradeYourListing = "Upgrade Your Listing"
    override val currentPlan = "CURRENT"
    override val requestUpgrade = "Request Upgrade"
    override val requestSponsorship = "Request Sponsorship"
    override val requestFeatured = "Request Featured"
    override val manualPaymentNote = "Payments are currently processed manually. We will contact you within 24 hours of your request."
    // Favorites
    override val myFavorites = "My Favorites"
    override val noFavoritesYet = "You haven't saved any businesses yet."
    override val myBusinesses = "My Businesses"
    override val noBusinessesYet = "You don't have any businesses yet"
    override val noBusinessesYetSubtitle = "Add your business to reach more customers"
    override val myBusinessesSubtitle = "Manage your business listings"
    override val myEvents = "My Events"
    override val noEventsYet = "You don't have any events yet"
    override val noEventsYetSubtitle = "Submit an event to reach more people"
    override val myEventsSubtitle = "Manage your submitted events"
    override val deleteEvent = "Delete Event"
    override val deleteEventConfirmTitle = "Delete this event?"
    override val deleteEventConfirmMessage = "This will permanently remove the event. This action cannot be undone."
    override val eventDeleted = "Event deleted"
    override val eventDeleteFailed = "Failed to delete event"
    override val noSearchResults = "No results found"
    override val upgradePremiumTitle = "Upgrade to Premium"

    // In EnglishStrings
    override val firstNameRequired = "First name is required"
    override val profileSaved = "Profile saved!"
    override val profileSaveFailed = "Failed to save profile"

    // In EnglishStrings
    override val eventPhotoSection = "Event Photo (Optional)"
    override val eventDetailsSection = "Event Details"
    override val eventDateSection = "Event Date *"
    override val eventLocationSection = "Location & Contact"

    override val eventTitleRequired = "Event title is required"
    override val eventDescriptionRequired = "Description is required"
    override val eventLocationRequired = "Location is required"
    override val eventInvalidDate = "Please enter a valid date"
    override val eventSubmitSuccess = "Event submitted successfully!"
    override val eventSubmitFailed = "Failed to submit event"
    override val submitEventButton = "Submit Event"

    override val writeReviewLabel = "Write your review"
    override val pleaseSelectRating = "Please select a rating"
    override val pleaseWriteReview = "Please write a review"
    override val reviewSubmitted = "Review submitted!"

    override val welcomeBack = "Welcome back!"
    override val signInToContinue = "Sign in to continue"
    override val signUpToGetStarted = "Sign up to get started"
    override val notNow = "Not now"

    override val emailRequired = "Email is required"
    override val passwordRequired = "Password is required"
    override val passwordsDoNotMatch = "Passwords don't match"
    override val passwordTooShort = "Password must be at least 8 characters"
    override val registerBusiness = "Register Business"
    override val requiredInformation = "Required Information"
    override val businessName = "Business Name *"
    override val locationSection = "Location *"
    override val fullAddress = "Full Address *"
    override val cityLabel = "City *"
    override val countryLabel = "Country *"
    override val cityRequired = "City is required"
    override val locatingAddress = "Finding coordinates…"
    override val geocodeFailed = "Couldn't find that address — please check it's correct"
    override val latitude = "Latitude *"
    override val longitude = "Longitude *"
    override val pickLocationFromMap = "Pick Location from Map"
    override val contactInformation = "Contact Information"
    override val phoneNumber = "Phone Number *"
    override val emailOptional = "Email (Optional)"
    override val websiteOptional = "Website (Optional)"
    override val workingHoursSection = "Working Hours"
    override val open247 = "Open 24/7"
    override val photoOptional = "Photo (Optional)"
    override val photoAdded = "Photo Added (1/1)"
    override val registering = "Registering..."
    override val registerBusinessButton = "Register Business"
    override val businessNameRequired = "Business name is required"
    override val selectCategory = "Please select a category"
    override val descriptionRequired = "Description is required"
    override val addressRequired = "Address is required"
    override val phoneRequired = "Phone number is required"
    override val validCoordinates = "Please enter valid coordinates"
    override val businessRegistered = "Business registered successfully!"
    override val cameraPermissionRequired = "Camera permission required"
    override val gallery = "Gallery"
    override val camera = "Camera"
    override val choosePhotoSource = "Choose photo source"

    // Generic dialog buttons
    override val ok = "OK"
    override val confirm = "Confirm"
    override val openButton = "Open"

    // Time picker / working hours
    override val hoursOpenLabel = "Open"
    override val hoursCloseLabel = "Close"

    // Add Event date fields
    override val dayLabel = "Day"
    override val dayPlaceholder = "DD"
    override val monthLabel = "Month"
    override val monthPlaceholder = "MM"
    override val yearLabel = "Year"
    override val yearPlaceholder = "YYYY"
    override val eventStartTimeLabel = "Start time"

    // Edit Business screen
    override val basicInformationSection = "Basic Information"
    override val locationSectionShort = "Location"
    override val saveChanges = "Save Changes"
    override val businessUpdatedSuccess = "Business updated successfully!"
    override val categoryRequiredLabel = "Category *"
    override val descriptionRequiredLabel = "Description *"
    override val photoLimitReached = "Photo limit reached for your plan"
    override val addJobPostingTitle = "Add Job Posting"
    override val jobTitleLabel = "Job Title *"
    override val jobTypeLabel = "Job Type *"
    override val jobSalaryLabel = "Salary (Optional)"
    override val jobSalaryPlaceholder = "e.g. \$1,500/month"
    override val addJobButton = "Add Job"
    override val jobTitleDescRequired = "Title and description are required"
    override val addPromotionTitle = "Add Promotion"
    override val promotionTitleLabel = "Promotion Title *"

    // Add Story screen
    override val addStoryTitle = "Add Story"
    override val addPhotosButton = "Add Photos"
    override val captionPlaceholder = "Write a caption..."
    override val storyLocationPlaceholder = "e.g. Tirana, Albania"
    override val postStory = "Post Story"

    // Admin screen
    override val importSampleBusinesses = "Import Sample Businesses"
    override val approveClaimTitle = "Approve Claim"
    override val approveClaimMessage = "Are you sure you want to approve %1\$s's claim for \"%2\$s\"? This will transfer ownership and verify the business."
    override val approve = "Approve"
    override val rejectClaimTitle = "Reject Claim"
    override val rejectClaimMessage = "Are you sure you want to reject this claim request from %1\$s?"
    override val reject = "Reject"

    // Business detail screen
    override val writeReplyPlaceholder = "Write a reply..."
    override val noLocationSetToast = "No location set for this business"
    override val googleMapsNotInstalled = "Google Maps isn't installed"

    // Business list screen
    override val applyFilters = "Apply Filters"
    override val allFilterOption = "All"

    // Jobs screen
    override val viewProfile = "View Profile"
    override val jobsEmptyTitle = "No job postings yet"
    override val jobsEmptySubtitle = "Businesses that are hiring will show up here"

    // Map / home screen
    override val appTagline = "Albanian Business Directory"
    override val viewFullProfile = "View Full Profile"
    override val noBusinessesNearYou = "No businesses near you yet"
    override val appGrowingMessage = "MeTont is growing — check back soon!"
    override val communityAnnouncements = "Community Announcements"
    override val noUpcomingEventsShort = "No upcoming events right now"
    override val mostFavoritedWorldwide = "Most Favorited Worldwide"
    override val noBusinessesYetHome = "No businesses yet"

    // Story viewer
    override val viewBusiness = "View Business"
    override val couldntOpenMaps = "Couldn't open maps"

    // Events screen
    override val couldntOpenLink = "Couldn't open that link"

    // Reviews
    override val loginRequiredForReview = "You must be logged in to submit a review"

    // Subscription
    override val freeTierName = "Free"

    // Edit Business screen (additional)
    override val editBusinessTitle = "Edit Business"
    override val savingLabel = "Saving..."
    override val promotionsEmptyTitle = "No promotions yet."
    override val closedLabel = "Closed"

    // Subscription screen (plan details)
    override val upgradeTitleTemplate = "Upgrade \"%1\$s\""
    override val subscriptionHeaderSubtitle = "Help your business stand out in the Albanian community"
    override val perMonth = "per month"
    override val forever = "forever"
    override val currentPlanButton = "Current Plan"
    override val notAvailableDash = "—"
    override val sendUpgradeRequest = "Send upgrade request"
    override val sendFeaturedRequest = "Send featured request"
    override val sendSponsorshipRequest = "Send sponsorship request"
    override val planFeatureNameCategory = "Business name & category"
    override val planFeatureLocationOnMap = "Location on map"
    override val planFeature100CharDesc = "100 character description"
    override val planFeature1Photo = "1 photo"
    override val planFeaturePhoneNumber = "Phone number"
    override val planFeatureEmailWebsite = "Email & website"
    override val planFeatureExtendedDesc = "Extended description"
    override val planFeatureHoursOfOperation = "Hours of operation"
    override val planFeaturePremiumBadge = "Premium badge"
    override val planFeatureUp6Photos = "Up to 6 photos"
    override val planFeatureEverythingPremium = "Everything in Premium"
    override val planFeatureUp10Photos = "Up to 10 photos"
    override val planFeatureFeaturedBadge = "Featured badge"
    override val planFeatureFeaturedDiscoveryRow = "Featured in discovery row"
    override val planFeatureHighlightedListView = "Highlighted in list view"
    override val planFeatureUp14Photos = "Up to 14 photos"
    override val planFeatureHighlightedMapPin = "Highlighted map pin"
    override val planFeatureTopSearchResults = "Top of search results"
    override val planFeatureSponsoredBadge = "Sponsored badge"
    override val planFeatureFeaturedDiscoverySection = "Featured in discovery section"
    override val planFeaturePriorityCustomerSupport = "Priority customer support"

    // Admin screen (additional)
    override val pendingClaimRequests = "Pending Claim Requests"
    override val noPendingClaims = "No pending claims!"
    override val allClaimsProcessed = "All claim requests have been processed."
    override val pendingCountLabel = "pending"

    // Exception/error messages
    override val mustBeLoggedInToAddBusiness = "You must be logged in to add a business"
    override val reviewNotFound = "Review not found"
    override val alreadyReportedReview = "You have already reported this review"
    override val replyNotFound = "Reply not found"
    override val reviewReportedSuccess = "Review reported successfully"
    override val failedToAddBusinessPrefix = "Failed to add business"
    override val failedToUploadImagesPrefix = "Failed to upload images"
    override val unexpectedErrorPrefix = "Unexpected error"
    override val failedToUploadPhotos = "Failed to upload photos"
    override val failedToUpdateBusiness = "Failed to update business"
    override val errorLoadingClaimsPrefix = "Error loading claims"
    override val failedToApprovePrefix = "Failed to approve"
    override val failedToRejectPrefix = "Failed to reject"
    override val importFailedPrefix = "Import failed"
    override val claimApprovedTemplate = "Claim approved — %1\$s is now owned by %2\$s"
    override val claimRejectedMsg = "Claim rejected"
    override val importSuccessTemplate = "Successfully imported %1\$d businesses!"
    override val mustBeLoggedInToPostStory = "You must be logged in to post a story"
    override val addAtLeastOnePhoto = "Please add at least one photo"
    override val maxPhotosPerStory = "Maximum 10 photos per story"
    override val failedToPostStoryFallback = "Failed to post story"

    override val promotionDiscountCodeLabel = "Discount Code (Optional)"
    override val promotionExpirySection = "Expiry Date (Optional)"
    override val promotionInvalidExpiry = "Please enter a valid expiry date"
    override val promotionCodePrefix = "Code: "
    override val promotionExpiresPrefix = "Expires: "

    override val maxPhotosPerReview = "Maximum 5 photos per review"

    override val editReviewTitle = "Edit Review"
    override val deleteReviewConfirmTitle = "Delete this review?"
    override val deleteReviewConfirmMessage = "This will permanently remove your review. This action cannot be undone."
    override val deleteReviewButton = "Delete Review"
    override val reviewUpdated = "Review updated"
    override val reviewUpdateFailed = "Failed to update review"
    override val reviewDeleted = "Review deleted"
    override val reviewDeleteFailed = "Failed to delete review"

    override val editReplyTitle = "Edit Reply"
    override val deleteReplyConfirmTitle = "Delete this reply?"
    override val deleteReplyConfirmMessage = "This will permanently remove your reply. This action cannot be undone."
    override val deleteReplyButton = "Delete Reply"
    override val replyUpdated = "Reply updated"
    override val replyUpdateFailed = "Failed to update reply"
    override val replyDeleted = "Reply deleted"
    override val replyDeleteFailed = "Failed to delete reply"
}

object AlbanianStrings : AppStrings {
    // General
    override val appName = "MeTont"
    override val save = "Ruaj"
    override val cancel = "Anulo"
    override val back = "Kthehu"
    override val loading = "Duke ngarkuar..."
    override val noResults = "Nuk u gjet asnjë biznes"
    override val welcomeUser = "Mirë se vini"
    // Auth
    override val welcomeTitle = "Mirësevini në MeTont"
    override val welcomeDesc = "Regjistroni biznesin tuaj, ose eksploroni hartën e bizneseve shqiptare në zonën tuaj dhe më gjerë."
    override val getStarted = "Fillo Tani"
    override val signIn = "Hyr"
    override val signUp = "Krijo Llogari"
    override val email = "Email"
    override val password = "Fjalëkalimi"
    override val confirmPassword = "Konfirmo Fjalëkalimin"
    override val logout = "Dil"
    override val noAccount = "Nuk keni llogari? Regjistrohu"
    override val haveAccount = "Keni llogari? Hyr"
    override val forgotPassword = "Keni harruar fjalëkalimin?"
    override val resetPasswordTitle = "Rivendos Fjalëkalimin"
    override val resetPasswordDescription = "Shkruani email-in tuaj dhe ne do t'ju dërgojmë një lidhje për të rivendosur fjalëkalimin."
    override val sendResetLink = "Dërgo Lidhjen"
    override val resetEmailSent = "Email-i për rivendosjen e fjalëkalimit u dërgua! Kontrolloni inbox-in tuaj."
    override val continueWithGoogle = "Vazhdo me Google"
    override val continueWithApple = "Vazhdo me Apple"
    // Map
    override val searchPlaceholder = "Kërko biznese..."
    override val addBusiness = "Shto Biznesin Tim"
    override val listView = "Lista"
    override val favorites = "Të Preferuarat"
    override val profile = "Profili"
    override val communityEvents = "Ngjarjet e Komunitetit"
    override val viewDetailsAndRate = "Shiko Detajet & Vlerëso"
    // Business List
    override val directory = "Drejtori"
    override val featured = "Bizneset e Rëndësishme"
    override val recentlyAdded = "Shtuar Së Fundmi"
    override val topRated = "Më të Vlerësuarit"
    override val allBusinesses = "Të Gjitha Bizneset"
    override val getDirections = "Drejtimet"
    override val nearMe = "Pranë Meje"
    // Business Detail
    override val recentReviews = "Vlerësimet e Fundit"
    override val noReviewsYet = "Asnjë vlerësim ende. Bëhu i pari!"
    override val writeReview = "Vlerëso"
    override val editBusiness = "Ndrysho"
    override val upgradePremium = "Kaloni në Premium për të zhbllokuar kontaktet, faqen dhe fotot për vetëm \$2.99/muaj"
    override val viewPlans = "Shiko Planet"
    override val photos = "Foto"
    override val workingHours = "Orari i Punës"
    override val promotions = "Promocione dhe Oferta"
    override val jobs = "Oferta Pune"
    override val readMore = "Lexo më shumë"
    override val readLess = "Lexo më pak"
    override val category = "Kategoria"
    override val verified = "Verifikuar"
    override val albanianOwned = "Pronë Shqiptare"
    override val premium = "Premium"
    override val featured2 = "I Veçuar"
    override val sponsored = "Sponsorizuar"
    // Profile
    override val myProfile = "Profili Im"
    override val personalInformation = "Informacioni Personal"
    override val firstName = "Emri"
    override val lastName = "Mbiemri"
    override val saveProfile = "Ruaj Profilin"
    override val upgradeToPremium = "Kalo në Premium"
    // Events
    override val communityEventsTitle = "Ngjarjet e Komunitetit"
    override val noEventsFound = "Nuk u gjetën ngjarje të ardhshme."
    override val submitEvent = "Dërgo Ngjarjen"
    override val eventTitle = "Titulli i Ngjarjes"
    override val eventDescription = "Përshkrimi"
    override val eventDate = "Data e Ngjarjes"
    override val eventLocation = "Emri i Vendit"
    override val eventWebsite = "URL e Faqes (Opsionale)"
    override val eventCategory = "Kategoria"
    override val addPhoto = "Shto Foto"
    override val changePhoto = "Ndrysho Foton"
    override val promoted = "PROMOVUAR"
    override val viewEventWebsite = "Shiko Faqen e Ngjarjes"
    // Reviews
    override val rateThisBusiness = "Vlerëso këtë biznes"
    override val tapStarToRate = "Trokitni një yll për të vlerësuar"
    override val shareExperience = "Ndani përvojën tuaj..."
    override val submitReview = "Dërgo Vlerësimin"
    override val submitting = "Duke dërguar..."
    // Subscription
    override val choosePlan = "Zgjidhni Planin Tuaj"
    override val upgradeYourListing = "Përmirësoni Listimin Tuaj"
    override val currentPlan = "AKTUAL"
    override val requestUpgrade = "Kërkoni Përmirësim"
    override val requestSponsorship = "Kërkoni Sponsorizim"
    override val requestFeatured = "Kërkoni të Veçohet"
    override val manualPaymentNote = "Pagesat aktualisht procesohen manualisht. Do t'ju kontaktojmë brenda 24 orëve."
    // Favorites
    override val myFavorites = "Të Preferuarat e Mia"
    override val noFavoritesYet = "Nuk keni ruajtur asnjë biznes ende."
    override val myBusinesses = "Bizneset e Mia"
    override val noBusinessesYet = "Ende nuk keni asnjë biznes"
    override val noBusinessesYetSubtitle = "Shtoni biznesin tuaj për të arritur më shumë klientë"
    override val myBusinessesSubtitle = "Menaxhoni listimet e biznesit tuaj"
    override val myEvents = "Eventet e Mia"
    override val noEventsYet = "Ende nuk keni asnjë event"
    override val noEventsYetSubtitle = "Shtoni një event për të arritur më shumë njerëz"
    override val myEventsSubtitle = "Menaxhoni eventet tuaja të dërguara"
    override val deleteEvent = "Fshi Eventin"
    override val deleteEventConfirmTitle = "Fshi këtë event?"
    override val deleteEventConfirmMessage = "Kjo do ta heqë përgjithmonë eventin. Ky veprim nuk mund të zhbëhet."
    override val eventDeleted = "Eventi u fshi"
    override val eventDeleteFailed = "Fshirja e eventit dështoi"
    override val noSearchResults = "Nuk u gjetën rezultate"
    override val upgradePremiumTitle = "Kalo në Premium"


    // In AlbanianStrings
    override val firstNameRequired = "Emri është i detyrueshëm"
    override val profileSaved = "Profili u ruajt!"
    override val profileSaveFailed = "Dështoi ruajtja e profilit"

    // In AlbanianStrings
    override val eventPhotoSection = "Foto e Ngjarjes (Opsionale)"
    override val eventDetailsSection = "Detajet e Ngjarjes"
    override val eventDateSection = "Data e Ngjarjes *"
    override val eventLocationSection = "Vendndodhja & Kontakti"

    override val eventTitleRequired = "Titulli i ngjarjes është i detyrueshëm"
    override val eventDescriptionRequired = "Përshkrimi është i detyrueshëm"
    override val eventLocationRequired = "Vendndodhja është e detyrueshme"
    override val eventInvalidDate = "Ju lutemi vendosni një datë të vlefshme"
    override val eventSubmitSuccess = "Ngjarja u dërgua me sukses!"
    override val eventSubmitFailed = "Dështoi dërgimi i ngjarjes"
    override val submitEventButton = "Dërgo Ngjarjen"

    override val writeReviewLabel = "Shkruaj vlerësimin tuaj"
    override val pleaseSelectRating = "Ju lutemi zgjidhni një vlerësim"
    override val pleaseWriteReview = "Ju lutemi shkruani një vlerësim"
    override val reviewSubmitted = "Vlerësimi u dërgua!"

    override val welcomeBack = "Mirësevini!"
    override val signInToContinue = "Hyni për të vazhduar"
    override val signUpToGetStarted = "Regjistrohu për të filluar"
    override val notNow = "Jo tani"

    override val emailRequired = "Email është i detyrueshëm"
    override val passwordRequired = "Fjalëkalimi është i detyrueshëm"
    override val passwordsDoNotMatch = "Fjalëkalimet nuk përputhen"
    override val passwordTooShort = "Fjalëkalimi duhet të ketë të paktën 8 karaktere"

    override val registerBusiness = "Regjistro Biznesin"
    override val requiredInformation = "Informacioni i Detyrueshëm"
    override val businessName = "Emri i Biznesit *"
    override val locationSection = "Vendndodhja *"
    override val fullAddress = "Adresa e Plotë *"
    override val cityLabel = "Qyteti *"
    override val countryLabel = "Shteti *"
    override val cityRequired = "Qyteti është i detyrueshëm"
    override val locatingAddress = "Duke gjetur koordinatat…"
    override val geocodeFailed = "Nuk u gjet ky adresë — ju lutem kontrolloni nëse është e saktë"
    override val latitude = "Gjerësia *"
    override val longitude = "Gjatësia *"
    override val pickLocationFromMap = "Zgjidhni Vendndodhjen nga Harta"
    override val contactInformation = "Informacioni i Kontaktit"
    override val phoneNumber = "Numri i Telefonit *"
    override val emailOptional = "Email (Opsional)"
    override val websiteOptional = "Faqja Web (Opsionale)"
    override val workingHoursSection = "Orari i Punës"
    override val open247 = "Hapur 24/7"
    override val photoOptional = "Foto (Opsionale)"
    override val photoAdded = "Foto u Shtua (1/1)"
    override val registering = "Duke Regjistruar..."
    override val registerBusinessButton = "Regjistro Biznesin"
    override val businessNameRequired = "Emri i biznesit është i detyrueshëm"
    override val selectCategory = "Ju lutemi zgjidhni një kategori"
    override val descriptionRequired = "Përshkrimi është i detyrueshëm"
    override val addressRequired = "Adresa është e detyrueshme"
    override val phoneRequired = "Numri i telefonit është i detyrueshëm"
    override val validCoordinates = "Ju lutemi vendosni koordinata të vlefshme"
    override val businessRegistered = "Biznesi u regjistrua me sukses!"
    override val cameraPermissionRequired = "Leja e kamerës është e nevojshme"
    override val gallery = "Galeria"
    override val camera = "Kamera"
    override val choosePhotoSource = "Zgjidhni burimin e fotos"

    // Generic dialog buttons
    override val ok = "OK"
    override val confirm = "Konfirmo"
    override val openButton = "Hap"

    // Time picker / working hours
    override val hoursOpenLabel = "Hapja"
    override val hoursCloseLabel = "Mbyllja"

    // Add Event date fields
    override val dayLabel = "Dita"
    override val dayPlaceholder = "DD"
    override val monthLabel = "Muaji"
    override val monthPlaceholder = "MM"
    override val yearLabel = "Viti"
    override val yearPlaceholder = "VVVV"
    override val eventStartTimeLabel = "Ora e fillimit"

    // Edit Business screen
    override val basicInformationSection = "Informacion Bazë"
    override val locationSectionShort = "Vendndodhja"
    override val saveChanges = "Ruaj Ndryshimet"
    override val businessUpdatedSuccess = "Biznesi u përditësua me sukses!"
    override val categoryRequiredLabel = "Kategoria *"
    override val descriptionRequiredLabel = "Përshkrimi *"
    override val photoLimitReached = "Limiti i fotove u arrit për planin tuaj"
    override val addJobPostingTitle = "Shto Vend Pune"
    override val jobTitleLabel = "Titulli i Punës *"
    override val jobTypeLabel = "Lloji i Punës *"
    override val jobSalaryLabel = "Paga (Opsionale)"
    override val jobSalaryPlaceholder = "p.sh. 1,500\$/muaj"
    override val addJobButton = "Shto Punë"
    override val jobTitleDescRequired = "Titulli dhe përshkrimi janë të detyrueshëm"
    override val addPromotionTitle = "Shto Promocion"
    override val promotionTitleLabel = "Titulli i Promocionit *"

    // Add Story screen
    override val addStoryTitle = "Shto Histori"
    override val addPhotosButton = "Shto Foto"
    override val captionPlaceholder = "Shkruani një përshkrim..."
    override val storyLocationPlaceholder = "p.sh. Tiranë, Shqipëri"
    override val postStory = "Posto Historinë"

    // Admin screen
    override val importSampleBusinesses = "Importo Biznese Shembull"
    override val approveClaimTitle = "Mirato Kërkesën"
    override val approveClaimMessage = "Jeni i sigurt që doni të miratoni kërkesën e %1\$s për \"%2\$s\"? Kjo do të transferojë pronësinë dhe do të verifikojë biznesin."
    override val approve = "Mirato"
    override val rejectClaimTitle = "Refuzo Kërkesën"
    override val rejectClaimMessage = "Jeni i sigurt që doni të refuzoni këtë kërkesë nga %1\$s?"
    override val reject = "Refuzo"

    // Business detail screen
    override val writeReplyPlaceholder = "Shkruani një përgjigje..."
    override val noLocationSetToast = "Nuk ka vendndodhje të vendosur për këtë biznes"
    override val googleMapsNotInstalled = "Google Maps nuk është i instaluar"

    // Business list screen
    override val applyFilters = "Apliko Filtrat"
    override val allFilterOption = "Të gjitha"

    // Jobs screen
    override val viewProfile = "Shiko Profilin"
    override val jobsEmptyTitle = "Ende nuk ka vende pune"
    override val jobsEmptySubtitle = "Bizneset që po punësojnë do të shfaqen këtu"

    // Map / home screen
    override val appTagline = "Direktoria e Bizneseve Shqiptare"
    override val viewFullProfile = "Shiko Profilin e Plotë"
    override val noBusinessesNearYou = "Ende nuk ka biznese pranë jush"
    override val appGrowingMessage = "MeTont po rritet — kontrolloni përsëri së shpejti!"
    override val communityAnnouncements = "Njoftime të Komunitetit"
    override val noUpcomingEventsShort = "Nuk ka evente të ardhshme tani"
    override val mostFavoritedWorldwide = "Më të Preferuarit në Botë"
    override val noBusinessesYetHome = "Ende nuk ka biznese"

    // Story viewer
    override val viewBusiness = "Shiko Biznesin"
    override val couldntOpenMaps = "Nuk u hap dot harta"

    // Events screen
    override val couldntOpenLink = "Nuk u hap dot lidhja"

    // Reviews
    override val loginRequiredForReview = "Duhet të jeni të kyçur për të dërguar një vlerësim"

    // Subscription
    override val freeTierName = "Falas"

    // Edit Business screen (additional)
    override val editBusinessTitle = "Ndrysho Biznesin"
    override val savingLabel = "Duke ruajtur..."
    override val promotionsEmptyTitle = "Ende nuk ka promocione."
    override val closedLabel = "Mbyllur"

    // Subscription screen (plan details)
    override val upgradeTitleTemplate = "Përmirëso \"%1\$s\""
    override val subscriptionHeaderSubtitle = "Ndihmoni biznesin tuaj të dallohet në komunitetin shqiptar"
    override val perMonth = "në muaj"
    override val forever = "përgjithmonë"
    override val currentPlanButton = "Plani Aktual"
    override val notAvailableDash = "—"
    override val sendUpgradeRequest = "Dërgo kërkesën për përmirësim"
    override val sendFeaturedRequest = "Dërgo kërkesën për t'u veçuar"
    override val sendSponsorshipRequest = "Dërgo kërkesën për sponsorizim"
    override val planFeatureNameCategory = "Emri dhe kategoria e biznesit"
    override val planFeatureLocationOnMap = "Vendndodhja në hartë"
    override val planFeature100CharDesc = "Përshkrim 100 karaktere"
    override val planFeature1Photo = "1 foto"
    override val planFeaturePhoneNumber = "Numri i telefonit"
    override val planFeatureEmailWebsite = "Email dhe faqja web"
    override val planFeatureExtendedDesc = "Përshkrim i zgjeruar"
    override val planFeatureHoursOfOperation = "Orari i punës"
    override val planFeaturePremiumBadge = "Distinktivi Premium"
    override val planFeatureUp6Photos = "Deri në 6 foto"
    override val planFeatureEverythingPremium = "Gjithçka në Premium"
    override val planFeatureUp10Photos = "Deri në 10 foto"
    override val planFeatureFeaturedBadge = "Distinktivi i Veçuar"
    override val planFeatureFeaturedDiscoveryRow = "I veçuar në rreshtin e zbulimit"
    override val planFeatureHighlightedListView = "I theksuar në pamjen e listës"
    override val planFeatureUp14Photos = "Deri në 14 foto"
    override val planFeatureHighlightedMapPin = "Shenjë e theksuar në hartë"
    override val planFeatureTopSearchResults = "Në krye të rezultateve të kërkimit"
    override val planFeatureSponsoredBadge = "Distinktivi Sponsorizuar"
    override val planFeatureFeaturedDiscoverySection = "I veçuar në seksionin e zbulimit"
    override val planFeaturePriorityCustomerSupport = "Mbështetje klientësh me përparësi"

    // Admin screen (additional)
    override val pendingClaimRequests = "Kërkesat në Pritje"
    override val noPendingClaims = "Nuk ka kërkesa në pritje!"
    override val allClaimsProcessed = "Të gjitha kërkesat janë përpunuar."
    override val pendingCountLabel = "në pritje"

    // Exception/error messages
    override val mustBeLoggedInToAddBusiness = "Duhet të jeni të kyçur për të shtuar një biznes"
    override val reviewNotFound = "Vlerësimi nuk u gjet"
    override val alreadyReportedReview = "E keni raportuar tashmë këtë vlerësim"
    override val replyNotFound = "Përgjigja nuk u gjet"
    override val reviewReportedSuccess = "Vlerësimi u raportua me sukses"
    override val failedToAddBusinessPrefix = "Shtimi i biznesit dështoi"
    override val failedToUploadImagesPrefix = "Ngarkimi i fotove dështoi"
    override val unexpectedErrorPrefix = "Gabim i papritur"
    override val failedToUploadPhotos = "Ngarkimi i fotove dështoi"
    override val failedToUpdateBusiness = "Përditësimi i biznesit dështoi"
    override val errorLoadingClaimsPrefix = "Gabim gjatë ngarkimit të kërkesave"
    override val failedToApprovePrefix = "Miratimi dështoi"
    override val failedToRejectPrefix = "Refuzimi dështoi"
    override val importFailedPrefix = "Importimi dështoi"
    override val claimApprovedTemplate = "Kërkesa u miratua — %1\$s tani i përket %2\$s"
    override val claimRejectedMsg = "Kërkesa u refuzua"
    override val importSuccessTemplate = "U importuan me sukses %1\$d biznese!"
    override val mustBeLoggedInToPostStory = "Duhet të jeni të kyçur për të postuar një histori"
    override val addAtLeastOnePhoto = "Ju lutemi shtoni të paktën një foto"
    override val maxPhotosPerStory = "Maksimumi 10 foto për histori"
    override val failedToPostStoryFallback = "Postimi i historisë dështoi"

    override val promotionDiscountCodeLabel = "Kodi i Zbritjes (Opsionale)"
    override val promotionExpirySection = "Data e Skadimit (Opsionale)"
    override val promotionInvalidExpiry = "Ju lutemi vendosni një datë skadimi të vlefshme"
    override val promotionCodePrefix = "Kodi: "
    override val promotionExpiresPrefix = "Skadon: "

    override val maxPhotosPerReview = "Maksimumi 5 foto për vlerësim"

    override val editReviewTitle = "Redakto Vlerësimin"
    override val deleteReviewConfirmTitle = "Fshi këtë vlerësim?"
    override val deleteReviewConfirmMessage = "Kjo do ta heqë përgjithmonë vlerësimin tuaj. Ky veprim nuk mund të zhbëhet."
    override val deleteReviewButton = "Fshi Vlerësimin"
    override val reviewUpdated = "Vlerësimi u përditësua"
    override val reviewUpdateFailed = "Përditësimi i vlerësimit dështoi"
    override val reviewDeleted = "Vlerësimi u fshi"
    override val reviewDeleteFailed = "Fshirja e vlerësimit dështoi"

    override val editReplyTitle = "Redakto Përgjigjen"
    override val deleteReplyConfirmTitle = "Fshi këtë përgjigje?"
    override val deleteReplyConfirmMessage = "Kjo do ta heqë përgjithmonë përgjigjen tuaj. Ky veprim nuk mund të zhbëhet."
    override val deleteReplyButton = "Fshi Përgjigjen"
    override val replyUpdated = "Përgjigja u përditësua"
    override val replyUpdateFailed = "Përditësimi i përgjigjes dështoi"
    override val replyDeleted = "Përgjigja u fshi"
    override val replyDeleteFailed = "Fshirja e përgjigjes dështoi"
}

val LocalAppStrings = staticCompositionLocalOf<AppStrings> { EnglishStrings }

// ViewModels and Repositories aren't Composables, so they can't read
// LocalAppStrings — there's no CompositionLocal context down there. This is a
// plain in-memory holder they can call instead (e.g. CurrentLanguage.strings().xxx)
// for the handful of app-authored exception/error messages that originate outside
// the UI layer. It's kept in sync with the real (Compose) language selection by
// ProvideAppStrings below, which every screen already sits under.
object CurrentLanguage {
    var language: AppLanguage = AppLanguage.EN
    fun strings(): AppStrings = if (language == AppLanguage.SQ) AlbanianStrings else EnglishStrings
}

@Composable
fun ProvideAppStrings(language: AppLanguage, content: @Composable () -> Unit) {
    CurrentLanguage.language = language
    val strings = if (language == AppLanguage.SQ) AlbanianStrings else EnglishStrings
    CompositionLocalProvider(LocalAppStrings provides strings) {
        content()
    }
}
// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import PhotosUI
import UIKit

// Mirrors Android's EditBusinessScreen.kt: full edit form (name, category,
// description, address/city/country, contact, working hours, photos, job
// postings, promotions) with the same Save validation order and the same
// geocode-on-save flow as AddBusinessScreen. Coordinates are never hand
// edited — they're re-resolved from Address+City+Country when Save is
// tapped, exactly like Android.
public struct EditBusinessScreen: View {
    @Environment(\.appStrings) private var strings

    public var business: Business
    public let onBackClick: () -> Void
    public let onBusinessUpdated: () -> Void

    @State private var name: String
    @State private var description: String
    @State private var phone: String
    @State private var email: String
    @State private var website: String
    @State private var address: String
    @State private var city: String
    @State private var country: String
    @State private var category: BusinessCategory?
    @State private var isOpen24Hours: Bool
    @State private var workingHours: [String: String]
    @State private var jobs: [JobPosting]
    @State private var promotions: [Promotion]
    @State private var existingPhotos: [String]
    @State private var newPhotosData: [Data] = []
    @State private var isAlbanianOwned: Bool
    @State private var isBusinessActive: Bool

    @State private var photosPickerItem: PhotosPickerItem? = nil
    @State private var showImageSourceDialog = false
    @State private var showGalleryPicker = false
    @State private var showCameraPicker = false

    @State private var showAddJobDialog = false
    @State private var showAddPromoDialog = false

    @State private var isGeocoding = false
    @State private var isSubmitting = false
    @State private var isUploadingPhotos = false
    @State private var errorMessage: String?

    public init(business: Business, onBackClick: @escaping () -> Void, onBusinessUpdated: @escaping () -> Void) {
        self.business = business
        self.onBackClick = onBackClick
        self.onBusinessUpdated = onBusinessUpdated
        _name = State(initialValue: business.name)
        _description = State(initialValue: business.description)
        _phone = State(initialValue: business.phone)
        _email = State(initialValue: business.email)
        _website = State(initialValue: business.website)
        _address = State(initialValue: business.address)
        _city = State(initialValue: business.city)
        _country = State(initialValue: business.country)
        _category = State(initialValue: BusinessCategory.match(business.category))
        _isOpen24Hours = State(initialValue: business.isOpen24Hours)
        _workingHours = State(initialValue: business.workingHours)
        _jobs = State(initialValue: business.jobs)
        _promotions = State(initialValue: business.promotions)
        _existingPhotos = State(initialValue: business.photos)
        _isAlbanianOwned = State(initialValue: business.isAlbanianOwned)
        _isBusinessActive = State(initialValue: business.isActive)
    }

    private var remainingPhotoSlots: Int {
        business.maxPhotos - existingPhotos.count - newPhotosData.count
    }

    private var isBusy: Bool { isSubmitting || isGeocoding || isUploadingPhotos }

    public var body: some View {
        ZStack {
            VStack(spacing: 0) {
                topAppBar

                ScrollView {
                    VStack(spacing: 12) {
                        activeStatusCard
                        basicInfoCard
                        locationCard
                        contactCard
                        workingHoursCard
                        albanianOwnedCard
                        photosCard
                        jobsCard
                        promotionsCard

                        if let err = errorMessage {
                            Text(err)
                                .font(.caption)
                                .foregroundColor(.red)
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding(.horizontal, 16)
                        }

                        saveButton

                        Spacer().frame(height: 32)
                    }
                    .padding(.top, 12)
                }
            }
            .background(Color.meTontBackground)

            // Presented as an overlay dialog card (not a `.sheet`), matching
            // Android's AlertDialog — it floats, dimmed, above this same
            // screen rather than pushing a separate full-screen page.
            if showAddJobDialog {
                AddJobSheet(strings: strings) { newJob in
                    jobs.append(newJob)
                    showAddJobDialog = false
                } onCancel: {
                    showAddJobDialog = false
                }
            }

            if showAddPromoDialog {
                AddPromotionSheet(strings: strings) { newPromo in
                    promotions.append(newPromo)
                    showAddPromoDialog = false
                } onCancel: {
                    showAddPromoDialog = false
                }
            }
        }
    }

    // MARK: - Top bar

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.editBusinessTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    // MARK: - Active status

    // Reuses the existing isActive field (already what the map/list filters
    // businesses on) as a reversible "hide my listing" toggle — deliberately
    // not a delete button. Placed first/most prominent since it affects
    // whether the business is visible at all, matching Android.
    private var activeStatusCard: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack {
                Text(strings.businessActiveStatus)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(isBusinessActive ? .meTontBlack : Color(red: 0.9, green: 0.32, blue: 0))
                Spacer()
                Toggle("", isOn: $isBusinessActive)
                    .labelsHidden()
                    .tint(.meTontRed)
            }
            Text(isBusinessActive ? strings.businessActiveDescription : strings.businessInactiveDescription)
                .font(.caption)
                .foregroundColor(.meTontGrey)
        }
        .padding(16)
        .background(isBusinessActive ? Color.white : Color(red: 1, green: 0.953, blue: 0.878))
        .overlay(
            RoundedRectangle(cornerRadius: 16)
                .stroke(isBusinessActive ? Color.clear : Color(red: 1, green: 0.651, blue: 0.149), lineWidth: 1)
        )
        .cornerRadius(16)
        .padding(.horizontal, 16)
    }

    // MARK: - Albanian owned

    private var albanianOwnedCard: some View {
        SectionCard(title: strings.albanianOwned) {
            HStack {
                Text(strings.albanianOwnedQuestion)
                    .fontWeight(.medium)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Toggle("", isOn: $isAlbanianOwned)
                    .labelsHidden()
                    .tint(.meTontRed)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Basic info

    private var basicInfoCard: some View {
        SectionCard(title: strings.basicInformationSection) {
            redField(icon: "storefront.fill", placeholder: strings.businessName, text: $name)

            Menu {
                ForEach(BusinessCategory.allCases) { cat in
                    Button(action: { category = cat }) {
                        Label(cat.displayName, systemImage: cat.iconName)
                    }
                }
            } label: {
                HStack {
                    Image(systemName: "square.grid.2x2.fill").foregroundColor(.meTontRed)
                    Text(category?.displayName ?? strings.categoryRequiredLabel)
                        .foregroundColor(category == nil ? .meTontGrey : .meTontBlack)
                    Spacer()
                    Image(systemName: "chevron.down").foregroundColor(.meTontGrey).font(.caption)
                }
                .padding(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
            }

            VStack(alignment: .trailing, spacing: 4) {
                TextEditor(text: $description)
                    .foregroundColor(.meTontBlack)
                    .scrollContentBackground(.hidden)
                    .background(Color.white)
                    .frame(height: 80)
                    .padding(8)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                    .overlay(alignment: .topLeading) {
                        if description.isEmpty {
                            Text(strings.descriptionRequiredLabel)
                                .foregroundColor(.meTontGrey)
                                .padding(.horizontal, 13)
                                .padding(.vertical, 16)
                                .allowsHitTesting(false)
                        }
                    }
                    .onChange(of: description) { _, newValue in
                        if newValue.count > 100 {
                            description = String(newValue.prefix(100))
                        }
                    }
                Text("\(description.count)/100")
                    .font(.caption2)
                    .foregroundColor(.meTontGrey)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Location

    private var locationCard: some View {
        SectionCard(title: strings.locationSectionShort) {
            redField(icon: "mappin.circle.fill", placeholder: strings.fullAddress, text: $address)

            HStack(spacing: 8) {
                TextField(strings.cityLabel, text: $city)
                    .padding(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                TextField(strings.countryLabel, text: $country)
                    .padding(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
            }

            if isGeocoding {
                HStack(spacing: 8) {
                    ProgressView().tint(.meTontRed)
                    Text(strings.locatingAddress)
                        .font(.caption)
                        .foregroundColor(.meTontGrey)
                }
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Contact

    private var contactCard: some View {
        SectionCard(title: strings.contactInformation) {
            redField(icon: "phone.fill", placeholder: strings.phoneNumber, text: $phone)
                .keyboardType(.phonePad)
            redField(icon: "envelope.fill", placeholder: strings.emailOptional, text: $email)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
            redField(icon: "globe", placeholder: strings.websiteOptional, text: $website)
                .keyboardType(.URL)
                .autocapitalization(.none)
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Working hours

    private var workingHoursCard: some View {
        SectionCard(title: strings.workingHoursSection) {
            HStack {
                Text(strings.open247).fontWeight(.medium)
                Spacer()
                Toggle("", isOn: $isOpen24Hours)
                    .labelsHidden()
                    .tint(.meTontRed)
            }

            if !isOpen24Hours {
                WorkingHoursEditorView(hours: $workingHours)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Photos

    private var photosCard: some View {
        SectionCard(title: strings.photos) {
            Text("\(existingPhotos.count + newPhotosData.count) / \(business.maxPhotos)")
                .font(.caption)
                .foregroundColor(.meTontGrey)

            if !existingPhotos.isEmpty || !newPhotosData.isEmpty {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(Array(existingPhotos.enumerated()), id: \.offset) { _, url in
                            AsyncImage(url: URL(string: url)) { image in
                                image.resizable().aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Color.gray.opacity(0.2)
                            }
                            .frame(width: 100, height: 100)
                            .clipped()
                            .cornerRadius(10)
                            .overlay(alignment: .topTrailing) {
                                Button(action: { existingPhotos.removeAll { $0 == url } }) {
                                    Image(systemName: "xmark.circle.fill")
                                        .foregroundColor(.white)
                                        .background(Circle().fill(Color.black.opacity(0.5)))
                                }
                                .padding(4)
                            }
                        }
                        ForEach(Array(newPhotosData.enumerated()), id: \.offset) { index, data in
                            if let uiImage = UIImage(data: data) {
                                Image(uiImage: uiImage)
                                    .resizable()
                                    .aspectRatio(contentMode: .fill)
                                    .frame(width: 100, height: 100)
                                    .clipped()
                                    .cornerRadius(10)
                                    .overlay(alignment: .topTrailing) {
                                        Button(action: { newPhotosData.remove(at: index) }) {
                                            Image(systemName: "xmark.circle.fill")
                                                .foregroundColor(.white)
                                                .background(Circle().fill(Color.black.opacity(0.5)))
                                        }
                                        .padding(4)
                                    }
                            }
                        }
                    }
                }
            }

            if remainingPhotoSlots > 0 {
                Button(action: { showImageSourceDialog = true }) {
                    HStack {
                        Image(systemName: "camera.fill.badge.ellipsis")
                        Text(strings.addPhoto).fontWeight(.semibold)
                    }
                    .frame(maxWidth: .infinity)
                    .frame(height: 48)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                    .foregroundColor(.meTontRed)
                }
                .confirmationDialog(strings.choosePhotoSource, isPresented: $showImageSourceDialog, titleVisibility: .visible) {
                    Button(strings.gallery) { showGalleryPicker = true }
                    Button(strings.camera) { showCameraPicker = true }
                }
                .photosPicker(isPresented: $showGalleryPicker, selection: $photosPickerItem, matching: .images)
                .onChange(of: photosPickerItem) { _, newItem in
                    Task {
                        if let data = try? await newItem?.loadTransferable(type: Data.self) {
                            await MainActor.run {
                                if remainingPhotoSlots > 0 {
                                    newPhotosData.append(data)
                                }
                                photosPickerItem = nil
                            }
                        }
                    }
                }
                .fullScreenCover(isPresented: $showCameraPicker) {
                    CameraPicker { data in
                        if remainingPhotoSlots > 0 {
                            newPhotosData.append(data)
                        }
                    }
                    .ignoresSafeArea()
                }
            } else {
                Text(strings.photoLimitReached)
                    .font(.caption)
                    .foregroundColor(.meTontGrey)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Jobs

    private var jobsCard: some View {
        SectionCard(title: strings.jobs) {
            if jobs.isEmpty {
                Text(strings.jobsEmptyTitle)
                    .font(.caption)
                    .foregroundColor(.meTontGrey)
            } else {
                ForEach(Array(jobs.enumerated()), id: \.offset) { index, job in
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(job.title)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0x67/255.0, green: 0x3A/255.0, blue: 0xB7/255.0))
                            Text(job.salary != nil ? "\(job.type) • \(job.salary!)" : job.type)
                                .font(.caption)
                                .foregroundColor(.meTontGrey)
                            Text(job.description)
                                .font(.caption)
                                .foregroundColor(.black.opacity(0.7))
                                .lineLimit(2)
                        }
                        Spacer()
                        Button(action: { jobs.remove(at: index) }) {
                            Image(systemName: "trash").foregroundColor(.meTontRed)
                        }
                    }
                    .padding(12)
                    .background(Color(red: 0xF3/255.0, green: 0xE5/255.0, blue: 0xF5/255.0))
                    .cornerRadius(10)
                }
            }

            Button(action: { showAddJobDialog = true }) {
                HStack {
                    Image(systemName: "plus")
                    Text(strings.addJobPostingTitle)
                }
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                .foregroundColor(.meTontRed)
            }
        }
        .padding(.horizontal, 16)
    }

    // MARK: - Promotions

    private var promotionsCard: some View {
        SectionCard(title: strings.promotions) {
            if promotions.isEmpty {
                Text(strings.promotionsEmptyTitle)
                    .font(.caption)
                    .foregroundColor(.meTontGrey)
            } else {
                ForEach(Array(promotions.enumerated()), id: \.offset) { index, promo in
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(promo.title)
                                .fontWeight(.bold)
                                .foregroundColor(Color(red: 0xF5/255.0, green: 0x7F/255.0, blue: 0x17/255.0))
                            Text(promo.description)
                                .font(.caption)
                                .foregroundColor(.meTontGrey)
                                .lineLimit(2)
                            if promo.discountCode != nil || promo.expiryDate != nil {
                                Text(promoMetaLine(promo))
                                    .font(.caption2)
                                    .fontWeight(.medium)
                                    .foregroundColor(Color(red: 0xF5/255.0, green: 0x7F/255.0, blue: 0x17/255.0))
                            }
                        }
                        Spacer()
                        Button(action: { promotions.remove(at: index) }) {
                            Image(systemName: "trash").foregroundColor(.meTontRed)
                        }
                    }
                    .padding(12)
                    .background(Color(red: 0xFF/255.0, green: 0xF9/255.0, blue: 0xC4/255.0))
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color(red: 0xFB/255.0, green: 0xC0/255.0, blue: 0x2D/255.0), lineWidth: 1))
                    .cornerRadius(10)
                }
            }

            Button(action: { showAddPromoDialog = true }) {
                HStack {
                    Image(systemName: "plus")
                    Text(strings.addPromotionTitle)
                }
                .frame(maxWidth: .infinity)
                .frame(height: 44)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                .foregroundColor(.meTontRed)
            }
        }
        .padding(.horizontal, 16)
    }

    private func promoMetaLine(_ promo: Promotion) -> String {
        var parts: [String] = []
        if let code = promo.discountCode {
            parts.append("\(strings.promotionCodePrefix)\(code)")
        }
        if let expiry = promo.expiryDate {
            let date = Date(timeIntervalSince1970: TimeInterval(expiry) / 1000)
            let formatter = DateFormatter()
            formatter.dateFormat = "MMM d, yyyy"
            parts.append("\(strings.promotionExpiresPrefix)\(formatter.string(from: date))")
        }
        return parts.joined(separator: "  •  ")
    }

    // MARK: - Save

    private var saveButton: some View {
        Button(action: save) {
            HStack {
                if isBusy {
                    ProgressView().tint(.white)
                    Text(isGeocoding ? strings.locatingAddress : strings.savingLabel)
                } else {
                    Image(systemName: "checkmark.circle.fill")
                    Text(strings.saveChanges).fontWeight(.bold)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(Color.meTontRed)
            .foregroundColor(.white)
            .cornerRadius(12)
        }
        .disabled(isBusy)
        .padding(.horizontal, 16)
    }

    private func save() {
        let trimmedName = name.trimmingCharacters(in: .whitespaces)
        let trimmedDescription = description.trimmingCharacters(in: .whitespaces)
        let trimmedAddress = address.trimmingCharacters(in: .whitespaces)
        let trimmedCity = city.trimmingCharacters(in: .whitespaces)
        let trimmedPhone = phone.trimmingCharacters(in: .whitespaces)

        if trimmedName.isEmpty {
            errorMessage = strings.businessNameRequired
            return
        }
        guard let category else {
            errorMessage = strings.selectCategory
            return
        }
        if trimmedDescription.isEmpty {
            errorMessage = strings.descriptionRequired
            return
        }
        if trimmedAddress.isEmpty {
            errorMessage = strings.addressRequired
            return
        }
        if trimmedCity.isEmpty {
            errorMessage = strings.cityRequired
            return
        }
        if trimmedPhone.isEmpty {
            errorMessage = strings.phoneRequired
            return
        }

        errorMessage = nil
        Task {
            await MainActor.run { isGeocoding = true }
            let fullAddress = "\(trimmedAddress), \(trimmedCity), \(country.trimmingCharacters(in: .whitespaces))"
            let coord = await LocationManager.geocodeAddress(fullAddress)
            await MainActor.run { isGeocoding = false }

            guard let coord else {
                await MainActor.run { errorMessage = strings.geocodeFailed }
                return
            }

            // Upload any newly picked photos first, then combine with whatever
            // existing photos survived the edit — never a blind replace, same
            // as Android's EditBusinessViewModel.updateBusiness.
            var finalPhotos = existingPhotos
            if !newPhotosData.isEmpty {
                await MainActor.run { isUploadingPhotos = true }
                var uploaded: [String] = []
                var startIndex = existingPhotos.count
                for data in newPhotosData {
                    do {
                        let url = try await FirestoreService.shared.uploadImage(businessId: business.id, data: data, index: startIndex)
                        uploaded.append(url)
                        startIndex += 1
                    } catch {
                        await MainActor.run {
                            isUploadingPhotos = false
                            errorMessage = strings.failedToUploadPhotos
                        }
                        return
                    }
                }
                await MainActor.run { isUploadingPhotos = false }
                finalPhotos = existingPhotos + uploaded
            }

            var updated = business
            updated.name = trimmedName
            updated.description = trimmedDescription
            updated.category = category.storageKey
            updated.address = trimmedAddress
            updated.city = trimmedCity
            updated.country = country.trimmingCharacters(in: .whitespaces)
            updated.phone = trimmedPhone
            updated.email = email.trimmingCharacters(in: .whitespaces)
            updated.website = website.trimmingCharacters(in: .whitespaces)
            updated.location = GeoPointLocation(latitude: coord.latitude, longitude: coord.longitude)
            updated.isOpen24Hours = isOpen24Hours
            updated.workingHours = isOpen24Hours ? [:] : workingHours
            updated.isAlbanianOwned = isAlbanianOwned
            updated.isActive = isBusinessActive
            updated.jobs = jobs
            updated.promotions = promotions
            updated.photos = finalPhotos

            await MainActor.run { isSubmitting = true }
            let result = await FirestoreService.shared.updateBusiness(updated)
            await MainActor.run { isSubmitting = false }

            switch result {
            case .success:
                await MainActor.run { onBusinessUpdated() }
            case .failure(let error):
                await MainActor.run { errorMessage = error.localizedDescription.isEmpty ? strings.failedToUpdateBusiness : error.localizedDescription }
            }
        }
    }

    // MARK: - Helpers

    private func redField(icon: String, placeholder: String, text: Binding<String>) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon).foregroundColor(.meTontRed)
            TextField(placeholder, text: text)
        }
        .padding(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
    }
}

// MARK: - Add Job sheet

private struct AddJobSheet: View {
    let strings: AppStrings
    let onAdd: (JobPosting) -> Void
    let onCancel: () -> Void

    @State private var title: String = ""
    @State private var description: String = ""
    @State private var type: String = "Full-time"
    @State private var salary: String = ""
    @State private var validationError: String?
    private let jobTypes = ["Full-time", "Part-time", "Contract"]

    var body: some View {
        DialogCard(onDismiss: onCancel) {
            Text(strings.addJobPostingTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.meTontRed)

            dialogField(strings.jobTitleLabel, text: $title)

            Menu {
                ForEach(jobTypes, id: \.self) { t in
                    Button(t) { type = t }
                }
            } label: {
                HStack {
                    Text(type).foregroundColor(.meTontBlack)
                    Spacer()
                    Image(systemName: "chevron.down").foregroundColor(.meTontGrey).font(.caption)
                }
                .padding(12)
                .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(strings.descriptionRequiredLabel).font(.caption).foregroundColor(.meTontGrey)
                TextEditor(text: $description)
                    .foregroundColor(.meTontBlack)
                    .scrollContentBackground(.hidden)
                    .background(Color.white)
                    .frame(height: 80)
                    .padding(8)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
            }

            dialogField(strings.jobSalaryPlaceholder, text: $salary)

            if let validationError {
                Text(validationError).font(.caption).foregroundColor(.red)
            }

            HStack {
                Spacer()
                Button(strings.cancel, action: onCancel)
                    .foregroundColor(.meTontGrey)
                Button(action: submit) {
                    Text(strings.addJobButton)
                        .fontWeight(.bold)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.meTontRed)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
        }
    }

    private func submit() {
        let trimmedTitle = title.trimmingCharacters(in: .whitespaces)
        let trimmedDescription = description.trimmingCharacters(in: .whitespaces)
        guard !trimmedTitle.isEmpty, !trimmedDescription.isEmpty else {
            validationError = strings.jobTitleDescRequired
            return
        }
        let trimmedSalary = salary.trimmingCharacters(in: .whitespaces)
        onAdd(JobPosting(title: trimmedTitle, description: trimmedDescription, type: type, salary: trimmedSalary.isEmpty ? nil : trimmedSalary))
    }
}

// MARK: - Add Promotion sheet

private struct AddPromotionSheet: View {
    let strings: AppStrings
    let onAdd: (Promotion) -> Void
    let onCancel: () -> Void

    @State private var title: String = ""
    @State private var description: String = ""
    @State private var discountCode: String = ""
    @State private var expiryDay: String = ""
    @State private var expiryMonth: String = ""
    @State private var expiryYear: String = ""
    @State private var validationError: String?

    var body: some View {
        DialogCard(onDismiss: onCancel) {
            Text(strings.addPromotionTitle)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.meTontRed)

            dialogField(strings.promotionTitleLabel, text: $title)

            VStack(alignment: .leading, spacing: 4) {
                Text(strings.descriptionRequiredLabel).font(.caption).foregroundColor(.meTontGrey)
                TextEditor(text: $description)
                    .foregroundColor(.meTontBlack)
                    .scrollContentBackground(.hidden)
                    .background(Color.white)
                    .frame(height: 80)
                    .padding(8)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
            }

            dialogField(strings.promotionDiscountCodeLabel, text: $discountCode)

            VStack(alignment: .leading, spacing: 6) {
                Text(strings.promotionExpirySection)
                    .font(.caption)
                    .foregroundColor(.meTontGrey)
                HStack(spacing: 8) {
                    dialogField(strings.dayPlaceholder, text: $expiryDay)
                        .keyboardType(.numberPad)
                    dialogField(strings.monthPlaceholder, text: $expiryMonth)
                        .keyboardType(.numberPad)
                    dialogField(strings.yearPlaceholder, text: $expiryYear)
                        .keyboardType(.numberPad)
                }
            }

            if let validationError {
                Text(validationError).font(.caption).foregroundColor(.red)
            }

            HStack {
                Spacer()
                Button(strings.cancel, action: onCancel)
                    .foregroundColor(.meTontGrey)
                Button(action: submit) {
                    Text(strings.addPromotionTitle)
                        .fontWeight(.bold)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 8)
                        .background(Color.meTontRed)
                        .foregroundColor(.white)
                        .cornerRadius(10)
                }
            }
        }
    }

    private func submit() {
        let trimmedTitle = title.trimmingCharacters(in: .whitespaces)
        let trimmedDescription = description.trimmingCharacters(in: .whitespaces)
        guard !trimmedTitle.isEmpty, !trimmedDescription.isEmpty else {
            validationError = strings.jobTitleDescRequired
            return
        }

        let anyExpiryEntered = !expiryDay.isEmpty || !expiryMonth.isEmpty || !expiryYear.isEmpty
        var expiryMillis: Int64? = nil
        if anyExpiryEntered {
            guard let day = Int(expiryDay), let month = Int(expiryMonth), let year = Int(expiryYear),
                  (1...31).contains(day), (1...12).contains(month), year >= 2024 else {
                validationError = strings.promotionInvalidExpiry
                return
            }
            var comps = DateComponents()
            comps.year = year
            comps.month = month
            comps.day = day
            comps.hour = 23
            comps.minute = 59
            comps.second = 59
            if let date = Calendar.current.date(from: comps) {
                expiryMillis = Int64(date.timeIntervalSince1970 * 1000)
            }
        }

        let trimmedCode = discountCode.trimmingCharacters(in: .whitespaces)
        onAdd(Promotion(title: trimmedTitle, description: trimmedDescription, discountCode: trimmedCode.isEmpty ? nil : trimmedCode, expiryDate: expiryMillis))
    }
}

// MARK: - Dialog card shell

// A centered, dimmed-backdrop card mirroring Android's Material AlertDialog
// (RoundedCornerShape(20.dp), tap-outside-to-dismiss scrim) — used instead of
// a `.sheet`/NavigationView so "Add Job Posting" and "Add Promotion" float
// over the Edit Business screen exactly like their Android counterparts,
// rather than pushing a separate full-screen page.
private struct DialogCard<Content: View>: View {
    let onDismiss: () -> Void
    @ViewBuilder let content: Content

    init(onDismiss: @escaping () -> Void, @ViewBuilder content: () -> Content) {
        self.onDismiss = onDismiss
        self.content = content()
    }

    var body: some View {
        ZStack {
            Color.black.opacity(0.4)
                .ignoresSafeArea()
                .onTapGesture(perform: onDismiss)

            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    content
                }
                .padding(20)
            }
            .frame(maxHeight: 480)
            .background(Color.white)
            .cornerRadius(20)
            .padding(.horizontal, 24)
        }
    }
}

private func dialogField(_ placeholder: String, text: Binding<String>) -> some View {
    TextField(placeholder, text: text)
        .padding(12)
        .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
}

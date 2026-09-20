// Bismillah Hir Rahman Nir Raheem
import SwiftUI
import PhotosUI
import UIKit

public struct AddBusinessScreen: View {
    @Environment(\.appStrings) private var strings

    @StateObject private var viewModel = AddBusinessViewModel()
    @State private var photosPickerItem: PhotosPickerItem? = nil
    @State private var showImageSourceDialog = false
    @State private var showGalleryPicker = false
    @State private var showCameraPicker = false
    public let onBackClick: () -> Void
    public let onBusinessAdded: () -> Void

    public init(onBackClick: @escaping () -> Void, onBusinessAdded: @escaping () -> Void) {
        self.onBackClick = onBackClick
        self.onBusinessAdded = onBusinessAdded
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            ScrollView {
                VStack(spacing: 12) {
                    requiredInfoCard
                    photoCard
                    locationCard
                    contactCard
                    workingHoursCard
                    albanianOwnedCard

                    if let err = viewModel.errorMessage {
                        Text(err)
                            .font(.caption)
                            .foregroundColor(.red)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }

                    submitButton

                    Spacer().frame(height: 20)
                }
                .padding(16)
            }
        }
        .background(Color.meTontBackground)
    }

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.registerBusiness)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    // MARK: - Required Info

    private var requiredInfoCard: some View {
        SectionCard(title: strings.requiredInformation) {
            redField(icon: "storefront.fill", placeholder: strings.businessName, text: $viewModel.name)

            categoryMenu

            VStack(alignment: .trailing, spacing: 4) {
                TextEditor(text: $viewModel.description)
                    .frame(height: 80)
                    .padding(8)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                    .onChange(of: viewModel.description) { _, newValue in
                        if newValue.count > 100 {
                            viewModel.description = String(newValue.prefix(100))
                        }
                    }
                Text("\(viewModel.description.count)/100")
                    .font(.caption2)
                    .foregroundColor(.meTontGrey)
            }
        }
    }

    private var categoryMenu: some View {
        Menu {
            ForEach(BusinessCategory.allCases) { cat in
                Button(action: { viewModel.category = cat }) {
                    Label(cat.displayName, systemImage: cat.iconName)
                }
            }
        } label: {
            HStack {
                Image(systemName: "square.grid.2x2.fill").foregroundColor(.meTontRed)
                Text(viewModel.category?.displayName ?? strings.selectCategory)
                    .foregroundColor(viewModel.category == nil ? .meTontGrey : .meTontBlack)
                Spacer()
                Image(systemName: "chevron.down").foregroundColor(.meTontGrey).font(.caption)
            }
            .padding(12)
            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
        }
    }

    // MARK: - Photo

    // A new business always starts on the free tier, which caps it to a single
    // photo (Business.maxPhotos) — same single-image flow as Android's
    // AddBusinessScreen "Photo (Optional)" card (Gallery picker, thumbnail with a
    // remove button, Add Photo disabled once one is picked). Camera capture isn't
    // wired up here yet — PhotosPicker covers the gallery half of Android's
    // gallery-or-camera chooser without needing any new camera permission.
    private var photoCard: some View {
        SectionCard(title: strings.photoOptional) {
            if let data = viewModel.selectedPhotoData, let uiImage = UIImage(data: data) {
                ZStack(alignment: .topTrailing) {
                    Image(uiImage: uiImage)
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                        .frame(height: 160)
                        .frame(maxWidth: .infinity)
                        .clipped()
                        .cornerRadius(12)

                    Button(action: {
                        viewModel.selectedPhotoData = nil
                        photosPickerItem = nil
                    }) {
                        Image(systemName: "xmark.circle.fill")
                            .font(.title2)
                            .foregroundColor(.white)
                            .background(Circle().fill(Color.black.opacity(0.5)))
                    }
                    .padding(8)
                }
            } else {
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
                            await MainActor.run { viewModel.selectedPhotoData = data }
                        }
                    }
                }
                .fullScreenCover(isPresented: $showCameraPicker) {
                    CameraPicker { data in
                        viewModel.selectedPhotoData = data
                    }
                    .ignoresSafeArea()
                }
            }

            if viewModel.isUploadingPhoto {
                HStack(spacing: 8) {
                    ProgressView().tint(.meTontRed)
                    Text(strings.uploadingPhoto)
                        .font(.caption)
                        .foregroundColor(.meTontGrey)
                }
            }
        }
    }

    // MARK: - Location

    private var locationCard: some View {
        SectionCard(title: strings.locationSection) {
            redField(icon: "mappin.circle.fill", placeholder: strings.fullAddress, text: $viewModel.address)

            HStack(spacing: 8) {
                TextField(strings.cityLabel, text: $viewModel.city)
                    .padding(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
                TextField(strings.countryLabel, text: $viewModel.country)
                    .padding(12)
                    .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed.opacity(0.4), lineWidth: 1))
            }

            if viewModel.isGeocoding {
                HStack(spacing: 8) {
                    ProgressView().tint(.meTontRed)
                    Text(strings.locatingAddress)
                        .font(.caption)
                        .foregroundColor(.meTontGrey)
                }
            }
        }
    }

    // MARK: - Contact

    private var contactCard: some View {
        SectionCard(title: strings.contactInformation) {
            redField(icon: "phone.fill", placeholder: strings.phoneNumber, text: $viewModel.phone)
                .keyboardType(.phonePad)
            redField(icon: "envelope.fill", placeholder: strings.emailOptional, text: $viewModel.email)
                .keyboardType(.emailAddress)
                .autocapitalization(.none)
            redField(icon: "globe", placeholder: strings.websiteOptional, text: $viewModel.website)
                .keyboardType(.URL)
                .autocapitalization(.none)
        }
    }

    // MARK: - Working Hours

    private var workingHoursCard: some View {
        SectionCard(title: strings.workingHoursSection) {
            HStack {
                Text(strings.open247).fontWeight(.medium)
                Spacer()
                Toggle("", isOn: $viewModel.isOpen24Hours)
                    .labelsHidden()
                    .tint(.meTontRed)
            }

            if !viewModel.isOpen24Hours {
                WorkingHoursEditorView(hours: $viewModel.workingHours)
            }
        }
    }

    // MARK: - Albanian Owned

    private var albanianOwnedCard: some View {
        SectionCard(title: strings.albanianOwned) {
            HStack {
                Text(strings.albanianOwnedQuestion)
                    .fontWeight(.medium)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Toggle("", isOn: $viewModel.isAlbanianOwned)
                    .labelsHidden()
                    .tint(.meTontRed)
            }
        }
    }

    // MARK: - Submit

    private var submitButton: some View {
        Button(action: {
            Task {
                let success = await viewModel.submit(ownerId: AuthManager.shared.currentUser?.uid ?? "", strings: strings)
                if success { onBusinessAdded() }
            }
        }) {
            HStack {
                if viewModel.isSubmitting || viewModel.isGeocoding || viewModel.isUploadingPhoto {
                    ProgressView().tint(.white)
                    Text(viewModel.isGeocoding ? strings.locatingAddress : (viewModel.isUploadingPhoto ? strings.uploadingPhoto : strings.registering))
                } else {
                    Image(systemName: "checkmark.circle.fill")
                    Text(strings.registerBusinessButton).fontWeight(.bold)
                }
            }
            .frame(maxWidth: .infinity)
            .frame(height: 52)
            .background(Color.meTontRed)
            .foregroundColor(.white)
            .cornerRadius(12)
        }
        .disabled(viewModel.isSubmitting || viewModel.isGeocoding || viewModel.isUploadingPhoto)
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


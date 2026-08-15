// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct AddBusinessScreen: View {
    @Environment(\.appStrings) private var strings
    
    @StateObject private var viewModel = AddBusinessViewModel()
    public let onBackClick: () -> Void
    public let onBusinessAdded: () -> Void
    
    public init(onBackClick: @escaping () -> Void, onBusinessAdded: @escaping () -> Void) {
        self.onBackClick = onBackClick
        self.onBusinessAdded = onBusinessAdded
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.registerBusiness)
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            Form {
                Section(header: Text(strings.requiredInformation)) {
                    TextField(strings.businessName, text: $viewModel.name)
                    
                    Picker(strings.selectCategory, selection: $viewModel.category) {
                        ForEach(BusinessCategory.allCases) { cat in
                            Text(cat.displayName).tag(cat)
                        }
                    }
                    
                    TextField(strings.eventDescriptionRequired, text: $viewModel.description)
                }
                
                Section(header: Text(strings.locationSection)) {
                    TextField(strings.fullAddress, text: $viewModel.address)
                    TextField(strings.cityLabel, text: $viewModel.city)
                    TextField(strings.countryLabel, text: $viewModel.country)
                    
                    Button(action: {
                        Task { await viewModel.geocodeCurrentAddress() }
                    }) {
                        HStack {
                            Image(systemName: "mappin.and.ellipse")
                            Text(viewModel.isLocating ? strings.locatingAddress : strings.pickLocationFromMap)
                        }
                    }
                    
                    HStack {
                        TextField(strings.latitude, text: $viewModel.latitudeString)
                        TextField(strings.longitude, text: $viewModel.longitudeString)
                    }
                }
                
                Section(header: Text(strings.contactInformation)) {
                    TextField(strings.phoneNumber, text: $viewModel.phone)
                    TextField(strings.emailOptional, text: $viewModel.email)
                    TextField(strings.websiteOptional, text: $viewModel.website)
                }
                
                Section {
                    Toggle(strings.open247, isOn: $viewModel.isOpen24Hours)
                    Toggle(strings.albanianOwned, isOn: $viewModel.isAlbanianOwned)
                }
                
                if let err = viewModel.errorMessage {
                    Text(err).foregroundColor(.red).font(.caption)
                }
                
                Button(action: {
                    Task {
                        let success = await viewModel.submit(ownerId: AuthManager.shared.currentUser?.uid ?? "")
                        if success { onBusinessAdded() }
                    }
                }) {
                    Text(viewModel.isSubmitting ? strings.registering : strings.registerBusinessButton)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .foregroundColor(.white)
                        .background(Color.meTontRed)
                        .cornerRadius(10)
                }
            }
        }
        .background(Color.meTontBackground)
    }
}

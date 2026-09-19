// Bismillah Hir Rahman Nir Raheem
import SwiftUI

public struct EditBusinessScreen: View {
    @Environment(\.appStrings) private var strings
    
    public var business: Business
    public let onBackClick: () -> Void
    public let onBusinessUpdated: () -> Void
    
    @State private var name: String = ""
    @State private var description: String = ""
    @State private var phone: String = ""
    @State private var email: String = ""
    @State private var website: String = ""
    @State private var isSubmitting: Bool = false
    
    public init(business: Business, onBackClick: @escaping () -> Void, onBusinessUpdated: @escaping () -> Void) {
        self.business = business
        self.onBackClick = onBackClick
        self.onBusinessUpdated = onBusinessUpdated
        _name = State(initialValue: business.name)
        _description = State(initialValue: business.description)
        _phone = State(initialValue: business.phone)
        _email = State(initialValue: business.email)
        _website = State(initialValue: business.website)
    }
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.editBusiness)
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            Form {
                Section(header: Text(strings.requiredInformation)) {
                    TextField(strings.businessName, text: $name)
                    TextField(strings.eventDescriptionRequired, text: $description)
                }
                
                Section(header: Text(strings.contactInformation)) {
                    TextField(strings.phoneNumber, text: $phone)
                    TextField(strings.emailOptional, text: $email)
                    TextField(strings.websiteOptional, text: $website)
                }
                
                Button(action: {
                    var updated = business
                    updated.name = name
                    updated.description = description
                    updated.phone = phone
                    updated.email = email
                    updated.website = website
                    
                    isSubmitting = true
                    Task {
                        _ = await FirestoreService.shared.updateBusiness(updated)
                        await MainActor.run {
                            isSubmitting = false
                            onBusinessUpdated()
                        }
                    }
                }) {
                    Text(isSubmitting ? strings.loading : strings.save)
                        .fontWeight(.bold)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .foregroundColor(.white)
                        .background(Color.meTontRed)
                        .cornerRadius(10)
                }
            }
        }
    }
}

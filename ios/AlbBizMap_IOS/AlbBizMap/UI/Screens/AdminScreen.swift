// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Mirrors Android's AdminScreen.kt: three sections off the same claim_requests
// listener (Business Claims / Verification Requests, split by
// ClaimRequest.type) plus a live Businesses & Plans table with a Clear
// Expired Plans cleanup action. Gated behind isUserAdmin — the nav entry
// point itself is already admin-only (UserProfileScreen), this is a second,
// server-checked gate.
public struct AdminScreen: View {
    @Environment(\.appStrings) private var strings
    @StateObject private var viewModel = AdminViewModel()
    public let currentUserId: String
    public let onBackClick: () -> Void

    public init(currentUserId: String, onBackClick: @escaping () -> Void) {
        self.currentUserId = currentUserId
        self.onBackClick = onBackClick
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            if let message = viewModel.message {
                Text(message)
                    .font(.caption)
                    .foregroundColor(.white)
                    .frame(maxWidth: .infinity)
                    .padding(10)
                    .background(Color.meTontRed)
                    .onAppear {
                        DispatchQueue.main.asyncAfter(deadline: .now() + 3) {
                            viewModel.clearMessage()
                        }
                    }
            }

            if !viewModel.isAdmin {
                accessDenied
            } else if viewModel.isLoading {
                VStack {
                    Spacer()
                    ProgressView().tint(.meTontRed)
                    Spacer()
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(Color(red: 0.961, green: 0.961, blue: 0.961))
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        adminSectionHeader(title: strings.adminBusinessClaims, count: viewModel.businessClaims.count)
                        if viewModel.businessClaims.isEmpty {
                            adminEmptyState(title: strings.noPendingClaims, subtitle: strings.allClaimsProcessed)
                        } else {
                            ForEach(viewModel.businessClaims) { claim in
                                claimRequestCard(claim: claim, typeLabel: strings.adminBusinessClaims)
                            }
                        }

                        Spacer().frame(height: 4)

                        adminSectionHeader(title: strings.adminVerificationRequests, count: viewModel.verificationRequests.count)
                        if viewModel.verificationRequests.isEmpty {
                            adminEmptyState(title: strings.adminNoPendingVerification, subtitle: strings.adminAllVerificationProcessed)
                        } else {
                            ForEach(viewModel.verificationRequests) { claim in
                                claimRequestCard(claim: claim, typeLabel: strings.adminVerificationRequests)
                            }
                        }

                        Spacer().frame(height: 4)

                        adminSectionHeader(title: strings.adminBusinessesAndPlans, count: viewModel.businessesWithPlans.count)
                        businessesPlansTable

                        // Stopgap for the missing RTDN/Cloud-Function pipeline (see
                        // Business.isEffectivelyPremium etc.) — the table above
                        // deliberately shows raw stored flags so stale ones are
                        // visible; this button is what actually clears them out of
                        // Firestore once spotted.
                        Button(action: { viewModel.clearExpiredPlans(strings: strings) }) {
                            HStack {
                                Image(systemName: "sparkles")
                                Text(strings.clearExpiredPlansButton).fontWeight(.bold)
                            }
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 12)
                            .overlay(RoundedRectangle(cornerRadius: 12).stroke(Color.meTontRed, lineWidth: 1))
                            .foregroundColor(.meTontRed)
                        }
                        .disabled(viewModel.isLoading)

                        Spacer().frame(height: 16)
                    }
                    .padding(16)
                }
                .background(Color(red: 0.961, green: 0.961, blue: 0.961))
            }
        }
        .background(Color.meTontBackground)
        .onAppear {
            viewModel.checkAdminStatus(userId: currentUserId)
        }
    }

    // MARK: - Top bar

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text("Admin Panel")
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    // MARK: - Access denied

    private var accessDenied: some View {
        VStack(spacing: 12) {
            Spacer()
            ZStack {
                Circle().fill(Color.meTontRed.opacity(0.1)).frame(width: 80, height: 80)
                Image(systemName: "lock.fill").font(.system(size: 32)).foregroundColor(.meTontRed)
            }
            Text("Access Denied")
                .font(.title3).fontWeight(.bold).foregroundColor(.meTontRed)
            Text("You don't have admin privileges.")
                .font(.subheadline)
                .foregroundColor(.meTontGrey)
                .multilineTextAlignment(.center)
            Spacer()
        }
        .padding(32)
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color(red: 0.961, green: 0.961, blue: 0.961))
    }

    // MARK: - Section header / empty state

    private func adminSectionHeader(title: String, count: Int) -> some View {
        HStack {
            Text(title)
                .font(.title3)
                .fontWeight(.bold)
                .foregroundColor(.black)
            Spacer()
            Text("\(count)")
                .font(.caption)
                .fontWeight(.bold)
                .foregroundColor(count == 0 ? Color(red: 0.298, green: 0.686, blue: 0.314) : .meTontRed)
                .padding(.horizontal, 12)
                .padding(.vertical, 4)
                .background((count == 0 ? Color(red: 0.298, green: 0.686, blue: 0.314) : Color.meTontRed).opacity(0.1))
                .cornerRadius(12)
        }
    }

    private func adminEmptyState(title: String, subtitle: String) -> some View {
        VStack(spacing: 12) {
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 40))
                .foregroundColor(Color(red: 0.298, green: 0.686, blue: 0.314))
            Text(title)
                .font(.headline)
                .fontWeight(.bold)
                .foregroundColor(Color(red: 0.298, green: 0.686, blue: 0.314))
            Text(subtitle)
                .font(.caption)
                .foregroundColor(.meTontGrey)
                .multilineTextAlignment(.center)
        }
        .frame(maxWidth: .infinity)
        .padding(32)
        .background(Color.white)
        .cornerRadius(16)
    }

    // MARK: - Businesses & Plans table

    private var businessesPlansTable: some View {
        VStack(spacing: 0) {
            if viewModel.businessesWithPlans.isEmpty {
                Text(strings.adminNoActivePlans)
                    .font(.caption)
                    .foregroundColor(.meTontGrey)
                    .frame(maxWidth: .infinity)
                    .padding(32)
            } else {
                HStack {
                    Text(strings.adminTableBusinessColumn)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    Text(strings.adminTableTierColumn)
                        .frame(width: 76, alignment: .leading)
                    Text(strings.adminTableExpiresColumn)
                        .frame(width: 84, alignment: .leading)
                }
                .font(.caption2)
                .fontWeight(.bold)
                .foregroundColor(.meTontGrey)
                .padding(.horizontal, 16)
                .padding(.vertical, 10)
                .background(Color(red: 0.980, green: 0.980, blue: 0.980))

                Divider()

                ForEach(Array(viewModel.businessesWithPlans.enumerated()), id: \.element.id) { index, business in
                    businessPlanRow(business)
                    if index < viewModel.businessesWithPlans.count - 1 {
                        Divider()
                    }
                }
            }
        }
        .background(Color.white)
        .cornerRadius(16)
    }

    private func businessPlanRow(_ business: Business) -> some View {
        let tierLabel: String
        let tierColor: Color
        let expiry: Int64?
        if business.isSponsored {
            tierLabel = strings.sponsored; tierColor = .tierGold; expiry = business.sponsoredUntil
        } else if business.isFeatured {
            tierLabel = strings.featured2; tierColor = .tierSilver; expiry = business.premiumUntil
        } else if business.isPremium {
            tierLabel = strings.premium; tierColor = .tierBronze; expiry = business.premiumUntil
        } else {
            tierLabel = ""; tierColor = .meTontGrey; expiry = nil
        }
        let isExpired = (expiry ?? 0) > 0 && expiry! < Int64(Date().timeIntervalSince1970 * 1000)

        return HStack(alignment: .top) {
            Text(business.name)
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundColor(.black)
                .lineLimit(1)
                .frame(maxWidth: .infinity, alignment: .leading)

            Text(tierLabel)
                .font(.caption2)
                .fontWeight(.bold)
                .foregroundColor(tierColor)
                .padding(.horizontal, 8)
                .padding(.vertical, 3)
                .background(tierColor.opacity(0.15))
                .cornerRadius(8)
                .frame(width: 76, alignment: .leading)

            VStack(alignment: .leading, spacing: 2) {
                Text(expiry.map { formattedShortDate($0) } ?? strings.adminNoExpiry)
                    .font(.caption)
                    .fontWeight(isExpired ? .bold : .regular)
                    .foregroundColor(isExpired ? .meTontRed : .meTontGrey)
                // Raw stored flag is still true even though the date's passed —
                // that's exactly what "Clear Expired Plans" above targets.
                if isExpired {
                    Text(strings.expiredLabel)
                        .font(.caption2)
                        .fontWeight(.bold)
                        .foregroundColor(.meTontRed)
                }
            }
            .frame(width: 84, alignment: .leading)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 10)
    }

    private func formattedShortDate(_ millis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(millis) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM d, yyyy"
        return formatter.string(from: date)
    }

    // MARK: - Claim request card

    private func claimRequestCard(claim: ClaimRequest, typeLabel: String) -> some View {
        ClaimRequestCardView(
            claim: claim,
            typeLabel: typeLabel,
            onApprove: { viewModel.approveClaim(claim) },
            onReject: { viewModel.rejectClaim(claim.id) }
        )
    }
}

// MARK: - Claim request card view

private struct ClaimRequestCardView: View {
    @Environment(\.appStrings) private var strings
    let claim: ClaimRequest
    let typeLabel: String
    let onApprove: () -> Void
    let onReject: () -> Void

    @State private var showApproveConfirm = false
    @State private var showRejectConfirm = false

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            HStack(spacing: 10) {
                ZStack {
                    Circle().fill(Color.meTontRed.opacity(0.1)).frame(width: 36, height: 36)
                    Image(systemName: "building.2.fill").font(.system(size: 16)).foregroundColor(.meTontRed)
                }
                VStack(alignment: .leading, spacing: 2) {
                    Text(claim.businessName)
                        .font(.headline)
                        .fontWeight(.bold)
                        .foregroundColor(.black)
                    Text(typeLabel)
                        .font(.caption2)
                        .foregroundColor(.meTontRed)
                }
            }

            Divider()

            HStack(spacing: 6) {
                Image(systemName: "person.fill").font(.caption).foregroundColor(.meTontGrey)
                Text(claim.userEmail).font(.caption).foregroundColor(.meTontGrey)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(strings.claimReasonLabel)
                    .font(.caption2)
                    .fontWeight(.bold)
                    .foregroundColor(.meTontRed)
                Text(claim.reason)
                    .font(.caption)
                    .foregroundColor(.black)
            }
            .padding(10)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(Color(red: 0.961, green: 0.961, blue: 0.961))
            .cornerRadius(8)

            HStack(spacing: 4) {
                Image(systemName: "calendar").font(.caption2).foregroundColor(.meTontGrey)
                Text(formattedDate(claim.createdAt)).font(.caption2).foregroundColor(.meTontGrey)
            }

            HStack(spacing: 8) {
                Button(action: { showRejectConfirm = true }) {
                    HStack {
                        Image(systemName: "xmark").font(.caption)
                        Text(strings.reject).fontWeight(.medium)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .overlay(RoundedRectangle(cornerRadius: 10).stroke(Color.meTontRed, lineWidth: 1))
                    .foregroundColor(.meTontRed)
                }
                Button(action: { showApproveConfirm = true }) {
                    HStack {
                        Image(systemName: "checkmark").font(.caption)
                        Text(strings.approve).fontWeight(.medium)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 10)
                    .background(Color(red: 0.298, green: 0.686, blue: 0.314))
                    .foregroundColor(.white)
                    .cornerRadius(10)
                }
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
        .alert(strings.approveClaimTitle, isPresented: $showApproveConfirm) {
            Button(strings.cancel, role: .cancel) {}
            Button(strings.approve) { onApprove() }
        } message: {
            Text(String(format: strings.approveClaimMessage, claim.userEmail, claim.businessName))
        }
        .alert(strings.rejectClaimTitle, isPresented: $showRejectConfirm) {
            Button(strings.cancel, role: .cancel) {}
            Button(strings.reject, role: .destructive) { onReject() }
        } message: {
            Text(String(format: strings.rejectClaimMessage, claim.userEmail))
        }
    }

    private func formattedDate(_ millis: Int64) -> String {
        let date = Date(timeIntervalSince1970: TimeInterval(millis) / 1000)
        let formatter = DateFormatter()
        formatter.dateFormat = "MMM dd, yyyy"
        return "Submitted: \(formatter.string(from: date))"
    }
}

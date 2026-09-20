// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

// Mirrors Android's AdminViewModel.kt: business claims and verification
// requests split from the same claim_requests listener by ClaimRequest.type,
// plus a live "Businesses & Plans" table (any business currently on a paid
// tier) with a "Clear Expired Plans" cleanup action.
public class AdminViewModel: ObservableObject {
    @Published public var isAdmin: Bool = false
    @Published public var businessClaims: [ClaimRequest] = []
    @Published public var verificationRequests: [ClaimRequest] = []
    @Published public var businessesWithPlans: [Business] = []
    @Published public var isLoading: Bool = false
    @Published public var message: String? = nil

    private var cancellables = Set<AnyCancellable>()
    private var isClearingExpired = false

    public init() {
        FirestoreService.shared.$claimRequests
            .map { claims in claims.filter { $0.type != "verification" } }
            .receive(on: DispatchQueue.main)
            .assign(to: \.businessClaims, on: self)
            .store(in: &cancellables)

        FirestoreService.shared.$claimRequests
            .map { claims in claims.filter { $0.type == "verification" } }
            .receive(on: DispatchQueue.main)
            .assign(to: \.verificationRequests, on: self)
            .store(in: &cancellables)

        // Businesses currently on a paid tier (Premium/Featured/Sponsored), for
        // the Businesses & Plans table. Sourced from the same `businesses` feed
        // the map screen uses (already active-only) — there's no separate
        // "plans" collection, tier flags just live on the Business doc itself.
        FirestoreService.shared.$businesses
            .map { businesses in
                businesses
                    .filter { $0.isPremium || $0.isFeatured || $0.isSponsored }
                    .sorted { a, b in
                        if a.isSponsored != b.isSponsored { return a.isSponsored }
                        if a.isFeatured != b.isFeatured { return a.isFeatured }
                        if a.isPremium != b.isPremium { return a.isPremium }
                        return false
                    }
            }
            .receive(on: DispatchQueue.main)
            .assign(to: \.businessesWithPlans, on: self)
            .store(in: &cancellables)
    }

    public func checkAdminStatus(userId: String) {
        Task {
            let admin = await FirestoreService.shared.isUserAdmin(userId: userId)
            await MainActor.run { self.isAdmin = admin }
        }
    }

    public func approveClaim(_ claim: ClaimRequest) {
        Task {
            _ = await FirestoreService.shared.approveClaim(claim)
        }
    }

    public func rejectClaim(_ claimId: String) {
        Task {
            _ = await FirestoreService.shared.rejectClaim(claimId: claimId)
        }
    }

    // Same double-tap guard as Android's clearExpiredMutex — this writes one
    // update per stale business, so a second concurrent tap could double up on
    // writes mid-flight. businessesWithPlans refreshes on its own afterward
    // since it's a live listener, not something this needs to manually re-fetch.
    public func clearExpiredPlans(strings: AppStrings) {
        guard !isClearingExpired else { return }
        isClearingExpired = true
        Task {
            await MainActor.run { self.isLoading = true }
            let result = await FirestoreService.shared.clearExpiredPlans(businessesWithPlans)
            await MainActor.run {
                self.isLoading = false
                switch result {
                case .success(let count):
                    self.message = count == 0
                        ? strings.clearExpiredPlansNoneFound
                        : String(format: strings.clearExpiredPlansSuccessTemplate, count)
                case .failure(let error):
                    self.message = "\(strings.clearExpiredPlansFailedPrefix): \(error.localizedDescription)"
                }
                self.isClearingExpired = false
            }
        }
    }

    public func clearMessage() {
        message = nil
    }
}

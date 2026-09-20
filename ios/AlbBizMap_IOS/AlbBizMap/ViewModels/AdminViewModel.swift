// Bismillah Hir Rahman Nir Raheem
import Foundation
import Combine

public class AdminViewModel: ObservableObject {
    @Published public var pendingClaims: [ClaimRequest] = []
    @Published public var isLoading: Bool = false
    
    private var cancellables = Set<AnyCancellable>()
    
    public init() {
        FirestoreService.shared.$claimRequests
            .map { $0.filter { $0.status == "pending" } }
            .assign(to: \.pendingClaims, on: self)
            .store(in: &cancellables)
    }
    
    public func approve(claim: ClaimRequest) async {
        _ = await FirestoreService.shared.approveClaim(claim)
    }
    
    public func reject(claimId: String) async {
        _ = await FirestoreService.shared.rejectClaim(claimId: claimId)
    }
}

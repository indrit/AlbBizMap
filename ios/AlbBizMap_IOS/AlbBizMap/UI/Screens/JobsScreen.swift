// Bismillah Hir Rahman Nir Raheem
import SwiftUI

// Same accent used for job postings on a business's own detail page —
// kept consistent here since this screen just gathers every business's
// postings into one place (matches Android's JobAccent).
private let jobAccent = Color(red: 0x67 / 255.0, green: 0x3A / 255.0, blue: 0xB7 / 255.0)

public struct JobsScreen: View {
    @Environment(\.appStrings) private var strings

    @ObservedObject public var viewModel: MapViewModel
    public let onBackClick: () -> Void
    public let onBusinessClick: (String) -> Void

    public init(viewModel: MapViewModel, onBackClick: @escaping () -> Void, onBusinessClick: @escaping (String) -> Void) {
        self.viewModel = viewModel
        self.onBackClick = onBackClick
        self.onBusinessClick = onBusinessClick
    }

    private var allJobs: [(Business, JobPosting)] {
        viewModel.businesses
            .flatMap { biz in biz.jobs.map { (biz, $0) } }
            .sorted { $0.1.postedAt > $1.1.postedAt }
    }

    public var body: some View {
        VStack(spacing: 0) {
            topAppBar

            if allJobs.isEmpty {
                emptyState
            } else {
                ScrollView {
                    VStack(alignment: .leading, spacing: 12) {
                        Text("\(allJobs.count) open \(allJobs.count == 1 ? "position" : "positions")")
                            .font(.system(size: 13))
                            .foregroundColor(.meTontGrey)

                        ForEach(allJobs, id: \.1.id) { biz, job in
                            JobListingCard(
                                business: biz,
                                job: job,
                                onViewProfileClick: { onBusinessClick(biz.id) }
                            )
                        }
                    }
                    .padding(16)
                }
            }
        }
        .background(Color.meTontBackground)
    }

    private var topAppBar: some View {
        HStack(spacing: 8) {
            Button(action: onBackClick) {
                Image(systemName: "chevron.left").foregroundColor(.white)
            }
            Text(strings.jobs)
                .font(.system(size: 18, weight: .bold))
                .foregroundColor(.white)
            Spacer()
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.meTontRed)
    }

    private var emptyState: some View {
        VStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 40)
                    .fill(jobAccent.opacity(0.1))
                    .frame(width: 80, height: 80)
                Image(systemName: "briefcase.fill")
                    .font(.system(size: 34))
                    .foregroundColor(jobAccent.opacity(0.5))
            }
            Text(strings.jobsEmptyTitle)
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(.meTontGrey)
            Text(strings.jobsEmptySubtitle)
                .font(.system(size: 13))
                .foregroundColor(.meTontGrey.opacity(0.7))
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}

private struct JobListingCard: View {
    @Environment(\.appStrings) private var strings
    let business: Business
    let job: JobPosting
    let onViewProfileClick: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            // Business name / category
            HStack(spacing: 8) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(Color.meTontRed)
                    .frame(width: 8, height: 8)
                Text(business.name)
                    .font(.system(size: 14, weight: .semibold))
                    .foregroundColor(.meTontRed)
                if !business.category.trimmingCharacters(in: .whitespaces).isEmpty {
                    Text("• \(BusinessCategory.displayName(for: business.category))")
                        .font(.system(size: 12))
                        .foregroundColor(.meTontGrey)
                }
            }

            Divider().background(Color(red: 0xF0/255.0, green: 0xF0/255.0, blue: 0xF0/255.0))

            // Job title
            Text(job.title)
                .font(.system(size: 17, weight: .bold))
                .foregroundColor(.black)

            // Type / salary
            HStack(spacing: 8) {
                Text(job.type)
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(jobAccent)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(jobAccent.opacity(0.12))
                    .cornerRadius(6)

                if let salary = job.salary, !salary.isEmpty {
                    Text("💰 \(salary)")
                        .font(.system(size: 13, weight: .medium))
                        .foregroundColor(.meTontGrey)
                }
            }

            // Description
            Text(job.description)
                .font(.system(size: 13))
                .foregroundColor(.meTontGrey)
                .lineLimit(3)

            Spacer().frame(height: 4)

            // View Profile button
            Button(action: onViewProfileClick) {
                Text(strings.viewProfile)
                    .fontWeight(.bold)
                    .frame(maxWidth: .infinity)
                    .frame(height: 44)
                    .background(Color.meTontRed)
                    .foregroundColor(.white)
                    .cornerRadius(12)
            }
        }
        .padding(16)
        .background(Color.white)
        .cornerRadius(16)
        .shadow(color: Color.black.opacity(0.06), radius: 4, x: 0, y: 2)
    }
}

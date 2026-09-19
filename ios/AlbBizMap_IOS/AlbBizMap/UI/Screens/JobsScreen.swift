// Bismillah Hir Rahman Nir Raheem
import SwiftUI

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
    
    public var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onBackClick) {
                    Image(systemName: "chevron.left").font(.title2).foregroundColor(.meTontBlack)
                }
                Text(strings.jobs)
                    .font(.title2).fontWeight(.bold)
                Spacer()
            }
            .padding().background(Color.white)
            
            let allJobs = viewModel.businesses.flatMap { biz in
                biz.jobs.map { (biz, $0) }
            }
            
            if allJobs.isEmpty {
                VStack {
                    Spacer()
                    Image(systemName: "briefcase").font(.system(size: 48)).foregroundColor(.gray)
                    Text("No job postings available").foregroundColor(.gray)
                    Spacer()
                }
            } else {
                ScrollView {
                    VStack(spacing: 12) {
                        ForEach(allJobs, id: \.1.id) { biz, job in
                            VStack(alignment: .leading, spacing: 8) {
                                HStack {
                                    Text(job.title)
                                        .font(.headline)
                                        .fontWeight(.bold)
                                    Spacer()
                                    Text(job.type)
                                        .font(.caption)
                                        .padding(.horizontal, 8)
                                        .padding(.vertical, 4)
                                        .background(Color.blue.opacity(0.1))
                                        .foregroundColor(.blue)
                                        .cornerRadius(6)
                                }
                                Text(biz.name)
                                    .font(.subheadline)
                                    .foregroundColor(.meTontRed)
                                
                                Text(job.description)
                                    .font(.caption)
                                    .foregroundColor(.secondary)
                            }
                            .padding(14)
                            .background(Color.white)
                            .cornerRadius(12)
                            .shadow(color: Color.black.opacity(0.05), radius: 3, x: 0, y: 1)
                            .onTapGesture {
                                onBusinessClick(biz.id)
                            }
                        }
                    }
                    .padding(16)
                }
            }
        }
        .background(Color.meTontBackground)
    }
}

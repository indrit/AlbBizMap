// swift-tools-version:5.9
// Bismillah Hir Rahman Nir Raheem
import PackageDescription

let package = Package(
    name: "AlbBizMap",
    platforms: [
        .iOS(.v16)
    ],
    products: [
        .library(
            name: "AlbBizMap",
            targets: ["AlbBizMap"]
        )
    ],
    targets: [
        .target(
            name: "AlbBizMap",
            path: "AlbBizMap"
        )
    ]
)

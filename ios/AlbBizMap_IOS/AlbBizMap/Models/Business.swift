// Bismillah Hir Rahman Nir Raheem
import Foundation
import CoreLocation

public struct Promotion: Identifiable, Codable, Hashable {
    public var id: String { title + (discountCode ?? "") }
    public var title: String
    public var description: String
    public var discountCode: String?
    public var expiryDate: Int64?
    
    public init(title: String = "", description: String = "", discountCode: String? = nil, expiryDate: Int64? = nil) {
        self.title = title
        self.description = description
        self.discountCode = discountCode
        self.expiryDate = expiryDate
    }
    
    public func toMap() -> [String: Any?] {
        return [
            "title": title,
            "description": description,
            "discountCode": discountCode,
            "expiryDate": expiryDate
        ]
    }
    
    public static func fromMap(_ map: [String: Any?]) -> Promotion {
        return Promotion(
            title: map["title"] as? String ?? "",
            description: map["description"] as? String ?? "",
            discountCode: map["discountCode"] as? String,
            expiryDate: (map["expiryDate"] as? NSNumber)?.int64Value
        )
    }
}

public struct JobPosting: Identifiable, Codable, Hashable {
    public var id: String { title + "\(postedAt)" }
    public var title: String
    public var description: String
    public var type: String // Full-time, Part-time, Contract
    public var salary: String?
    public var postedAt: Int64
    
    public init(title: String = "", description: String = "", type: String = "Full-time", salary: String? = nil, postedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)) {
        self.title = title
        self.description = description
        self.type = type
        self.salary = salary
        self.postedAt = postedAt
    }
    
    public func toMap() -> [String: Any?] {
        return [
            "title": title,
            "description": description,
            "type": type,
            "salary": salary,
            "postedAt": postedAt
        ]
    }
    
    public static func fromMap(_ map: [String: Any?]) -> JobPosting {
        return JobPosting(
            title: map["title"] as? String ?? "",
            description: map["description"] as? String ?? "",
            type: map["type"] as? String ?? "Full-time",
            salary: map["salary"] as? String,
            postedAt: (map["postedAt"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000)
        )
    }
}

public struct GeoPointLocation: Codable, Hashable {
    public var latitude: Double
    public var longitude: Double
    
    public init(latitude: Double, longitude: Double) {
        self.latitude = latitude
        self.longitude = longitude
    }
    
    public var coordinate: CLLocationCoordinate2D {
        CLLocationCoordinate2D(latitude: latitude, longitude: longitude)
    }
}

public struct Business: Identifiable, Codable, Hashable {
    public var id: String
    public var name: String
    public var category: String
    public var description: String
    public var longDescription: String
    public var address: String
    public var city: String
    public var country: String
    public var phone: String
    public var email: String
    public var website: String
    public var isOpen24Hours: Bool
    public var workingHours: [String: String]
    public var location: GeoPointLocation?
    public var photos: [String]
    public var rating: Double
    public var reviewCount: Int
    public var isActive: Bool
    public var isSponsored: Bool
    public var sponsoredUntil: Int64?
    public var isPremium: Bool
    public var premiumUntil: Int64?
    public var ownerId: String
    public var ownerEmail: String
    public var ownerName: String
    public var isVerified: Bool
    public var isAlbanianOwned: Bool
    public var isFeatured: Bool
    public var promotions: [Promotion]
    public var jobs: [JobPosting]
    public var likeCount: Int
    public var likedBy: [String]
    
    public init(
        id: String = "",
        name: String = "",
        category: String = "",
        description: String = "",
        longDescription: String = "",
        address: String = "",
        city: String = "",
        country: String = "",
        phone: String = "",
        email: String = "",
        website: String = "",
        isOpen24Hours: Bool = false,
        workingHours: [String: String] = [:],
        location: GeoPointLocation? = nil,
        photos: [String] = [],
        rating: Double = 0.0,
        reviewCount: Int = 0,
        isActive: Bool = true,
        isSponsored: Bool = false,
        sponsoredUntil: Int64? = nil,
        isPremium: Bool = false,
        premiumUntil: Int64? = nil,
        ownerId: String = "",
        ownerEmail: String = "",
        ownerName: String = "",
        isVerified: Bool = false,
        isAlbanianOwned: Bool = false,
        isFeatured: Bool = false,
        promotions: [Promotion] = [],
        jobs: [JobPosting] = [],
        likeCount: Int = 0,
        likedBy: [String] = []
    ) {
        self.id = id
        self.name = name
        self.category = category
        self.description = description
        self.longDescription = longDescription
        self.address = address
        self.city = city
        self.country = country
        self.phone = phone
        self.email = email
        self.website = website
        self.isOpen24Hours = isOpen24Hours
        self.workingHours = workingHours
        self.location = location
        self.photos = photos
        self.rating = rating
        self.reviewCount = reviewCount
        self.isActive = isActive
        self.isSponsored = isSponsored
        self.sponsoredUntil = sponsoredUntil
        self.isPremium = isPremium
        self.premiumUntil = premiumUntil
        self.ownerId = ownerId
        self.ownerEmail = ownerEmail
        self.ownerName = ownerName
        self.isVerified = isVerified
        self.isAlbanianOwned = isAlbanianOwned
        self.isFeatured = isFeatured
        self.promotions = promotions
        self.jobs = jobs
        self.likeCount = likeCount
        self.likedBy = likedBy
    }
    
    // True while the flag is set AND its matching expiry timestamp hasn't
    // passed yet (a nil timestamp means "no expiry recorded" — treated as
    // still active rather than expired, matching Android). Featured has no
    // timestamp of its own — updateSubscription() writes premiumUntil for
    // both the "premium" and "featured" purchase tiers, so isEffectivelyFeatured
    // checks premiumUntil too.
    public var isEffectivelyPremium: Bool {
        isPremium && (premiumUntil == nil || premiumUntil! > Int64(Date().timeIntervalSince1970 * 1000))
    }
    public var isEffectivelyFeatured: Bool {
        isFeatured && (premiumUntil == nil || premiumUntil! > Int64(Date().timeIntervalSince1970 * 1000))
    }
    public var isEffectivelySponsored: Bool {
        isSponsored && (sponsoredUntil == nil || sponsoredUntil! > Int64(Date().timeIntervalSince1970 * 1000))
    }

    public var maxPhotos: Int {
        if isEffectivelySponsored { return 14 }
        if isEffectivelyFeatured { return 10 }
        if isEffectivelyPremium { return 6 }
        return 1
    }
    
    public func toMap() -> [String: Any?] {
        var map: [String: Any?] = [
            "id": id,
            "name": name,
            "category": category,
            "description": description,
            "longDescription": longDescription,
            "address": address,
            "city": city,
            "country": country,
            "phone": phone,
            "email": email,
            "website": website,
            "isOpen24Hours": isOpen24Hours,
            "workingHours": workingHours,
            "photos": photos,
            "rating": rating,
            "reviewCount": reviewCount,
            "isActive": isActive,
            "isSponsored": isSponsored,
            "sponsoredUntil": sponsoredUntil,
            "isPremium": isPremium,
            "premiumUntil": premiumUntil,
            "ownerId": ownerId,
            "ownerEmail": ownerEmail,
            "ownerName": ownerName,
            "isVerified": isVerified,
            "isAlbanianOwned": isAlbanianOwned,
            "isFeatured": isFeatured,
            "promotions": promotions.map { $0.toMap() },
            "jobs": jobs.map { $0.toMap() },
            "likeCount": likeCount,
            "likedBy": likedBy
        ]
        
        if let location = location {
            map["latitude"] = location.latitude
            map["longitude"] = location.longitude
        }
        
        return map
    }
    
    public static func fromMap(id: String, map: [String: Any?]) -> Business {
        var loc: GeoPointLocation? = nil
        if let lat = (map["latitude"] as? NSNumber)?.doubleValue,
           let lng = (map["longitude"] as? NSNumber)?.doubleValue {
            loc = GeoPointLocation(latitude: lat, longitude: lng)
        }
        
        var workingHoursDict: [String: String] = [:]
        if let wh = map["workingHours"] as? [String: Any] {
            for (k, v) in wh {
                workingHoursDict[k] = "\(v)"
            }
        }
        
        let promoList = (map["promotions"] as? [[String: Any?]])?.map { Promotion.fromMap($0) } ?? []
        let jobList = (map["jobs"] as? [[String: Any?]])?.map { JobPosting.fromMap($0) } ?? []
        
        return Business(
            id: id,
            name: map["name"] as? String ?? "",
            category: map["category"] as? String ?? "",
            description: map["description"] as? String ?? "",
            longDescription: map["longDescription"] as? String ?? "",
            address: map["address"] as? String ?? "",
            city: map["city"] as? String ?? "",
            country: map["country"] as? String ?? "",
            phone: map["phone"] as? String ?? "",
            email: map["email"] as? String ?? "",
            website: map["website"] as? String ?? "",
            isOpen24Hours: map["isOpen24Hours"] as? Bool ?? false,
            workingHours: workingHoursDict,
            location: loc,
            photos: (map["photos"] as? [String]) ?? [],
            rating: (map["rating"] as? NSNumber)?.doubleValue ?? 0.0,
            reviewCount: (map["reviewCount"] as? NSNumber)?.intValue ?? 0,
            isActive: map["isActive"] as? Bool ?? true,
            isSponsored: map["isSponsored"] as? Bool ?? false,
            sponsoredUntil: (map["sponsoredUntil"] as? NSNumber)?.int64Value,
            isPremium: map["isPremium"] as? Bool ?? false,
            premiumUntil: (map["premiumUntil"] as? NSNumber)?.int64Value,
            ownerId: map["ownerId"] as? String ?? "",
            ownerEmail: map["ownerEmail"] as? String ?? "",
            ownerName: map["ownerName"] as? String ?? "",
            isVerified: map["isVerified"] as? Bool ?? false,
            isAlbanianOwned: map["isAlbanianOwned"] as? Bool ?? false,
            isFeatured: map["isFeatured"] as? Bool ?? false,
            promotions: promoList,
            jobs: jobList,
            likeCount: (map["likeCount"] as? NSNumber)?.intValue ?? 0,
            likedBy: (map["likedBy"] as? [String]) ?? []
        )
    }
}

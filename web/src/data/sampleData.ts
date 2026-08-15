// Bismillah Hir Rahman Nir Raheem

export interface Business {
  id: string;
  name: string;
  category: string;
  description: string;
  longDescription?: string;
  address: string;
  city: string;
  country: string;
  phone: string;
  email: string;
  website: string;
  lat: number;
  lng: number;
  photos: string[];
  rating: number;
  reviewCount: number;
  isActive: boolean;
  isSponsored: boolean;
  isPremium: boolean;
  isFeatured: boolean;
  isVerified: boolean;
  isAlbanianOwned: boolean;
  likeCount: number;
  likedBy: string[];
  ownerId?: string;
}

export interface Story {
  id: string;
  userName: string;
  userAvatar?: string;
  businessName?: string;
  businessId?: string;
  location: string;
  photoUrl: string;
  text: string;
  createdAt: number;
  isSponsored?: boolean;
}

export interface EventItem {
  id: string;
  title: string;
  description: string;
  locationName: string;
  date: string;
  category: string;
  imageUrl: string;
  isPromoted: boolean;
  websiteUrl?: string;
}

export interface JobItem {
  id: string;
  title: string;
  businessName: string;
  businessId: string;
  type: string;
  location: string;
  description: string;
  salary?: string;
}

export const SAMPLE_BUSINESSES: Business[] = [
  {
    id: "biz_padova_1",
    name: "Bar & Caffe Iliria Padova",
    category: "Cafe",
    description: "Authentic Albanian macchiato, cornetti, and traditional espresso in central Padova.",
    longDescription: "Bar Caffe Iliria brings warm Albanian hospitality right into the heart of Padova. Enjoy our famous espresso, artisan pastries, and refreshing summer drinks in a cozy terrace setting.",
    address: "Prato della Valle 12",
    city: "Padova",
    country: "Italy",
    phone: "+39 049 876 5432",
    email: "iliria.padova@metont.com",
    website: "https://metont.com/biz/bar-iliria-padova",
    lat: 45.4012,
    lng: 11.8754,
    photos: ["https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb"],
    rating: 4.9,
    reviewCount: 34,
    isActive: true,
    isSponsored: true,
    isPremium: true,
    isFeatured: true,
    isVerified: true,
    isAlbanianOwned: true,
    likeCount: 52,
    likedBy: []
  },
  {
    id: "biz_padova_2",
    name: "Ristorante Skanderbeg Padova",
    category: "Restaurant",
    description: "Traditional grilled meat (zgara), tavë kosi, fergesë, and fresh Mediterranean seafood.",
    longDescription: "Ristorante Skanderbeg offers an unparalleled dining experience in Padova. Specializing in Balkan and Albanian traditional recipes, fresh meat cuts, and finest local & Albanian wines.",
    address: "Via Venezia 45",
    city: "Padova",
    country: "Italy",
    phone: "+39 049 987 6543",
    email: "skanderbeg.padova@metont.com",
    website: "https://metont.com/biz/skanderbeg-padova",
    lat: 45.4115,
    lng: 11.8920,
    photos: ["https://images.unsplash.com/photo-1555396273-367ea4eb4db5"],
    rating: 4.8,
    reviewCount: 42,
    isActive: true,
    isSponsored: true,
    isPremium: true,
    isFeatured: true,
    isVerified: true,
    isAlbanianOwned: true,
    likeCount: 68,
    likedBy: []
  },
  {
    id: "biz_padova_3",
    name: "EdilBesa Costruzioni Padova",
    category: "Contractor",
    description: "Professional home remodeling, painting, plastering, and masonry services across Veneto.",
    address: "Via Vicenza 18",
    city: "Padova",
    country: "Italy",
    phone: "+39 340 123 4567",
    email: "info@edilbesa.it",
    website: "https://edilbesa.it",
    lat: 45.4080,
    lng: 11.8650,
    photos: ["https://images.unsplash.com/photo-1541888946425-d0fbb186a5b7"],
    rating: 4.7,
    reviewCount: 15,
    isActive: true,
    isSponsored: false,
    isPremium: true,
    isFeatured: false,
    isVerified: true,
    isAlbanianOwned: true,
    likeCount: 24,
    likedBy: []
  },
  {
    id: "biz_1",
    name: "Sofra Shqiptare NYC",
    category: "Restaurant",
    description: "Traditional Albanian cuisine, tavë kosi, and grilled meats in warm family setting.",
    longDescription: "Welcome to Sofra Shqiptare! We bring authentic Albanian traditional taste to your table with freshly prepared flia, tavë kosi, fergesë, and fresh salads.",
    address: "123 Main St",
    city: "New York",
    country: "USA",
    phone: "+1 212-555-0199",
    email: "info@sofrashqiptare.com",
    website: "https://sofrashqiptare.com",
    lat: 40.7128,
    lng: -74.0060,
    photos: ["https://images.unsplash.com/photo-1555396273-367ea4eb4db5"],
    rating: 4.9,
    reviewCount: 28,
    isActive: true,
    isSponsored: true,
    isPremium: true,
    isFeatured: true,
    isVerified: true,
    isAlbanianOwned: true,
    likeCount: 45,
    likedBy: []
  },
  {
    id: "biz_2",
    name: "Peja Espresso Bar Bronx",
    category: "Cafe",
    description: "Authentic macchiato, Turkish coffee, and freshly baked pastries.",
    address: "456 Grand Ave",
    city: "Bronx",
    country: "USA",
    phone: "+1 718-555-0144",
    email: "peja@bronxcafe.com",
    website: "https://pejabronx.com",
    lat: 40.8448,
    lng: -73.8648,
    photos: ["https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb"],
    rating: 4.8,
    reviewCount: 19,
    isActive: true,
    isSponsored: false,
    isPremium: true,
    isFeatured: true,
    isVerified: true,
    isAlbanianOwned: true,
    likeCount: 32,
    likedBy: []
  },
  {
    id: "biz_3",
    name: "Besa Construction Stamford",
    category: "Contractor",
    description: "General contractor specializing in modern home renovation, roofing, and tile work.",
    address: "789 Broadway",
    city: "Stamford",
    country: "USA",
    phone: "+1 203-555-0177",
    email: "contact@besaconstruction.com",
    website: "https://besaconstruction.com",
    lat: 41.0534,
    lng: -73.5387,
    photos: ["https://images.unsplash.com/photo-1541888946425-d0fbb186a5b7"],
    rating: 4.7,
    reviewCount: 12,
    isActive: true,
    isSponsored: false,
    isPremium: false,
    isFeatured: false,
    isVerified: true,
    isAlbanianOwned: true,
    likeCount: 15,
    likedBy: []
  },
  {
    id: "biz_tirana_1",
    name: "Mulliri i Vjetër Tirana",
    category: "Cafe",
    description: "Iconic specialty coffee shop serving premium espresso and desserts in Blloku.",
    address: "Rruga Ibrahim Rugova, Blloku",
    city: "Tirana",
    country: "Albania",
    phone: "+355 4 223 4567",
    email: "info@mulliri.al",
    website: "https://mulliri.al",
    lat: 41.3217,
    lng: 19.8188,
    photos: ["https://images.unsplash.com/photo-1495474472287-4d71bcdd2085"],
    rating: 4.9,
    reviewCount: 88,
    isActive: true,
    isSponsored: true,
    isPremium: true,
    isFeatured: true,
    isVerified: true,
    isAlbanianOwned: true,
    likeCount: 140,
    likedBy: []
  }
];

export const SAMPLE_STORIES: Story[] = [
  {
    id: "s1",
    userName: "Bar & Caffe Iliria",
    businessName: "Bar & Caffe Iliria Padova",
    businessId: "biz_padova_1",
    location: "Padova, Italy",
    photoUrl: "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb",
    text: "Fresh macchiato and warm cornetti ready for you in Prato della Valle!",
    createdAt: Date.now() - 3600000,
    isSponsored: true
  },
  {
    id: "s2",
    userName: "Ristorante Skanderbeg",
    businessName: "Ristorante Skanderbeg Padova",
    businessId: "biz_padova_2",
    location: "Padova, Italy",
    photoUrl: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5",
    text: "Tavë Kosi hot out of the oven tonight! Reserve your table.",
    createdAt: Date.now() - 7200000,
    isSponsored: true
  },
  {
    id: "s3",
    userName: "Sofra Shqiptare",
    businessName: "Sofra Shqiptare NYC",
    businessId: "biz_1",
    location: "New York, USA",
    photoUrl: "https://images.unsplash.com/photo-1555396273-367ea4eb4db5",
    text: "Authentic flia prepared fresh for weekend guests!",
    createdAt: Date.now() - 10800000
  }
];

export const SAMPLE_EVENTS: EventItem[] = [
  {
    id: "e1",
    title: "Albanian Cultural Night & Concert Padova",
    description: "Traditional folk dancing, live music performance, and Albanian wine tasting.",
    locationName: "Gran Teatro Geox, Padova, Italy",
    date: "August 28, 2026",
    category: "Cultural",
    imageUrl: "https://images.unsplash.com/photo-1511578314322-379afb476865",
    isPromoted: true,
    websiteUrl: "https://metont.com/events/padova-cultural-night"
  },
  {
    id: "e2",
    title: "Albanian Flag Day Parade & Gala NYC",
    description: "Annual parade celebrating independence day with traditional outfits & food stalls.",
    locationName: "Manhattan Center, New York",
    date: "November 28, 2026",
    category: "Festival",
    imageUrl: "https://images.unsplash.com/photo-1492684223066-81342ee5ff30",
    isPromoted: true,
    websiteUrl: "https://albanianfestival.org"
  }
];

export const SAMPLE_JOBS: JobItem[] = [
  {
    id: "j1",
    title: "Head Barista / Manager",
    businessName: "Bar & Caffe Iliria Padova",
    businessId: "biz_padova_1",
    type: "Full-time",
    location: "Padova, Italy",
    description: "Seeking experienced barista fluent in Italian & Albanian to manage daily cafe operations.",
    salary: "€1,600 - €2,000 / mo"
  },
  {
    id: "j2",
    title: "Executive Chef (Albanian Cuisine)",
    businessName: "Sofra Shqiptare NYC",
    businessId: "biz_1",
    type: "Full-time",
    location: "New York, USA",
    description: "Looking for skilled chef specializing in traditional Balkan and Albanian dishes.",
    salary: "$55,000 - $70,000 / yr"
  }
];

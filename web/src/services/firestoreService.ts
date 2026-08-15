// Bismillah Hir Rahman Nir Raheem
import { 
  collection, 
  onSnapshot, 
  addDoc, 
  doc, 
  updateDoc, 
  arrayUnion, 
  arrayRemove, 
  increment
} from 'firebase/firestore';
import { db } from '../config/firebase';
import type { Business, Story } from '../data/sampleData';
import { SAMPLE_BUSINESSES, SAMPLE_STORIES } from '../data/sampleData';

export class FirestoreService {
  private static instance: FirestoreService;
  
  public static getInstance(): FirestoreService {
    if (!FirestoreService.instance) {
      FirestoreService.instance = new FirestoreService();
    }
    return FirestoreService.instance;
  }

  // Subscribe to real-time Firestore businesses across Android, iOS, and Web
  public subscribeBusinesses(onUpdate: (businesses: Business[]) => void) {
    try {
      const bizRef = collection(db, 'businesses');
      return onSnapshot(bizRef, (snapshot: any) => {
        if (snapshot && !snapshot.empty) {
          const fetchedBusinesses: Business[] = snapshot.docs.map((docSnap: any) => {
            const data = docSnap.data();
            
            let lat = 41.3275; // Default Tirana
            let lng = 19.8187;
            if (data.location && typeof data.location.latitude === 'number') {
              lat = data.location.latitude;
              lng = data.location.longitude;
            } else if (typeof data.lat === 'number' && typeof data.lng === 'number') {
              lat = data.lat;
              lng = data.lng;
            } else if (typeof data.latitude === 'number' && typeof data.longitude === 'number') {
              lat = data.latitude;
              lng = data.longitude;
            }

            return {
              id: docSnap.id,
              name: data.name || '',
              category: data.category || 'Restaurant',
              description: data.description || '',
              longDescription: data.longDescription || '',
              address: data.address || '',
              city: data.city || 'Tirana',
              country: data.country || 'Albania',
              phone: data.phone || '',
              email: data.email || '',
              website: data.website || '',
              lat,
              lng,
              photos: Array.isArray(data.photos) && data.photos.length > 0 ? data.photos : ['https://images.unsplash.com/photo-1555396273-367ea4eb4db5'],
              rating: typeof data.rating === 'number' ? data.rating : 5.0,
              reviewCount: typeof data.reviewCount === 'number' ? data.reviewCount : 0,
              isActive: data.isActive !== false,
              isSponsored: data.isSponsored === true,
              isPremium: data.isPremium === true,
              isFeatured: data.isFeatured === true,
              isVerified: data.isVerified === true,
              isAlbanianOwned: data.isAlbanianOwned !== false,
              likeCount: typeof data.likeCount === 'number' ? data.likeCount : 0,
              likedBy: Array.isArray(data.likedBy) ? data.likedBy : [],
              ownerId: data.ownerId || ''
            };
          });

          const combinedMap = new Map<string, Business>();
          SAMPLE_BUSINESSES.forEach(b => combinedMap.set(b.id, b));
          fetchedBusinesses.forEach(b => combinedMap.set(b.id, b));
          
          onUpdate(Array.from(combinedMap.values()));
        } else {
          onUpdate(SAMPLE_BUSINESSES);
        }
      }, (error: any) => {
        console.warn("Firestore snapshot error (using local cache):", error);
        onUpdate(SAMPLE_BUSINESSES);
      });
    } catch (e) {
      console.warn("Firestore init error:", e);
      onUpdate(SAMPLE_BUSINESSES);
      return () => {};
    }
  }

  // Subscribe to real-time stories
  public subscribeStories(onUpdate: (stories: Story[]) => void) {
    try {
      const storiesRef = collection(db, 'stories');
      return onSnapshot(storiesRef, (snapshot: any) => {
        if (snapshot && !snapshot.empty) {
          const stories: Story[] = snapshot.docs.map((docSnap: any) => {
            const d = docSnap.data();
            return {
              id: docSnap.id,
              userName: d.userName || 'User',
              userAvatar: d.userAvatar || '',
              businessName: d.businessName || '',
              businessId: d.businessId || '',
              location: d.location || 'Tirana',
              photoUrl: Array.isArray(d.photos) && d.photos.length > 0 ? d.photos[0] : (d.photoUrl || 'https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb'),
              text: d.text || '',
              createdAt: d.createdAt || Date.now(),
              isSponsored: d.isSponsored === true
            };
          });
          onUpdate(stories);
        } else {
          onUpdate(SAMPLE_STORIES);
        }
      }, () => onUpdate(SAMPLE_STORIES));
    } catch (e) {
      onUpdate(SAMPLE_STORIES);
      return () => {};
    }
  }

  // Add business to Firestore
  public async addBusiness(biz: Partial<Business>): Promise<string> {
    try {
      const bizRef = collection(db, 'businesses');
      const docRef = await addDoc(bizRef, {
        name: biz.name,
        category: biz.category,
        description: biz.description,
        address: biz.address,
        city: biz.city,
        country: biz.country,
        phone: biz.phone,
        email: biz.email,
        website: biz.website,
        lat: biz.lat,
        lng: biz.lng,
        photos: biz.photos || [],
        rating: 5.0,
        reviewCount: 1,
        isActive: true,
        isSponsored: false,
        isPremium: false,
        isFeatured: false,
        isVerified: false,
        isAlbanianOwned: biz.isAlbanianOwned ?? true,
        likeCount: 0,
        likedBy: [],
        createdAt: Date.now()
      });
      return docRef.id;
    } catch (e) {
      console.warn("Error adding business to Firestore:", e);
      return `biz_${Date.now()}`;
    }
  }

  // Toggle business like
  public async toggleLike(businessId: string, userId: string, isLiked: boolean) {
    try {
      const bizRef = doc(db, 'businesses', businessId);
      if (isLiked) {
        await updateDoc(bizRef, {
          likedBy: arrayRemove(userId),
          likeCount: increment(-1)
        });
      } else {
        await updateDoc(bizRef, {
          likedBy: arrayUnion(userId),
          likeCount: increment(1)
        });
      }
    } catch (e) {
      console.warn("Like toggle error:", e);
    }
  }
}

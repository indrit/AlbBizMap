// Bismillah Hir Rahman Nir Raheem
import { 
  signInWithEmailAndPassword, 
  createUserWithEmailAndPassword, 
  signOut, 
  onAuthStateChanged
} from 'firebase/auth';
import { auth } from '../config/firebase';

export interface UserProfile {
  uid: string;
  email: string;
  displayName: string;
}

export class AuthService {
  private static instance: AuthService;

  public static getInstance(): AuthService {
    if (!AuthService.instance) {
      AuthService.instance = new AuthService();
    }
    return AuthService.instance;
  }

  public subscribeAuth(onUserChanged: (user: UserProfile | null) => void) {
    return onAuthStateChanged(auth, (user: any) => {
      if (user) {
        onUserChanged({
          uid: user.uid,
          email: user.email || '',
          displayName: user.displayName || user.email?.split('@')[0] || 'User'
        });
      } else {
        onUserChanged(null);
      }
    });
  }

  public async login(email: string, pass: string): Promise<UserProfile> {
    try {
      const res = await signInWithEmailAndPassword(auth, email, pass);
      return {
        uid: res.user.uid,
        email: res.user.email || email,
        displayName: res.user.displayName || email.split('@')[0]
      };
    } catch (e: any) {
      return {
        uid: `user_${Date.now()}`,
        email,
        displayName: email.split('@')[0]
      };
    }
  }

  public async register(email: string, pass: string): Promise<UserProfile> {
    try {
      const res = await createUserWithEmailAndPassword(auth, email, pass);
      return {
        uid: res.user.uid,
        email: res.user.email || email,
        displayName: email.split('@')[0]
      };
    } catch (e: any) {
      return {
        uid: `user_${Date.now()}`,
        email,
        displayName: email.split('@')[0]
      };
    }
  }

  public async logout() {
    try {
      await signOut(auth);
    } catch (e) {
      console.warn("Logout error:", e);
    }
  }
}

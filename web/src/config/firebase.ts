// Bismillah Hir Rahman Nir Raheem
import { initializeApp, getApps, getApp } from 'firebase/app';
import { getAuth } from 'firebase/auth';
import { getFirestore } from 'firebase/firestore';
import { getStorage } from 'firebase/storage';

const firebaseConfig = {
  apiKey: "AIzaSyCmFeKElkqLvnwmvoCwTwe7YqHz_Z-iMRc",
  authDomain: "albbizmap.firebaseapp.com",
  projectId: "albbizmap",
  storageBucket: "albbizmap.firebasestorage.app",
  messagingSenderId: "626415932806",
  appId: "1:626415932806:web:698ee9e7c0214c6e698807",
  measurementId: "G-MH5KDMS6HS"
};

const app = !getApps().length ? initializeApp(firebaseConfig) : getApp();
export const auth = getAuth(app);
export const db = getFirestore(app);
export const storage = getStorage(app);

export default app;

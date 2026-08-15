// Bismillah Hir Rahman Nir Raheem

export interface GeocodeResult {
  lat: number;
  lng: number;
  displayName: string;
}

export async function geocodePlace(query: string): Promise<GeocodeResult | null> {
  const trimmed = query.trim();
  if (!trimmed) return null;

  // Pre-defined quick coordinates for common cities to ensure fast zero-latency response
  const lower = trimmed.toLowerCase();
  if (lower.includes("padova") || lower.includes("padua")) {
    return { lat: 45.4064, lng: 11.8768, displayName: "Padova, Veneto, Italy" };
  }
  if (lower.includes("tirana") || lower.includes("tiranë")) {
    return { lat: 41.3275, lng: 19.8187, displayName: "Tirana, Albania" };
  }
  if (lower.includes("prishtina") || lower.includes("pristina")) {
    return { lat: 42.6629, lng: 21.1655, displayName: "Prishtina, Kosovo" };
  }
  if (lower.includes("new york") || lower.includes("nyc")) {
    return { lat: 40.7128, lng: -74.0060, displayName: "New York, NY, USA" };
  }
  if (lower.includes("bronx")) {
    return { lat: 40.8448, lng: -73.8648, displayName: "Bronx, NY, USA" };
  }
  if (lower.includes("stamford")) {
    return { lat: 41.0534, lng: -73.5387, displayName: "Stamford, CT, USA" };
  }
  if (lower.includes("durres") || lower.includes("durrës")) {
    return { lat: 41.3246, lng: 19.4565, displayName: "Durrës, Albania" };
  }

  // Fallback: OpenStreetMap Nominatim Geocoding API
  try {
    const res = await fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(trimmed)}&limit=1`);
    if (res.ok) {
      const data = await res.json();
      if (data && data.length > 0) {
        return {
          lat: parseFloat(data[0].lat),
          lng: parseFloat(data[0].lon),
          displayName: data[0].display_name
        };
      }
    }
  } catch (e) {
    console.warn("Geocoding fetch error:", e);
  }

  return null;
}

export function getUserLocation(): Promise<{ lat: number; lng: number }> {
  return new Promise((resolve, reject) => {
    if (!navigator.geolocation) {
      reject(new Error("Geolocation is not supported by your browser"));
      return;
    }
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        resolve({
          lat: pos.coords.latitude,
          lng: pos.coords.longitude
        });
      },
      (err) => reject(err),
      { timeout: 10000 }
    );
  });
}

// Frontend aur backend EK hi server pe hain (Spring Boot static folder) to API_BASE khaali rehne do.
//
// Agar frontend alag deploy karte ho (Netlify / Vercel / GitHub Pages), to yahan backend ka poora URL do:
//   API_BASE: "https://tumhara-backend.onrender.com"
// (aakhir me slash '/' mat lagao)
window.APP_CONFIG = {
  API_BASE: ""
};

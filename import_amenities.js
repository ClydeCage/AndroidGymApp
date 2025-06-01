const admin = require('firebase-admin');
const amenities = require('./amenities_master.json');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function importAmenities() {
  for (const name of amenities) {
    await db.collection('amenities').add({ name });
    console.log('Imported:', name);
  }
  console.log('Done!');
}

importAmenities(); 
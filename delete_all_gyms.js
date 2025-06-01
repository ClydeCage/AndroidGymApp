const admin = require('firebase-admin');

// Thay đường dẫn này bằng file serviceAccountKey.json bạn tải từ Firebase Console
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function deleteGyms() {
  const collectionRef = db.collection('gyms');
  const snapshot = await collectionRef.get();

  if (snapshot.empty) {
    console.log('No gyms found to delete.');
    return;
  }

  const batch = db.batch();
  snapshot.docs.forEach(doc => {
    batch.delete(doc.ref);
  });

  await batch.commit();
  console.log(`Deleted ${snapshot.size} gyms.`);
}

deleteGyms(); 
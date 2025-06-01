const admin = require('firebase-admin');
const trainers = require('./trainers_sample.json');

// Thay đường dẫn này bằng file serviceAccountKey.json bạn tải từ Firebase Console
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function importTrainers() {
  for (const trainer of trainers) {
    await db.collection('trainers').add(trainer);
    console.log('Imported:', trainer.name);
  }
  console.log('Done!');
}

importTrainers(); 
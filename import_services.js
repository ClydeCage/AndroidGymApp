const admin = require('firebase-admin');
const services = require('./services_master.json');
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function importServices() {
  for (const name of services) {
    await db.collection('services').add({ name });
    console.log('Imported:', name);
  }
  console.log('Done!');
}

importServices(); 
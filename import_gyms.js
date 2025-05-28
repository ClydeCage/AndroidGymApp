const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json'); // Thay bằng tên file JSON của bạn
const data = require('./gyms_hadong.json'); // Thay bằng tên file JSON dữ liệu của bạn

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function importData() {
  console.log('Bắt đầu nhập dữ liệu phòng gym...');

  for (const gym of data) {
    try {
      // Thêm document vào collection 'gyms'
      // Firestore sẽ tự tạo ID document
      const docRef = await db.collection('gyms').add(gym);
      console.log(`Đã thêm phòng gym: ${gym.name} với ID: ${docRef.id}`);
    } catch (error) {
      console.error(`Lỗi khi thêm phòng gym ${gym.name}:`, error);
    }
  }

  console.log('Kết thúc nhập dữ liệu.');
  // Thoát script sau khi hoàn thành
  process.exit(0);
}

importData(); 
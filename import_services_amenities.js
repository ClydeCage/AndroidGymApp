const admin = require('firebase-admin');
const serviceAccount = require('./serviceAccountKey.json'); // Thay bằng tên file JSON của bạn

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

const initialServices = [
    { name: 'Yoga' },
    { name: 'Zumba' },
    { name: 'PT (Huấn luyện viên cá nhân)' },
    { name: 'Group Class' },
    { name: 'Cardio Area' },
    { name: 'Weight Training Area' }
];

const initialAmenities = [
    { name: 'Wifi' },
    { name: 'Parking' },
    { name: 'Locker Room' },
    { name: 'Shower' },
    { name: 'Sauna' },
    { name: 'Steam Room' },
    { name: 'Swimming Pool' },
    { name: 'Towel Service' },
    { name: 'Filtered Water' }
];

async function importData() {
    console.log('Bắt đầu import dữ liệu Dịch vụ và Tiện ích...');

    // Import Services
    const servicesCollectionRef = db.collection('services');
    for (const service of initialServices) {
        try {
            // Sử dụng tên làm ID document hoặc thêm document ngẫu nhiên
            await servicesCollectionRef.add(service);
            console.log(`Đã thêm dịch vụ: ${service.name}`);
        } catch (error) {
            console.error(`Lỗi khi thêm dịch vụ ${service.name}:`, error);
        }
    }

    // Import Amenities
    const amenitiesCollectionRef = db.collection('amenities');
    for (const amenity of initialAmenities) {
        try {
             // Sử dụng tên làm ID document hoặc thêm document ngẫu nhiên
            await amenitiesCollectionRef.add(amenity);
            console.log(`Đã thêm tiện ích: ${amenity.name}`);
        } catch (error) {
            console.error(`Lỗi khi thêm tiện ích ${amenity.name}:`, error);
        }
    }

    console.log('Hoàn thành import dữ liệu.');
}

importData(); 
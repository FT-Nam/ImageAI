from flask import Flask, request, jsonify
from flask_cors import CORS
import torch
import torch.nn as nn
from torchvision import transforms
from PIL import Image

# Tạo Flask app
app = Flask(__name__)
CORS(app)

# Định nghĩa lại mô hình đã huấn luyện
class CatDogClassifier(nn.Module):
    def __init__(self):
        super(CatDogClassifier, self).__init__()
        self.conv1 = nn.Conv2d(3, 32, kernel_size=3, padding=1)
        self.bn1 = nn.BatchNorm2d(32)
        self.conv2 = nn.Conv2d(32, 64, kernel_size=3, padding=1)
        self.bn2 = nn.BatchNorm2d(64)
        self.conv3 = nn.Conv2d(64, 128, kernel_size=3, padding=1)
        self.bn3 = nn.BatchNorm2d(128)
        self.pool = nn.MaxPool2d(2, 2)
        self.fc1 = nn.Linear(128 * 18 * 18, 256)
        self.fc2 = nn.Linear(256, 1)
        self.dropout = nn.Dropout(0.5)

    def forward(self, x):
        x = self.pool(torch.relu(self.bn1(self.conv1(x))))
        x = self.pool(torch.relu(self.bn2(self.conv2(x))))
        x = self.pool(torch.relu(self.bn3(self.conv3(x))))
        x = x.view(-1, 128 * 18 * 18)
        x = torch.relu(self.fc1(x))
        x = self.dropout(x)
        x = self.fc2(x)
        return x


# Load mô hình đã train
model_path = "improved_dog_cat_model.pth"
model = CatDogClassifier()
model.load_state_dict(torch.load(model_path, map_location=torch.device("cpu")))
model.eval()

# Transform ảnh
transform = transforms.Compose([
    transforms.Resize((150, 150)),
    transforms.ToTensor(),
    transforms.Normalize((0.5, 0.5, 0.5), (0.5, 0.5, 0.5))
])

@app.route('/predict', methods=['POST'])
def predict():
    if 'file' not in request.files:
        return jsonify({'error': 'No file uploaded'}), 400

    file = request.files['file']

    try:
        # Đọc và chuyển đổi ảnh
        img = Image.open(file.stream).convert("RGB")
        img = transform(img).unsqueeze(0)  # Thêm batch dimension
    except Exception as e:
        return jsonify({'error': 'Invalid image file'}), 400

    # Dự đoán
    with torch.no_grad():
        output = model(img)
        prob = torch.sigmoid(output).item()  # Xác suất đầu ra
        prediction = "Chó" if prob > 0.5 else "Mèo"
        accuracy = int(prob * 100 if prob > 0.5 else (1 - prob) * 100)
        description = (
            "Là loài vật trung thành, gắn bó với con người, thường được nuôi làm bạn hoặc trông nhà."
            if prediction == "Chó"
            else "Là loài vật độc lập, tinh nghịch và đáng yêu, được yêu thích bởi sự nhẹ nhàng."
        )

    return jsonify({
        'prediction': prediction,
        'description': description,
        'accuracy': accuracy
    })

if __name__ == '__main__':
    app.run(debug=True)

from flask import Flask, request, jsonify
from flask_cors import CORS
import tensorflow as tf
import numpy as np
from PIL import Image
from tensorflow.keras.applications.efficientnet import preprocess_input

# =====================
# App init
# =====================
app = Flask(__name__)
CORS(app)

# =====================
# Load models
# =====================
dog_cat_other_model = tf.keras.models.load_model("dog_cat_other_model.keras")
dog_breed_model = tf.keras.models.load_model("dog_breed_model.keras")
cat_breed_model = tf.keras.models.load_model("cat_breed_model.keras")

# =====================
# Thresholds
# =====================
ANIMAL_THRESHOLD = 0.6     # dog / cat / other
BREED_HIGH = 0.75
BREED_LOW = 0.6

# =====================
# Labels
# =====================
DOG_CAT_OTHER_LABELS = ["CAT", "DOG", "OTHER"]

DOG_BREEDS = [
    "Afghan","Airedale","Basenji","Basset","Beagle","Bearded Collie",
    "Bloodhound","Bluetick","Border Collie","Boston Terrier","Boxer","Bulldog","Cairn",
    "Chihuahua","Chinese Crested","Chow","Collie","Corgi","Doberman","French Bulldog","German Shepherd",
    "Golden Retriever","Great Dane","Greyhound","Japanese Spaniel","Labrador","Lhasa","Malinois","Maltese",
    "Newfoundland","Pekinese","Pomeranian","Poodle","Pug","Rottweiler","Saint Bernard","Schnauzer",
    "Scotch Terrier","Shar_Pei","Shiba Inu","Shih-Tzu","Siberian Husky","Yorkie"
]

CAT_BREEDS = [
    "Abyssinian", "American Bobtail", "American Shorthair", "Bengal",
    "Birman", "Bombay", "British Shorthair", "Egyptian Mau",
    "Maine Coon", "Persian", "Ragdoll", "Russian Blue",
    "Siamese", "Sphynx", "Tuxedo"
]

# =====================
# Preprocess
# =====================
def preprocess_image(image):
    image = image.resize((224, 224))
    img = np.array(image)
    img = preprocess_input(img)
    return np.expand_dims(img, axis=0)

# =====================
# Predict API
# =====================
@app.route("/predict", methods=["POST"])
def predict():
    if "file" not in request.files:
        return jsonify({"error": "No file uploaded"}), 400

    try:
        image = Image.open(request.files["file"]).convert("RGB")
    except:
        return jsonify({"error": "Invalid image"}), 400

    img = preprocess_image(image)

    # ==================================================
    # TẦNG 1: DOG / CAT / OTHER (softmax)
    # ==================================================
    preds = dog_cat_other_model.predict(img)[0]  # shape (3,)
    idx = int(np.argmax(preds))
    conf = float(preds[idx])

    animal = DOG_CAT_OTHER_LABELS[idx]

    # Reject OTHER hoặc low confidence
    if animal == "OTHER" or conf < ANIMAL_THRESHOLD:
        return jsonify({
            "animal": "OTHER",
            "animal_confidence": round(conf, 3),
            "message": "Not a dog or cat"
        })

    # ==================================================
    # TẦNG 2: BREED
    # ==================================================
    if animal == "DOG":
        preds = dog_breed_model.predict(img)[0]
        idx = int(np.argmax(preds))
        conf = float(preds[idx])

        if conf >= BREED_HIGH:
            breed = DOG_BREEDS[idx]
        elif conf >= BREED_LOW:
            breed = DOG_BREEDS[idx] + " (likely)"
        else:
            breed = "DOG_MIXED_BREED"

    else:  # CAT
        preds = cat_breed_model.predict(img)[0]
        idx = int(np.argmax(preds))
        conf = float(preds[idx])

        if conf >= BREED_HIGH:
            breed = CAT_BREEDS[idx]
        elif conf >= BREED_LOW:
            breed = CAT_BREEDS[idx] + " (likely)"
        else:
            breed = "CAT_MIXED_BREED"

    return jsonify({
        "animal": animal,
        "animal_confidence": round(conf, 3),
        "breed": breed,
        "breed_confidence": round(conf, 3),
        "status": "SUCCESS"
    })


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)

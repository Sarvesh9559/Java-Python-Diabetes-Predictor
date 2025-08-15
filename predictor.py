import sys
import os
import pickle
import numpy as np
import pandas as pd
import warnings
warnings.filterwarnings("ignore")
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.preprocessing import StandardScaler

MODEL_FILE = "diabetes_model.pkl"

def train_and_save_model():
    print("Model file not found. Training new model...")
    # Load dataset
    url = "https://raw.githubusercontent.com/jbrownlee/Datasets/master/pima-indians-diabetes.data.csv"
    names = ['Pregnancies','Glucose','BloodPressure','SkinThickness','Insulin','BMI',
             'DiabetesPedigreeFunction','Age','Outcome']
    data = pd.read_csv(url, names=names)

    X = data.drop('Outcome', axis=1)
    y = data['Outcome']

    # Scale features
    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    # Train model
    X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.33, random_state=42)
    model = LogisticRegression(max_iter=500)
    model.fit(X_train, y_train)

    # Save model + scaler
    with open(MODEL_FILE, "wb") as f:
        pickle.dump((model, scaler), f)

    print(f"Model trained and saved to {MODEL_FILE}")

# Ensure model exists
if not os.path.exists(MODEL_FILE):
    train_and_save_model()

# Load model and scaler
with open(MODEL_FILE, "rb") as f:
    model, scaler = pickle.load(f)

# If no arguments, show usage
if len(sys.argv) < 9:
    print("Usage: python predictor.py Pregnancies Glucose BloodPressure SkinThickness Insulin BMI DiabetesPedigreeFunction Age")
    sys.exit(1)

# Get input data from command line
input_data = np.array([list(map(float, sys.argv[1:]))])
input_scaled = scaler.transform(input_data)

# Predict
prediction = model.predict(input_scaled)
probability = model.predict_proba(input_scaled)[0][1] * 100

# Output
if prediction[0] == 1:
    print(f"Prediction: Positive ({probability:.2f}% probability)")
    print("Suggestion: Consult a doctor, maintain a healthy diet, exercise regularly, and monitor blood sugar levels.")
else:
    print(f"Prediction: Negative ({probability:.2f}% probability)")
    print("Suggestion: Maintain a healthy lifestyle to prevent diabetes in the future.")

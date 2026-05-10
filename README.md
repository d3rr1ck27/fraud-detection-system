# Fraud Detection System
A three-tier full-stack fraud detection system combining a React dashboard, Spring Boot REST API, and a Python FastAPI ML microservice trained on real credit card transaction data.
## Architecture
React Frontend (port 3000) → Spring Boot Backend (port 8080) → FastAPI ML Service (port 8000) → Random Forest Model
## Tech Stack
- Frontend: React, JavaScript, CSS
- Backend: Spring Boot, Java, Spring Data JPA, H2 (dev) / PostgreSQL (prod)
- ML Service: Python, FastAPI, scikit-learn, Random Forest, SMOTE, pandas, NumPy
- Tools: Maven, Git, Docker (coming soon)
## Model Performance
- Dataset: Kaggle Credit Card Fraud Detection (284,807 transactions, 0.17% fraud rate)
- Algorithm: Random Forest Classifier (100 estimators)
- Class imbalance handled with SMOTE oversampling
- ROC-AUC Score: 0.9731
- Fraud Precision: 84.5%
- Fraud Recall: 83.7
## Live Demo
- Frontend: https://fraud-detection-system-omega.vercel.app
- Backend API: https://fraud-detection-system-e4wl.onrender.com
- ML Service: https://fraud-detection-system-production-9199.up.railway.app

Note: The free tier backend on Render may take 50 seconds to wake up on first request after inactivity.
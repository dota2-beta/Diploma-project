import os
import numpy as np
import tensorflow as tf
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from sentence_transformers import SentenceTransformer
from tensorflow.keras import layers, Model
from contextlib import asynccontextmanager

# ==========================================
# КОНФИГУРАЦИЯ
# ==========================================

WEIGHTS_PATH = "best_model.weights.h5"

# список категорий
LABEL_COLUMNS = [
    'theme_science_research', 'theme_academic_process', 'theme_academic_contests', 
    'theme_extracurricular', 'theme_sport', 'theme_culture_art', 
    'theme_career_employment', 'theme_administration_official', 
    'theme_partnership_collaboration', 'theme_civic_patriotic', 
    'person_students', 'person_academics', 'person_staff_admin', 
    'person_applicants', 'person_alumni', 'person_general'
]

ml_models = {}

# ==========================================
# АРХИТЕКТУРА НЕЙРОСЕТИ
# ==========================================
def create_model_architecture():
    input_dim = 1024 
    num_labels = len(LABEL_COLUMNS)

    inputs = layers.Input(shape=(input_dim,), name='input_embedding')
    
    x = layers.Dense(512)(inputs)
    x = layers.BatchNormalization()(x)
    x = layers.Activation('relu')(x)
    x = layers.Dropout(0.5)(x)
    
    x = layers.Dense(256)(x)
    x = layers.BatchNormalization()(x)
    x = layers.Activation('relu')(x)
    x = layers.Dropout(0.4)(x)
    
    outputs = layers.Dense(num_labels, activation='sigmoid', name='output_layer')(x)
    
    model = Model(inputs=inputs, outputs=outputs)
    return model

# ==========================================
# ИНИЦИАЛИЗАЦИЯ
# ==========================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    print("--- ЗАГРУЗКА МОДЕЛЕЙ НАЧАТА ---")
    local_model_path = "./sbert_model"
    if os.path.exists(local_model_path):
        print(f" Загрузка SBERT из локальной папки: {local_model_path}...")
        ml_models["sbert"] = SentenceTransformer(local_model_path)
    else:
        print(" Локальная модель не найдена. Скачивание SBERT из интернета...")
        ml_models["sbert"] = SentenceTransformer('sberbank-ai/sbert_large_nlu_ru')
    
    print(f" Загрузка весов Keras из {WEIGHTS_PATH}...")
    classifier = create_model_architecture()
    
    if os.path.exists(WEIGHTS_PATH):
        classifier.load_weights(WEIGHTS_PATH)
        ml_models["classifier"] = classifier
        print("МОДЕЛИ ЗАГРУЖЕНЫ")
    else:
        print(f"ОШИБКА: Файл {WEIGHTS_PATH} не найден. Сервис не будет работать корректно")
    
    yield
    
    ml_models.clear()
    print("--- МОДЕЛИ ВЫГРУЖЕНЫ ---")

# ==========================================
# API (FASTAPI)
# ==========================================
app = FastAPI(title="News Classifier ML Service", lifespan=lifespan)

class NewsRequest(BaseModel):
    text: str

class NewsResponse(BaseModel):
    probabilities: dict[str, float]

@app.get("/")
def health_check():
    return {"status": "ok", "message": "ML Service is running"}

@app.post("/predict", response_model=NewsResponse)
def predict_news(request: NewsRequest):
    if "sbert" not in ml_models or "classifier" not in ml_models:
        raise HTTPException(status_code=500, detail="Models not loaded")

    text = request.text.strip()
    if not text:
        return {"probabilities": {label: 0.0 for label in LABEL_COLUMNS}}

    embeddings = ml_models["sbert"].encode([text])
    
    predictions = ml_models["classifier"].predict(embeddings, verbose=0)[0]
    
    result_dict = {
        label: float(prob) 
        for label, prob in zip(LABEL_COLUMNS, predictions)
    }
    
    return {"probabilities": result_dict}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
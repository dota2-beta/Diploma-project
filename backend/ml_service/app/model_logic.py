import os
import numpy as np
from tensorflow.keras import layers, Model
from sentence_transformers import SentenceTransformer
from .config import LABEL_COLUMNS, WEIGHTS_PATH, SBERT_PATH
from .ontology import get_ontology_features

ml_models = {}

def create_model_architecture():
    # Входы
    input_sbert = layers.Input(shape=(1024,), name='input_sbert')
    input_onto = layers.Input(shape=(12,), name='input_ontology')
    
    # Склейка
    combined = layers.Concatenate(name='concatenate')([input_sbert, input_onto])
    
    # Блок 1
    x = layers.Dense(512, name='dense')(combined)
    x = layers.BatchNormalization(name='batch_normalization')(x)
    x = layers.Activation('relu', name='activation')(x)
    x = layers.Dropout(0.5, name='dropout')(x)
    
    # Блок 2
    x = layers.Dense(256, name='dense_1')(x)
    x = layers.BatchNormalization(name='batch_normalization_1')(x)
    x = layers.Activation('relu', name='activation_1')(x)
    x = layers.Dropout(0.4, name='dropout_1')(x)
    
    # Выход
    output = layers.Dense(len(LABEL_COLUMNS), activation='sigmoid', name='output_layer')(x)
    
    return Model(inputs=[input_sbert, input_onto], outputs=output)

def load_models():
    if os.path.exists(SBERT_PATH):
        ml_models["sbert"] = SentenceTransformer(SBERT_PATH)
    else:
        ml_models["sbert"] = SentenceTransformer('sberbank-ai/sbert_large_nlu_ru')
    
    classifier = create_model_architecture()
    
    if os.path.exists(WEIGHTS_PATH):
        classifier.load_weights(WEIGHTS_PATH)
        ml_models["classifier"] = classifier
        print(f"✅ Веса загружены успешно из {WEIGHTS_PATH}")
    else:
        print(f"❌ ОШИБКА: Файл весов НЕ НАЙДЕН по пути: {os.path.abspath(WEIGHTS_PATH)}")
        print(f"Доступные файлы: {os.listdir('.')}")
    
    return ml_models

def run_prediction(text):
    if not text or not text.strip():
        return {label: 0.0 for label in LABEL_COLUMNS}
    
    emb = ml_models["sbert"].encode([text])
    onto = get_ontology_features(text).reshape(1, -1)
    preds = ml_models["classifier"].predict([emb, onto], verbose=0)[0]
    
    return {label: float(prob) for label, prob in zip(LABEL_COLUMNS, preds)}
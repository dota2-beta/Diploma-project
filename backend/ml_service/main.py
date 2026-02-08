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

# СПИСОК ИЗ 17 КАТЕГОРИЙ (В ТОЧНОМ ПОРЯДКЕ КАК ПРИ ОБУЧЕНИИ)
LABEL_COLUMNS = [
    'theme_science_research', 'theme_academic_process', 'theme_academic_contests',
    'theme_extracurricular', 'theme_sport', 'theme_culture_art',
    'theme_career_employment', 'theme_administration_official',
    'theme_partnership_collaboration', 'theme_civic_patriotic',
    'theme_admission_campaign', # <-- ВОТ ОНА, 17-Я
    'person_students', 'person_academics', 'person_staff_admin',
    'person_applicants', 'person_alumni', 'person_general'
]

ml_models = {}

# ==========================================
# МЕТОД ОНТОЛОГИИ (ДОЛЖЕН БЫТЬ 1 В 1 КАК В COLAB)
# ==========================================
def get_ontology_features(text):
    if not isinstance(text, str):
        return np.zeros(12)
    
    text = text.lower()
    ontology = {
        'official': ['приказ', 'распоряжение', 'ученый совет', 'ректор', 'проректор', 'деканат', 'управление', 'администрац', 'директор', 'хозяйствен', 'закупк', 'пожарн', 'безопасн'],
        'science': ['грант', 'рффи', 'рнф', 'статья', 'публикац', 'scopus', 'лаборатор', 'исследовани', 'научн', 'симпозиум', 'конференц', 'доклад', 'диссертац', 'патент', 'совет'],
        'academic': ['расписание', 'сессия', 'зачет', 'экзамен', 'пересдача', 'диплом', 'вкр', 'практика', 'учебн', 'семестр', 'лекция', 'инструкция', 'выдач'],
        'social': ['студсовет', 'профком', 'общежити', 'волонтер', 'доброволец', 'активист', 'квн', 'студвесна', 'социальн', 'молодежн', 'заселен'],
        'sport': ['чемпионат', 'матч', 'турнир', 'гто', 'спорт', 'сборная', 'тренер', 'соревнован', 'физкультур', 'олимпиад'],
        'culture': ['концерт', 'выставка', 'музей', 'театр', 'творческ', 'искусств', 'фестиваль', 'хор', 'ансамбль', 'музык'],
        'career': ['вакансия', 'работодатель', 'стажировка', 'трудоустройств', 'карьер', 'ярмарка вакансий', 'hh.ru', 'предприятие', 'резюме', 'soft skills'],
        'admission': ['егэ', 'абитуриент', 'приемная комиссия', 'поступлен', 'бакалавриат', 'магистратура', 'аспирантура', 'день открытых дверей', 'поступай', 'профориент'],
        'international': ['соглашение', 'меморандум', 'делегация', 'узбекистан', 'ташкент', 'иностранный', 'зарубежн', 'международн', 'обмен'],
        'patriotic': ['победа', 'ветеран', 'бессмертный полк', 'вов', 'патриот', 'возложение', 'отечество', 'герой', 'россия', '9 мая'],
        'safety_health': ['ковид', 'covid', 'вакцина', 'безопасность', 'мчс', 'здоровье', 'профилактика', 'масочный', 'инфекция', 'чс'],
        'university_brand': ['сгу', 'саратовский университет', 'чумаченко', 'комкова', 'бердникова', 'куликова', 'колледж', 'институт', 'факультет']
    }
    return np.array([1 if any(word in text for word in words) else 0 for words in ontology.values()])

# ==========================================
# ГИБРИДНАЯ АРХИТЕКТУРА (SBERT + ONTO)
# ==========================================
def create_model_architecture():
    # Вход 1: SBERT (1024)
    input_sbert = layers.Input(shape=(1024,), name='input_sbert')
    # Вход 2: Онтология (12)
    input_onto = layers.Input(shape=(12,), name='input_ontology')
    
    combined = layers.Concatenate()([input_sbert, input_onto])
    
    x = layers.Dense(512)(combined)
    x = layers.BatchNormalization()(x)
    x = layers.Activation('relu')(x)
    x = layers.Dropout(0.5)(x)
    
    x = layers.Dense(256)(x)
    x = layers.BatchNormalization()(x)
    x = layers.Activation('relu')(x)
    x = layers.Dropout(0.4)(x)
    
    # 17 выходов (sigmoid)
    outputs = layers.Dense(len(LABEL_COLUMNS), activation='sigmoid', name='output_layer')(x)
    
    model = Model(inputs=[input_sbert, input_onto], outputs=outputs)
    return model

# ==========================================
# ИНИЦИАЛИЗАЦИЯ (LIFESPAN)
# ==========================================
@asynccontextmanager
async def lifespan(app: FastAPI):
    print("--- ЗАГРУЗКА МОДЕЛЕЙ НАЧАТА ---")
    
    # Загрузка SBERT
    local_model_path = "./sbert_model"
    if os.path.exists(local_model_path):
        ml_models["sbert"] = SentenceTransformer(local_model_path)
    else:
        ml_models["sbert"] = SentenceTransformer('sberbank-ai/sbert_large_nlu_ru')
    
    # Загрузка классификатора
    classifier = create_model_architecture()
    if os.path.exists(WEIGHTS_PATH):
        # Теперь веса загрузятся, так как архитектура совпадает (17 выходов и 2 входа)
        classifier.load_weights(WEIGHTS_PATH)
        ml_models["classifier"] = classifier
        print("ГИБРИДНАЯ МОДЕЛЬ ЗАГРУЖЕНА")
    else:
        print(f"ОШИБКА: Веса {WEIGHTS_PATH} не найдены!")
    
    yield
    ml_models.clear()

app = FastAPI(title="News Classifier ML Service", lifespan=lifespan)

class NewsRequest(BaseModel):
    text: str

class NewsResponse(BaseModel):
    probabilities: dict[str, float]

@app.post("/predict", response_model=NewsResponse)
def predict_news(request: NewsRequest):
    if "sbert" not in ml_models or "classifier" not in ml_models:
        raise HTTPException(status_code=500, detail="Models not loaded")

    text = request.text.strip()
    if not text:
        return {"probabilities": {label: 0.0 for label in LABEL_COLUMNS}}

    # 1. Получаем SBERT эмбеддинг
    embeddings = ml_models["sbert"].encode([text])
    
    # 2. Получаем онтологические признаки
    onto_features = get_ontology_features(text).reshape(1, -1)
    
    # 3. Предсказание гибридной моделью (передаем список из двух входов)
    predictions = ml_models["classifier"].predict([embeddings, onto_features], verbose=0)[0]
    
    result_dict = {
        label: float(prob) 
        for label, prob in zip(LABEL_COLUMNS, predictions)
    }
    
    return {"probabilities": result_dict}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
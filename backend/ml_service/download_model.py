from sentence_transformers import SentenceTransformer
import os

MODEL_NAME = 'sberbank-ai/sbert_large_nlu_ru'
SAVE_PATH = './sbert_model'

if __name__ == "__main__":
    print(f"Начинаю скачивание модели {MODEL_NAME}...")
    
    model = SentenceTransformer(MODEL_NAME)
    
    print(f"Сохраняю модель в папку {SAVE_PATH}...")
    model.save(SAVE_PATH)
    
    print("Готово! Теперь модель лежит локально.")
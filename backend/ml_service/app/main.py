import asyncio
from fastapi import FastAPI
from contextlib import asynccontextmanager
from prometheus_fastapi_instrumentator import Instrumentator

from .model_logic import load_models, run_prediction
from .rabbit_worker import start_consumer
from .config import LABEL_COLUMNS

@asynccontextmanager
async def lifespan(app: FastAPI):
    print("Инициализация сервиса...")
    load_models()
    
    asyncio.create_task(start_consumer())
    yield
    print("Остановка сервиса...")

app = FastAPI(title="News Classifier ML Service", lifespan=lifespan)
Instrumentator().instrument(app).expose(app)

@app.post("/predict")
def predict_sync(request: dict):
    return {"probabilities": run_prediction(request.get("text", ""))}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
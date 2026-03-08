import json
import aio_pika
from .config import RABBIT_URL
from .model_logic import run_prediction

async def start_consumer():
    connection = await aio_pika.connect_robust(RABBIT_URL)
    
    async with connection:
        channel = await connection.channel()
        
        await channel.set_qos(prefetch_count=1)
        
        tasks_queue = await channel.declare_queue("news_tasks", durable=True)
        results_queue = await channel.declare_queue("news_results", durable=True)

        print("✅ --- Python Worker: ПОДКЛЮЧЕНО (robust). Слушаю очередь news_tasks ---")

        async with tasks_queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    try:
                        payload = json.loads(message.body.decode())
                        news_id = payload['id']
                        text = payload['text']

                        print(f"Разметка новости с ID: {news_id}")
                        
                        probabilities = run_prediction(text)

                        result = {
                            "id": news_id,
                            "probabilities": probabilities
                        }

                        await channel.default_exchange.publish(
                            aio_pika.Message(
                                body=json.dumps(result).encode(),
                                content_type="application/json"
                            ),
                            routing_key="news_results"
                        )
                        print(f"Результат по ID: {news_id} отправлен в news_results")
                        
                    except Exception as e:
                        print(f"❌ Ошибка при обработке сообщения: {e}")
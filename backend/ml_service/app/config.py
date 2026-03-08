import os

WEIGHTS_PATH = "best_model.weights.h5" 
SBERT_PATH = "./sbert_model"
RABBIT_URL = os.getenv("RABBITMQ_URL", "amqp://guest:guest@rabbitmq/")

LABEL_COLUMNS = [
    'theme_science_research', 'theme_academic_process', 'theme_academic_contests',
    'theme_extracurricular', 'theme_sport', 'theme_culture_art',
    'theme_career_employment', 'theme_administration_official',
    'theme_partnership_collaboration', 'theme_civic_patriotic',
    'theme_admission_campaign',
    'person_students', 'person_academics', 'person_staff_admin',
    'person_applicants', 'person_alumni', 'person_general'
]
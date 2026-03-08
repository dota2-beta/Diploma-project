import React, { useState } from 'react';
import { Container, Paper, Typography, Button, Box, CircularProgress } from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { getAuthHeader } from '../services/auth';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

const BulkUploadPage = ({ onUploadSuccess }) => {
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleFileChange = (e) => setFile(e.target.files[0]);

    const handleUpload = async () => {
        if (!file) return;
        setLoading(true);

        const formData = new FormData();
        formData.append('file', file);

        try {
            // Отправляем файл на сервер
            await axios.post('http://localhost:8080/api/news/upload', formData, {
                headers: {
                    ...getAuthHeader(),
                    'Content-Type': 'multipart/form-data'
                }
            });
            
            // Вызываем уведомление из App.js
            if (onUploadSuccess) {
                onUploadSuccess("Файл принят! Новости сохраняются в базу и размечаются в фоновом режиме.", "success");
            }

            // Сразу уходим на главную страницу
            navigate('/');
            
        } catch (err) {
            console.error(err);
            if (onUploadSuccess) {
                onUploadSuccess("Ошибка при загрузке файла. Проверьте соединение и формат.", "error");
            }
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm" sx={{ py: 8 }}>
            <Paper elevation={3} sx={{ p: 4, textAlign: 'center', borderRadius: 2 }}>
                <Typography variant="h5" sx={{ mb: 3, fontWeight: 'bold' }}>
                    Массовый импорт новостей
                </Typography>
                
                <Typography variant="body2" color="text.secondary" sx={{ mb: 4 }}>
                    Выберите Excel файл (.xlsx). Система автоматически проверит новости на дубликаты и запустит интеллектуальную разметку.
                </Typography>

                <Box sx={{ mb: 4 }}>
                    <Button
                        variant="outlined"
                        component="label"
                        startIcon={<CloudUploadIcon />}
                        fullWidth
                        size="large"
                    >
                        {file ? file.name : "Выбрать файл"}
                        <input type="file" hidden onChange={handleFileChange} accept=".xlsx, .xls" />
                    </Button>
                </Box>

                <Box sx={{ display: 'flex', gap: 2 }}>
                    <Button 
                        variant="text" 
                        fullWidth 
                        onClick={() => navigate('/')}
                        disabled={loading}
                    >
                        Отмена
                    </Button>
                    <Button 
                        variant="contained" 
                        fullWidth 
                        disabled={!file || loading} 
                        onClick={handleUpload}
                    >
                        {loading ? <CircularProgress size={24} color="inherit" /> : "Загрузить новости"}
                    </Button>
                </Box>
            </Paper>
        </Container>
    );
};

export default BulkUploadPage;
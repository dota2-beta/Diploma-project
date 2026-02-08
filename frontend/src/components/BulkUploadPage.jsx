import React, { useState } from 'react';
import { Container, Paper, Typography, Button, Box, CircularProgress, Alert, List, ListItem, ListItemText } from '@mui/material';
import axios from 'axios';
import { getAuthHeader } from '../services/auth';
import CloudUploadIcon from '@mui/icons-material/CloudUpload';

const BulkUploadPage = () => {
    const [file, setFile] = useState(null);
    const [loading, setLoading] = useState(false);
    const [result, setResult] = useState(null);

    const handleFileChange = (e) => setFile(e.target.files[0]);

    const handleUpload = async () => {
        if (!file) return;
        setLoading(true);
        setResult(null);

        const formData = new FormData();
        formData.append('file', file);

        try {
            const response = await axios.post('http://localhost:8080/api/news/upload', formData, {
                headers: {
                    ...getAuthHeader(),
                    'Content-Type': 'multipart/form-data'
                }
            });
            setResult(response.data);
        } catch (err) {
            alert("Ошибка при загрузке. Проверьте формат файла.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="sm" sx={{ py: 4 }}>
            <Paper sx={{ p: 4, textAlign: 'center' }}>
                <Typography variant="h5" sx={{ mb: 3, fontWeight: 'bold' }}>Загрузка новостей из Excel</Typography>
                
                <Button
                    variant="outlined"
                    component="label"
                    startIcon={<CloudUploadIcon />}
                    sx={{ mb: 3 }}
                >
                    Выбрать файл (XLS/XLSX)
                    <input type="file" hidden onChange={handleFileChange} accept=".xlsx, .xls" />
                </Button>
                
                {file && <Typography sx={{ mb: 2 }}>Файл: {file.name}</Typography>}

                <Button 
                    variant="contained" 
                    fullWidth 
                    disabled={!file || loading} 
                    onClick={handleUpload}
                >
                    {loading ? <CircularProgress size={24} color="inherit" /> : "Начать импорт и разметку ИИ"}
                </Button>

                {result && (
                    <Box sx={{ mt: 4, textAlign: 'left' }}>
                        <Alert severity={result.errorCount === 0 ? "success" : "warning"}>
                            Успешно: {result.successCount} | Ошибок: {result.errorCount}
                        </Alert>
                        {result.failedTitles.length > 0 && (
                            <Box sx={{ mt: 2 }}>
                                <Typography variant="subtitle2" color="error">Не удалось загрузить:</Typography>
                                <List dense>
                                    {result.failedTitles.map((t, i) => (
                                        <ListItem key={i}><ListItemText primary={t} /></ListItem>
                                    ))}
                                </List>
                            </Box>
                        )}
                    </Box>
                )}
            </Paper>
        </Container>
    );
};

export default BulkUploadPage;
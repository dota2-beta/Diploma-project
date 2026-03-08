import React, { useState } from 'react';
import { Container, TextField, Button, Typography, Paper, Box, CircularProgress } from '@mui/material';
import axios from 'axios';
import { getAuthHeader } from '../services/auth';
import { useNavigate } from 'react-router-dom';

const AddNewsPage = ({ onAddSuccess }) => {
    const [form, setForm] = useState({ title: '', content: '', link: '', dateStr: '' });
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            await axios.post('http://localhost:8080/api/news', form, {
                headers: getAuthHeader()
            });
            
            if (onAddSuccess) {
                onAddSuccess("Новость добавлена и отправлена на разметку!", "success");
            }
            navigate('/');
        } catch (err) {
            if (onAddSuccess) 
                onAddSuccess("Ошибка при сохранении новости", "error");
        } finally {
            setLoading(false);
        }
    };

    return (
        <Container maxWidth="md" sx={{ py: 4 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold' }}>
                    Новая новость
                </Typography>
                <form onSubmit={handleSubmit}>
                    <TextField fullWidth label="Заголовок" required margin="normal"
                        value={form.title} onChange={(e) => setForm({...form, title: e.target.value})} />
                    <TextField fullWidth label="Текст новости" required multiline rows={8} margin="normal"
                        value={form.content} onChange={(e) => setForm({...form, content: e.target.value})} />
                    <TextField fullWidth label="Ссылка на источник" margin="normal"
                        value={form.link} onChange={(e) => setForm({...form, link: e.target.value})} />
                    <TextField fullWidth label="Дата (дд.мм.гггг)" placeholder="Напр: 01.02.2026" margin="normal"
                        value={form.dateStr} onChange={(e) => setForm({...form, dateStr: e.target.value})} />
                    
                    <Box sx={{ mt: 3, textAlign: 'right' }}>
                        <Button variant="outlined" onClick={() => navigate('/')} sx={{ mr: 2 }}>
                            Отмена
                        </Button>
                        <Button type="submit" variant="contained" disabled={loading} size="large">
                            {loading ? <CircularProgress size={24} /> : "Опубликовать"}
                        </Button>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
};

export default AddNewsPage;
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { 
    Container, TextField, Button, Typography, 
    Paper, Box, CircularProgress, Alert 
} from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import SaveIcon from '@mui/icons-material/Save';
import { getAuthHeader } from '../services/auth';

const EditNewsPage = ({ onUpdateSuccess }) => {
    const { id } = useParams();
    const navigate = useNavigate();
    
    const [form, setForm] = useState({
        title: '',
        content: '',
        originalLink: '',
    });
    
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchNews = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/news/${id}`);
                setForm({
                    title: response.data.title || '',
                    content: response.data.content || '',
                    originalLink: response.data.originalLink || ''
                });
            } catch (err) {
                setError("Не удалось загрузить новость для редактирования.");
                console.error(err);
            } finally {
                setLoading(false);
            }
        };
        fetchNews();
    }, [id]);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setSaving(true);
        try {
            await axios.put(`http://localhost:8080/api/news/${id}`, form, {
                headers: getAuthHeader()
            });
            
            if (onUpdateSuccess) onUpdateSuccess();
            navigate(`/news/${id}`);
        } catch (err) {
            alert("Ошибка при сохранении. Проверьте права доступа.");
            console.error(err);
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}>
                <CircularProgress />
            </Box>
        );
    }

    return (
        <Container maxWidth="md" sx={{ py: 4 }}>
            <Button 
                startIcon={<ArrowBackIcon />} 
                onClick={() => navigate(-1)} 
                sx={{ mb: 2 }}
            >
                Назад
            </Button>

            <Paper elevation={3} sx={{ p: 4 }}>
                <Typography variant="h5" gutterBottom sx={{ fontWeight: 'bold', mb: 3 }}>
                    Редактирование новости #{id}
                </Typography>

                {error && <Alert severity="error" sx={{ mb: 3 }}>{error}</Alert>}

                <form onSubmit={handleSubmit}>
                    <TextField
                        fullWidth
                        label="Заголовок"
                        required
                        margin="normal"
                        value={form.title}
                        onChange={(e) => setForm({ ...form, title: e.target.value })}
                    />

                    <TextField
                        fullWidth
                        label="Текст новости"
                        required
                        multiline
                        rows={12}
                        margin="normal"
                        value={form.content}
                        onChange={(e) => setForm({ ...form, content: e.target.value })}
                    />

                    <TextField
                        fullWidth
                        label="Ссылка на источник"
                        margin="normal"
                        value={form.originalLink}
                        onChange={(e) => setForm({ ...form, originalLink: e.target.value })}
                    />

                    <Box sx={{ mt: 4, display: 'flex', gap: 2, justifyContent: 'flex-end' }}>
                        <Button 
                            variant="outlined" 
                            onClick={() => navigate(`/news/${id}`)}
                            disabled={saving}
                        >
                            Отмена
                        </Button>
                        <Button 
                            type="submit" 
                            variant="contained" 
                            color="primary"
                            startIcon={saving ? <CircularProgress size={20} color="inherit" /> : <SaveIcon />}
                            disabled={saving}
                        >
                            {saving ? "Сохранение..." : "Сохранить изменения"}
                        </Button>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
};

export default EditNewsPage;
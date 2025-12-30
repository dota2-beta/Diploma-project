import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Container, Typography, Chip, Stack, Box, Button, Paper, CircularProgress, Link } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { CATEGORIES } from '../constants';

const NewsPage = () => {
    const { id } = useParams();
    const navigate = useNavigate();
    const [news, setNews] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        const fetchOneNews = async () => {
            try {
                const response = await axios.get(`http://localhost:8080/api/news/${id}`);
                setNews(response.data);
            } catch (error) {
                console.error("Ошибка:", error);
            } finally {
                setLoading(false);
            }
        };
        fetchOneNews();
    }, [id]);

    if (loading) return <Box sx={{ display: 'flex', justifyContent: 'center', mt: 10 }}><CircularProgress /></Box>;
    if (!news) return <Typography align="center" sx={{ mt: 10 }}>Новость не найдена</Typography>;

    const getTags = () => {
        const tags = [];
        Object.keys(CATEGORIES).forEach(key => {
            if (news[key] && news[key] > 0.3) {
                tags.push({ label: `${CATEGORIES[key]} ${Math.round(news[key] * 100)}%`, score: news[key] });
            }
        });
        return tags.sort((a, b) => b.score - a.score);
    };

    return (
        <Container maxWidth="md" sx={{ py: 4 }}>
            <Button 
                startIcon={<ArrowBackIcon />} 
                onClick={() => navigate(-1)} // Кнопка Назад
                sx={{ mb: 2 }}
            >
                Назад к ленте
            </Button>

            <Paper elevation={3} sx={{ p: 4 }}>
                {/* Дата */}
                <Typography variant="caption" color="text.secondary">
                    {new Date(news.publishedDate).toLocaleDateString()}
                </Typography>

                {/* Заголовок */}
                <Typography variant="h4" component="h1" sx={{ fontWeight: 'bold', mt: 1, mb: 2 }}>
                    {news.title}
                </Typography>

                {/* Теги */}
                <Stack direction="row" spacing={1} sx={{ mb: 3, flexWrap: 'wrap', gap: 1 }}>
                    {getTags().map((tag) => (
                        <Chip 
                            key={tag.label} 
                            label={tag.label} 
                            color={tag.score > 0.8 ? "success" : "primary"} 
                            variant={tag.score > 0.8 ? "filled" : "outlined"}
                        />
                    ))}
                </Stack>

                {/* Полный текст */}
                <Typography variant="body1" sx={{ whiteSpace: 'pre-wrap', lineHeight: 1.8 }}>
                    {news.content}
                </Typography>

                {news.originalLink && (
                    <Box sx={{ mt: 4, pt: 2, borderTop: '1px solid #eee' }}>
                        <Typography variant="body2" color="text.secondary">
                            Источник: <Link href={news.originalLink} target="_blank" rel="noopener">{news.originalLink}</Link>
                        </Typography>
                    </Box>
                )}
            </Paper>
        </Container>
    );
};

export default NewsPage;
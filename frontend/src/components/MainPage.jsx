import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Container, Grid, Typography, CircularProgress, Box, TextField } from '@mui/material';
import NewsCard from './NewsCard';
import Sidebar from './Sidebar';
import { CATEGORIES } from '../constants';

const initialFilters = {};
Object.keys(CATEGORIES).forEach(key => initialFilters[key] = 0);

function MainPage() {
    const [news, setNews] = useState([]);
    const [loading, setLoading] = useState(false);
    const [filters, setFilters] = useState(initialFilters);
    
    const [search, setSearch] = useState('');
    const [debouncedSearch, setDebouncedSearch] = useState('');

    useEffect(() => {
        const handler = setTimeout(() => {
            setDebouncedSearch(search);
        }, 600);

        return () => clearTimeout(handler);
    }, [search]);

    const fetchNews = async () => {
        setLoading(true);
        try {
            const params = { 
                size: 50,
                search: debouncedSearch
            };
            
            Object.keys(filters).forEach(key => {
                if (filters[key] > 0) {
                    params[key] = filters[key];
                }
            });

            const response = await axios.get('http://localhost:8080/api/news', { params });
            setNews(response.data.content);
        } catch (error) {
            console.error("Ошибка загрузки новостей:", error);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchNews();
    }, [filters, debouncedSearch]);

    return (
        <Box sx={{ flexGrow: 1, bgcolor: '#f5f5f5', minHeight: '100vh' }}>
            <Grid container spacing={2}>
                <Grid item xs={12} md={3}>
                    <Sidebar 
                        filters={filters} 
                        setFilters={setFilters} 
                        onReset={() => {
                            setFilters(initialFilters);
                            setSearch('');
                        }} 
                    />
                </Grid>

                <Grid item xs={12} md={9} sx={{ p: 3 }}>
                    <Container maxWidth="md">
                        <TextField 
                            fullWidth 
                            variant="outlined" 
                            placeholder="Поиск по заголовкам и тексту..." 
                            value={search}
                            onChange={(e) => setSearch(e.target.value)}
                            sx={{ mb: 4, bgcolor: '#fff', borderRadius: 1 }}
                        />

                        <Typography variant="h4" sx={{ mb: 3, fontWeight: 'bold' }}>
                            Лента новостей университета
                        </Typography>
                        
                        {loading ? (
                            <Box sx={{ display: 'flex', justifyContent: 'center', mt: 5 }}>
                                <CircularProgress />
                            </Box>
                        ) : (
                            <>
                                <Typography variant="subtitle1" sx={{ mb: 2, color: 'text.secondary' }}>
                                    Найдено новостей: {news.length}
                                </Typography>
                                {news.map(item => (
                                    <NewsCard key={item.id} news={item} />
                                ))}
                                {news.length === 0 && (
                                    <Typography variant="h6" align="center" sx={{ mt: 5 }}>
                                        Ничего не найдено. Попробуйте изменить поиск или фильтры.
                                    </Typography>
                                )}
                            </>
                        )}
                    </Container>
                </Grid>
            </Grid>
        </Box>
    );
}

export default MainPage;
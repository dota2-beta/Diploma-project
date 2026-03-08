import React from 'react';
import { Card, CardContent, Typography, Chip, Stack, Button, CardActions } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import { CATEGORIES } from '../constants';

const NewsCard = ({ news }) => {
    const getTags = () => {
        const tags = [];
        Object.keys(CATEGORIES).forEach(key => {
            if (news[key] && news[key] > 0.3) {
                tags.push({ 
                    label: `${CATEGORIES[key]} ${Math.round(news[key] * 100)}%`, 
                    score: news[key] 
                });
            }
        });
        return tags.sort((a, b) => b.score - a.score);
    };

    return (
        <Card sx={{ mb: 2, boxShadow: 3, display: 'flex', flexDirection: 'column', height: '100%' }}>
            <CardContent sx={{ flexGrow: 1 }}>
                <Typography variant="caption" color="text.secondary">
                    {new Date(news.publishedDate).toLocaleDateString()}
                </Typography>
                
                <Typography variant="h6" component="div" sx={{ fontWeight: 'bold', mt: 1, lineHeight: 1.3 }}>
                    {news.title}
                </Typography>

                <Stack direction="row" spacing={1} sx={{ mt: 1.5, mb: 1.5, flexWrap: 'wrap', gap: 1 }}>
                    {getTags().map((tag) => (
                        <Chip 
                            key={tag.label} 
                            label={tag.label} 
                            size="small" 
                            color={tag.score > 0.8 ? "success" : "primary"} 
                            variant={tag.score > 0.8 ? "filled" : "outlined"}
                        />
                    ))}
                    {!news.isAnalyzed && (
                        <Chip 
                            label="Обработка ИИ..." 
                            size="small" 
                            variant="outlined" 
                            sx={{ fontStyle: 'italic', borderStyle: 'dashed' }} 
                        />
                    )}
                </Stack>

                <Typography variant="body2" color="text.secondary">
                    {news.content.length > 150 ? news.content.substring(0, 150) + "..." : news.content}
                </Typography>
            </CardContent>

            <CardActions sx={{ p: 2, pt: 0 }}>
                {/* Кнопка ведет на /news/ID */}
                <Button 
                    component={RouterLink} 
                    to={`/news/${news.id}`} 
                    size="small" 
                    variant="contained"
                >
                    Читать полностью
                </Button>
            </CardActions>
        </Card>
    );
};

export default NewsCard;
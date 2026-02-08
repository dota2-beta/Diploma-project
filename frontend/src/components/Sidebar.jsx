import React from 'react';
import { Paper, Typography, Slider, Box, Divider, Button } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import { CATEGORIES, THEME_KEYS, PERSON_KEYS } from '../constants';
import { isAuthenticated, logout } from '../services/auth'

const Sidebar = ({ filters, setFilters, onReset }) => {
    const navigate = useNavigate();
    const loggedIn = isAuthenticated();

    const handleChange = (key) => (event, newValue) => {
        setFilters(prev => ({ ...prev, [key]: newValue / 100 }));
    };

    const handleLogout = () => {
        logout();
        navigate('/');
    };

    const renderSlider = (key) => (
        <Box key={key} sx={{ mb: 2 }}>
            <Typography variant="body2" gutterBottom>
                {CATEGORIES[key]} {filters[key] > 0 && `(>${Math.round(filters[key] * 100)}%)`}
            </Typography>
            <Slider
                size="small"
                value={filters[key] * 100 || 0}
                onChange={handleChange(key)}
                valueLabelDisplay="auto"
                min={0}
                max={100}
                step={5}
                sx={{ color: filters[key] > 0 ? 'primary.main' : 'grey.400' }}
            />
        </Box>
    );

    return (
        <Paper sx={{ p: 2, height: '100vh', overflowY: 'auto', position: 'sticky', top: 0, display: 'flex', flexDirection: 'column' }}>
            <Box sx={{ flexGrow: 1 }}>
                <Typography variant="h6" gutterBottom color="primary" sx={{ fontWeight: 'bold' }}>
                    Умный фильтр
                </Typography>
                
                <Button variant="outlined" fullWidth size="small" onClick={onReset} sx={{ mb: 2 }}>
                    Сбросить все
                </Button>

                <Divider sx={{ mb: 2 }}>ТЕМАТИКА</Divider>
                {THEME_KEYS.map(renderSlider)}

                <Divider sx={{ my: 2 }}>АУДИТОРИЯ</Divider>
                {PERSON_KEYS.map(renderSlider)}
            </Box>
        </Paper>
    );
};

export default Sidebar;
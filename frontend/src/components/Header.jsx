import React from 'react';
import { AppBar, Toolbar, Typography, Button, Box, Container } from '@mui/material';
import { useNavigate, Link as RouterLink } from 'react-router-dom';
import { isAuthenticated, logout } from '../services/auth';
import AddIcon from '@mui/icons-material/Add';
import LoginIcon from '@mui/icons-material/Login';
import LogoutIcon from '@mui/icons-material/Logout';
import FileUploadIcon from '@mui/icons-material/FileUpload';

const Header = () => {
    const navigate = useNavigate();
    const loggedIn = isAuthenticated();

    const handleLogout = () => {
        logout();
        navigate('/');
        window.location.reload(); 
    };

    return (
        <AppBar position="sticky" sx={{ bgcolor: '#fff', color: '#333', boxShadow: 1 }}>
            <Container maxWidth="lg">
                <Toolbar disableGutters>
                    {/* Логотип */}
                    <Typography 
                        variant="h6" 
                        component={RouterLink} 
                        to="/" 
                        sx={{ 
                            flexGrow: 1, 
                            fontWeight: 'bold', 
                            textDecoration: 'none', 
                            color: 'primary.main',
                            display: 'flex',
                            alignItems: 'center'
                        }}
                    >
                        INews <span style={{ color: '#666', fontWeight: 'normal', marginLeft: '8px', fontSize: '0.9rem' }}>| Интеллектуальная лента</span>
                    </Typography>

                    <Box sx={{ display: 'flex', gap: 1.5 }}>
                        {loggedIn ? (
                            <>
                                <Button 
                                    startIcon={<AddIcon />} 
                                    variant="contained" 
                                    color="success"
                                    onClick={() => navigate('/admin/add')}
                                    size="small"
                                >
                                    Добавить
                                </Button>

                                <Button 
                                    startIcon={<FileUploadIcon />} 
                                    variant="contained" 
                                    color="info"
                                    onClick={() => navigate('/admin/upload')}
                                    size="small"
                                >
                                    Импорт
                                </Button>

                                <Button 
                                    startIcon={<LogoutIcon />} 
                                    variant="outlined" 
                                    color="inherit"
                                    onClick={handleLogout}
                                    size="small"
                                >
                                    Выход
                                </Button>
                            </>
                        ) : (
                            <Button 
                                startIcon={<LoginIcon />} 
                                variant="outlined" 
                                onClick={() => navigate('/login')}
                                size="small"
                            >
                                Войти
                            </Button>
                        )}
                    </Box>
                </Toolbar>
            </Container>
        </AppBar>
    );
};

export default Header;
import React, { useState } from 'react';
import { Container, Paper, TextField, Button, Typography, Alert } from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { saveAuth } from '../services/auth';

const LoginPage = ({ onLoginSuccess }) => {
    const [username, setUsername] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = async (e) => {
        e.preventDefault();
        const authString = btoa(`${username}:${password}`);
        try {
            await axios.post('http://localhost:8080/api/auth/login', 
                { username, password }, 
                { headers: { 'Authorization': `Basic ${authString}` } }
            );
            saveAuth(username, password);
            if (onLoginSuccess) onLoginSuccess('Вход выполнен успешно!', 'success');
            navigate('/'); 
        } catch (err) {
            setError('Неверный логин или пароль');
        }
    };
    

    return (
        <Container maxWidth="xs" sx={{ mt: 10 }}>
            <Paper sx={{ p: 4 }}>
                <Typography variant="h5" align="center" gutterBottom sx={{ fontWeight: 'bold' }}>
                    Вход в систему
                </Typography>
                <form onSubmit={handleLogin}>
                    <TextField 
                        fullWidth label="Логин" margin="normal" 
                        value={username} onChange={(e) => setUsername(e.target.value)} 
                    />
                    <TextField 
                        fullWidth label="Пароль" type="password" margin="normal" 
                        value={password} onChange={(e) => setPassword(e.target.value)} 
                    />
                    {error && <Alert severity="error" sx={{ mt: 2 }}>{error}</Alert>}
                    <Button type="submit" variant="contained" fullWidth sx={{ mt: 3, py: 1.5 }}>
                        Войти
                    </Button>
                </form>
            </Paper>
        </Container>
    );
};

export default LoginPage;
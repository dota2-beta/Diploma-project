import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Snackbar, Alert } from '@mui/material';
import Header from './components/Header';
import MainPage from './components/MainPage';
import NewsPage from './components/NewsPage';
import LoginPage from './components/LoginPage';
import AddNewsPage from './components/AddNewsPage';
import BulkUploadPage from './components/BulkUploadPage'; 

function App() {
  const [notify, setNotify] = useState({ open: false, message: '', severity: 'success' });

  const showNotify = (message, severity = 'success') => {
    setNotify({ open: true, message, severity });
  };

  const handleCloseNotify = () => setNotify({ ...notify, open: false });

  return (
    <BrowserRouter>
      <Header />
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/news/:id" element={<NewsPage />} />
        <Route path="/login" element={<LoginPage onLoginSuccess={() => showNotify('Вход выполнен!', 'success')} />} />
        <Route path="/admin/add" element={<AddNewsPage onAddSuccess={() => showNotify('Новость добавлена и размечена ИИ!', 'success')} />} />
        <Route path="/admin/upload" element={<BulkUploadPage onUploadSuccess={() => showNotify('Файл обработан!', 'success')} />} />
      </Routes>

      <Snackbar 
        open={notify.open} 
        autoHideDuration={4000} 
        onClose={handleCloseNotify}
        anchorOrigin={{ vertical: 'bottom', horizontal: 'center' }}
      >
        <Alert onClose={handleCloseNotify} severity={notify.severity} sx={{ width: '100%' }}>
          {notify.message}
        </Alert>
      </Snackbar>
    </BrowserRouter>
  );
}

export default App;
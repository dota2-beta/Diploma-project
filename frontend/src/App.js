import React, { useState } from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { Snackbar, Alert } from '@mui/material';
import Header from './components/Header';
import MainPage from './components/MainPage';
import NewsPage from './components/NewsPage';
import LoginPage from './components/LoginPage';
import AddNewsPage from './components/AddNewsPage';
import BulkUploadPage from './components/BulkUploadPage'; 
import EditNewsPage from './components/EditNewsPage';

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
        <Route 
          path="/admin/add" 
          element={<AddNewsPage onAddSuccess={showNotify} />} 
        />
        <Route 
          path="/admin/upload" 
          element={<BulkUploadPage onUploadSuccess={(msg, sev) => showNotify(msg, sev)} />} 
        />
        <Route 
          path="/admin/edit/:id" 
          element={<EditNewsPage onUpdateSuccess={() => showNotify('Новость обновлена!', 'success')} />} 
        />
      </Routes>

      <Snackbar 
        open={notify.open} 
        autoHideDuration={6000}
        onClose={handleCloseNotify}
        anchorOrigin={{ vertical: 'top', horizontal: 'right' }}
      >
        <Alert onClose={handleCloseNotify} severity={notify.severity} sx={{ width: '100%', whiteSpace: 'pre-line' }}>
          {notify.message}
        </Alert>
      </Snackbar>
    </BrowserRouter>
  );
}

export default App;
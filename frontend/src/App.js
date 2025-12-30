import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import MainPage from './components/MainPage';
import NewsPage from './components/NewsPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<MainPage />} />
        <Route path="/news/:id" element={<NewsPage />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;
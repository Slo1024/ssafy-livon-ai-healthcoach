import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import './styles/global.css';
import './styles/variables.css';
import { Layout } from './components/layout/Layout';
import { HomePage } from './pages/main/HomePage';
import { AboutPage } from './pages/main/AboutPage';
import { DownloadPage } from './pages/main/DownloadPage';
import { LoginPage } from './pages/auth/LoginPage';
import { SignupPage } from './pages/auth/SignupPage';
import { TermsPage } from './pages/main/TermsPage';

function App() {
  return (
    <Router>
      <Layout>
        <Routes>
          <Route path="/" element={<HomePage />} />
          <Route path="/about" element={<AboutPage />} />
          <Route path="/download" element={<DownloadPage />} />
          <Route path="/auth/login" element={<LoginPage />} />
          <Route path="/auth/signup" element={<SignupPage />} />
          <Route path="/terms" element={<TermsPage />} />
        </Routes>
      </Layout>
    </Router>
  );
}

export default App;
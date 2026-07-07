import { useEffect } from 'react';
import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import BottomNav from '@/components/layout/BottomNav';
import ToastContainer from '@/components/ui/ToastContainer';
import { useStore } from '@/store';
import HomePage from '@/pages/HomePage';
import ActivitiesPage from '@/pages/ActivitiesPage';
import VipPage from '@/pages/VipPage';
import ProfilePage from '@/pages/ProfilePage';
import LoginPage from '@/pages/LoginPage';
import PostDetailPage from '@/pages/PostDetailPage';
import PublishPage from '@/pages/PublishPage';
import MessagesPage from '@/pages/MessagesPage';
import SettingsPage from '@/pages/SettingsPage';
import RedeemPage from '@/pages/RedeemPage';
import SearchPage from '@/pages/SearchPage';
import ActivityDetailPage from '@/pages/ActivityDetailPage';
import ChatPage from '@/pages/ChatPage';
import EditProfilePage from '@/pages/EditProfilePage';

function AppContent() {
  const { init, isLoggedIn, loadChats, loadNotifications } = useStore();
  const location = useLocation();

  useEffect(() => {
    init();
  }, []);

  useEffect(() => {
    if (isLoggedIn) {
      loadChats();
      loadNotifications();
    }
  }, [isLoggedIn]);

  useEffect(() => {
    window.scrollTo(0, 0);
  }, [location.pathname]);

  return (
    <div className="max-w-[480px] mx-auto min-h-screen bg-white relative">
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/" element={<HomePage />} />
        <Route path="/activities" element={<ActivitiesPage />} />
        <Route path="/vip" element={<VipPage />} />
        <Route path="/profile" element={<ProfilePage />} />
        <Route path="/post/:id" element={<PostDetailPage />} />
        <Route path="/publish" element={<PublishPage />} />
        <Route path="/messages" element={<MessagesPage />} />
        <Route path="/chat/:chatId" element={<ChatPage />} />
        <Route path="/settings" element={<SettingsPage />} />
        <Route path="/redeem" element={<RedeemPage />} />
        <Route path="/search" element={<SearchPage />} />
        <Route path="/activity/:id" element={<ActivityDetailPage />} />
        <Route path="/edit-profile" element={<EditProfilePage />} />
      </Routes>
      <BottomNav />
      <ToastContainer />
    </div>
  );
}

export default function App() {
  return (
    <Router>
      <AppContent />
    </Router>
  );
}

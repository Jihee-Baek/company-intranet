import React, { useEffect, useState } from 'react';
import { NavLink, Outlet, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getUnreadCount } from '../api/files';

export default function Layout() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [unread, setUnread] = useState(0);

  useEffect(() => {
    getUnreadCount()
      .then((res) => setUnread(res.data.data))
      .catch(() => {});
  }, []);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  return (
    <div className="app-layout">
      <aside className="sidebar">
        <div className="sidebar-logo">FileHub</div>
        <nav className="sidebar-nav">
          <NavLink to="/" end className={({ isActive }) => isActive ? 'active' : ''}>
            <span>📥</span>
            받은 파일함
            {unread > 0 && <span className="badge">{unread}</span>}
          </NavLink>
          <NavLink to="/sent" className={({ isActive }) => isActive ? 'active' : ''}>
            <span>📤</span>
            보낸 파일함
          </NavLink>
          <NavLink to="/upload" className={({ isActive }) => isActive ? 'active' : ''}>
            <span>⬆️</span>
            파일 업로드
          </NavLink>
          <NavLink to="/notices" className={({ isActive }) => isActive ? 'active' : ''}>
            <span>📢</span>
            공지사항
          </NavLink>
        </nav>
        <div className="sidebar-user">
          <div className="user-name">{user?.name}</div>
          <div className="user-dept">{user?.department} · {user?.employeeId}</div>
          <button
            onClick={handleLogout}
            style={{
              marginTop: 12, background: 'rgba(255,255,255,0.1)',
              border: 'none', color: '#fff', padding: '6px 16px',
              borderRadius: 6, cursor: 'pointer', fontSize: 13
            }}
          >
            로그아웃
          </button>
        </div>
      </aside>
      <main className="main-content">
        <Outlet />
      </main>
    </div>
  );
}

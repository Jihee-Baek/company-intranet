import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { useAuth } from '../context/AuthContext';
import { getNotice, deleteNotice } from '../api/notices';

function formatDateTime(str) {
  if (!str) return '-';
  const d = new Date(str);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
}

export default function NoticeDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const [notice, setNotice] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    getNotice(id)
      .then((res) => setNotice(res.data.data))
      .catch(() => {
        toast.error('공지사항을 불러올 수 없습니다');
        navigate('/notices');
      })
      .finally(() => setLoading(false));
  }, [id, navigate]);

  const handleDelete = () => {
    if (!window.confirm('공지사항을 삭제하시겠습니까?')) return;
    deleteNotice(id)
      .then(() => {
        toast.success('공지사항이 삭제되었습니다');
        navigate('/notices');
      })
      .catch(() => toast.error('삭제에 실패했습니다'));
  };

  if (loading) {
    return <div style={{ padding: 48, textAlign: 'center', color: '#9ca3af' }}>로딩 중...</div>;
  }

  if (!notice) return null;

  return (
    <div>
      <button
        className="btn"
        style={{ marginBottom: 16, fontSize: 13 }}
        onClick={() => navigate('/notices')}
      >
        ← 목록으로
      </button>

      <div className="card">
        <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', gap: 12 }}>
          <h2 style={{ margin: 0, fontSize: 20, fontWeight: 600 }}>
            {notice.pinned && <span style={{ marginRight: 6 }}>📌</span>}
            {notice.title}
          </h2>
          {isAdmin && (
            <div style={{ display: 'flex', gap: 8, flexShrink: 0 }}>
              <button
                className="btn"
                style={{ fontSize: 13 }}
                onClick={() => navigate(`/notices/${id}/edit`)}
              >
                수정
              </button>
              <button
                className="btn"
                style={{ fontSize: 13, background: '#ef4444', color: '#fff', border: 'none' }}
                onClick={handleDelete}
              >
                삭제
              </button>
            </div>
          )}
        </div>

        <div style={{ marginTop: 8, fontSize: 13, color: '#6b7280' }}>
          {notice.authorName} · {formatDateTime(notice.createdAt)}
          {notice.createdAt !== notice.updatedAt && (
            <span style={{ marginLeft: 8 }}>(수정됨 {formatDateTime(notice.updatedAt)})</span>
          )}
        </div>

        <hr style={{ margin: '16px 0', border: 'none', borderTop: '1px solid #e5e7eb' }} />

        <div style={{ whiteSpace: 'pre-wrap', lineHeight: 1.7, fontSize: 15 }}>
          {notice.content}
        </div>
      </div>
    </div>
  );
}

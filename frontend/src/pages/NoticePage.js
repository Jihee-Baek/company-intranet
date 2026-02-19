import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { getNotices } from '../api/notices';

function formatDate(str) {
  const d = new Date(str);
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${month}.${day} ${hours}:${minutes}`;
}

export default function NoticePage() {
  const [notices, setNotices] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const { isAdmin } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    setLoading(true);
    getNotices(page, 10)
      .then((res) => {
        setNotices(res.data.data.content);
        setTotalPages(res.data.data.totalPages);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page]);

  return (
    <div>
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16 }}>
        <h1 className="page-title" style={{ marginBottom: 0 }}>공지사항</h1>
        {isAdmin && (
          <button className="btn btn-primary" onClick={() => navigate('/notices/write')}>
            + 공지 작성
          </button>
        )}
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: 48, textAlign: 'center', color: '#9ca3af' }}>로딩 중...</div>
        ) : notices.length === 0 ? (
          <div style={{ padding: 48, textAlign: 'center', color: '#9ca3af' }}>등록된 공지사항이 없습니다</div>
        ) : (
          notices.map((n) => (
            <div
              key={n.id}
              className="file-list-item"
              onClick={() => navigate(`/notices/${n.id}`)}
              style={{ cursor: 'pointer' }}
            >
              <div className="file-info">
                <div className="file-name">
                  {n.pinned && <span style={{ marginRight: 6 }}>📌</span>}
                  {n.title}
                </div>
                <div className="file-meta">
                  {n.authorName} · {formatDate(n.createdAt)}
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>이전</button>
          {Array.from({ length: totalPages }, (_, i) => (
            <button key={i} className={page === i ? 'active' : ''} onClick={() => setPage(i)}>
              {i + 1}
            </button>
          ))}
          <button disabled={page === totalPages - 1} onClick={() => setPage(page + 1)}>다음</button>
        </div>
      )}
    </div>
  );
}

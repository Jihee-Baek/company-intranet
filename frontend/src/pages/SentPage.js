import React, { useEffect, useState, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { getSentFiles } from '../api/files';

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function formatDate(str) {
  const d = new Date(str);
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  return `${month}.${day} ${hours}:${minutes}`;
}

function getFileIcon(type) {
  if (!type) return { icon: '📄', bg: '#f3f4f6' };
  if (type.startsWith('image/')) return { icon: '🖼️', bg: '#fef3c7' };
  if (type.includes('pdf')) return { icon: '📕', bg: '#fee2e2' };
  if (type.includes('word') || type.includes('document')) return { icon: '📘', bg: '#dbeafe' };
  if (type.includes('excel') || type.includes('sheet')) return { icon: '📗', bg: '#d1fae5' };
  if (type.includes('zip') || type.includes('compressed')) return { icon: '📦', bg: '#ede9fe' };
  return { icon: '📄', bg: '#f3f4f6' };
}

const SORT_OPTIONS = [
  { label: '날짜순', value: 'createdAt' },
  { label: '이름순', value: 'originalName' },
  { label: '크기순', value: 'fileSize' },
];

export default function SentPage() {
  const [files, setFiles] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [keyword, setKeyword] = useState('');
  const [sortBy, setSortBy] = useState('createdAt');
  const [sortDir, setSortDir] = useState('desc');
  const navigate = useNavigate();
  const debounceRef = useRef(null);

  useEffect(() => {
    setLoading(true);
    getSentFiles(page, 20, keyword || undefined, sortBy, sortDir)
      .then((res) => {
        setFiles(res.data.data.content);
        setTotalPages(res.data.data.totalPages);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, [page, keyword, sortBy, sortDir]);

  const handleKeywordChange = (e) => {
    const value = e.target.value;
    if (debounceRef.current) clearTimeout(debounceRef.current);
    debounceRef.current = setTimeout(() => {
      setKeyword(value);
      setPage(0);
    }, 300);
  };

  const toggleSortDir = () => {
    setSortDir((prev) => (prev === 'desc' ? 'asc' : 'desc'));
  };

  const getReadCount = (recipients) => {
    const read = recipients.filter((r) => r.isDownloaded).length;
    return `${read}/${recipients.length}`;
  };

  return (
    <div>
      <h1 className="page-title">보낸 파일함</h1>

      {/* 검색 + 정렬 */}
      <div className="card" style={{ display: 'flex', gap: 12, alignItems: 'center', flexWrap: 'wrap' }}>
        <input
          className="form-control"
          placeholder="파일명 검색..."
          style={{ flex: 1, minWidth: 200 }}
          onChange={handleKeywordChange}
        />
        <select className="form-control" style={{ width: 'auto' }}
          value={sortBy} onChange={(e) => { setSortBy(e.target.value); setPage(0); }}>
          {SORT_OPTIONS.map((o) => (
            <option key={o.value} value={o.value}>{o.label}</option>
          ))}
        </select>
        <button className="btn" onClick={toggleSortDir}
          style={{ padding: '8px 12px', fontSize: 14, minWidth: 40 }}>
          {sortDir === 'desc' ? '▼' : '▲'}
        </button>
      </div>

      <div className="card" style={{ padding: 0, overflow: 'hidden' }}>
        {loading ? (
          <div style={{ padding: 48, textAlign: 'center', color: '#9ca3af' }}>로딩 중...</div>
        ) : files.length === 0 ? (
          <div style={{ padding: 48, textAlign: 'center', color: '#9ca3af' }}>
            {keyword ? '검색 결과가 없습니다' : '보낸 파일이 없습니다'}
          </div>
        ) : (
          files.map((f) => {
            const { icon, bg } = getFileIcon(f.fileType);
            const allRead = f.recipients.every((r) => r.isDownloaded);
            return (
              <div key={f.id} className="file-list-item"
                onClick={() => navigate(`/files/${f.id}`)}>
                <div className="file-icon" style={{ background: bg }}>{icon}</div>
                <div className="file-info">
                  <div className="file-name">{f.originalName}</div>
                  <div className="file-meta">
                    수신: {f.recipients.map((r) => r.name).join(', ')} · {formatSize(f.fileSize)} · {formatDate(f.createdAt)}
                  </div>
                </div>
                <div className="file-status">
                  <span className={`download-status ${allRead ? 'read' : 'unread'}`}>
                    {getReadCount(f.recipients)} 확인
                  </span>
                </div>
              </div>
            );
          })
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

import React, { useState, useCallback } from 'react';
import { useDropzone } from 'react-dropzone';
import { useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { uploadFile } from '../api/files';
import { searchUsers } from '../api/users';

export default function UploadPage() {
  const [files, setFiles] = useState([]);
  const [recipients, setRecipients] = useState([]);
  const [memo, setMemo] = useState('');
  const [progress, setProgress] = useState(0);
  const [uploading, setUploading] = useState(false);
  const [searchKeyword, setSearchKeyword] = useState('');
  const [searchResults, setSearchResults] = useState([]);
  const navigate = useNavigate();

  const onDrop = useCallback((accepted) => {
    if (accepted.length > 0) setFiles((prev) => [...prev, ...accepted]);
  }, []);

  const { getRootProps, getInputProps, isDragActive } = useDropzone({
    onDrop,
    multiple: true,
    maxSize: 100 * 1024 * 1024,
  });

  const removeFile = (index) => {
    setFiles((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSearch = async (keyword) => {
    setSearchKeyword(keyword);
    if (keyword.length < 1) { setSearchResults([]); return; }
    try {
      const res = await searchUsers(keyword);
      const existing = new Set(recipients.map((r) => r.id));
      setSearchResults(res.data.data.filter((u) => !existing.has(u.id)));
    } catch { setSearchResults([]); }
  };

  const addRecipient = (user) => {
    setRecipients([...recipients, user]);
    setSearchKeyword('');
    setSearchResults([]);
  };

  const removeRecipient = (id) => {
    setRecipients(recipients.filter((r) => r.id !== id));
  };

  const formatSize = (bytes) => {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
  };

  const handleUpload = async () => {
    if (files.length === 0) { toast.error('파일을 선택해주세요'); return; }
    if (recipients.length === 0) { toast.error('수신자를 1명 이상 지정해주세요'); return; }

    setUploading(true);
    setProgress(0);

    const formData = new FormData();
    files.forEach((f) => formData.append('files', f));
    recipients.forEach((r) => formData.append('recipientIds', r.id));
    if (memo) formData.append('memo', memo);

    try {
      await uploadFile(formData, setProgress);
      toast.success('파일이 성공적으로 전송되었습니다!');
      navigate('/sent');
    } catch (err) {
      toast.error(err.response?.data?.message || '업로드에 실패했습니다');
    } finally {
      setUploading(false);
    }
  };

  return (
    <div>
      <h1 className="page-title">파일 업로드</h1>

      {/* Dropzone */}
      <div className="card">
        <div {...getRootProps()} className={`dropzone ${isDragActive ? 'active' : ''}`}>
          <input {...getInputProps()} />
          <div className="dropzone-icon">📁</div>
          {files.length > 0 ? (
            <>
              <div className="dropzone-text" style={{ fontWeight: 600 }}>{files.length}개 파일 선택됨</div>
              <div className="dropzone-sub">클릭하거나 드래그하여 파일 추가</div>
            </>
          ) : (
            <>
              <div className="dropzone-text">파일을 여기에 드래그하거나 클릭하여 선택</div>
              <div className="dropzone-sub">최대 100MB · 여러 파일 동시 선택 가능</div>
            </>
          )}
        </div>

        {files.length > 0 && (
          <div style={{ marginTop: 12 }}>
            {files.map((f, i) => (
              <div key={i} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '8px 12px', borderBottom: '1px solid #f3f4f6', fontSize: 14
              }}>
                <span style={{ flex: 1 }}>{f.name}</span>
                <span style={{ color: '#6b7280', marginRight: 12 }}>{formatSize(f.size)}</span>
                <button onClick={(e) => { e.stopPropagation(); removeFile(i); }}
                  style={{ background: 'none', border: 'none', color: '#ef4444', cursor: 'pointer', fontSize: 16 }}>
                  &times;
                </button>
              </div>
            ))}
          </div>
        )}

        {uploading && (
          <div style={{ marginTop: 16 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 13, marginBottom: 4 }}>
              <span>업로드 중...</span>
              <span>{progress}%</span>
            </div>
            <div className="progress-bar">
              <div className="progress-fill" style={{ width: `${progress}%` }} />
            </div>
          </div>
        )}
      </div>

      {/* 수신자 */}
      <div className="card">
        <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>수신자 지정</h3>
        <div style={{ position: 'relative' }}>
          <input
            className="form-control"
            placeholder="이름, 사번, 부서로 검색..."
            value={searchKeyword}
            onChange={(e) => handleSearch(e.target.value)}
          />
          {searchResults.length > 0 && (
            <div style={{
              position: 'absolute', top: '100%', left: 0, right: 0,
              background: '#fff', border: '1px solid #d1d5db', borderRadius: 8,
              boxShadow: '0 4px 12px rgba(0,0,0,0.1)', zIndex: 10, maxHeight: 200, overflow: 'auto'
            }}>
              {searchResults.map((u) => (
                <div key={u.id} onClick={() => addRecipient(u)} style={{
                  padding: '10px 14px', cursor: 'pointer', borderBottom: '1px solid #f3f4f6',
                  fontSize: 14
                }}>
                  <strong>{u.name}</strong>
                  <span style={{ color: '#6b7280', marginLeft: 8 }}>{u.department} · {u.employeeId}</span>
                </div>
              ))}
            </div>
          )}
        </div>
        {recipients.length > 0 && (
          <div className="recipient-tags">
            {recipients.map((r) => (
              <span key={r.id} className="recipient-tag">
                {r.name} ({r.department})
                <button onClick={() => removeRecipient(r.id)}>&times;</button>
              </span>
            ))}
          </div>
        )}
      </div>

      {/* 메모 */}
      <div className="card">
        <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 12 }}>메모 (선택)</h3>
        <textarea
          className="form-control"
          rows={3}
          placeholder="파일에 대한 메모를 작성하세요..."
          value={memo}
          onChange={(e) => setMemo(e.target.value)}
        />
      </div>

      {/* 업로드 버튼 */}
      <button className="btn btn-primary" onClick={handleUpload} disabled={uploading}
        style={{ width: '100%', padding: 14, fontSize: 16 }}>
        {uploading ? `업로드 중... ${progress}%` : `파일 전송${files.length > 0 ? ` (${files.length}개)` : ''}`}
      </button>
    </div>
  );
}

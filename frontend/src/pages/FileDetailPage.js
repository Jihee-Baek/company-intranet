import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import toast from 'react-hot-toast';
import { getFileDetail, downloadFile, getDownloadLogs, previewFile } from '../api/files';
import { useAuth } from '../context/AuthContext';

function formatSize(bytes) {
  if (bytes < 1024) return bytes + ' B';
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + ' KB';
  return (bytes / (1024 * 1024)).toFixed(1) + ' MB';
}

function formatDateTime(str) {
  if (!str) return '-';
  const d = new Date(str);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`;
}

function formatExpiryDate(str) {
  if (!str) return '-';
  const d = new Date(str);
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')} 만료`;
}

function isPreviewable(fileType) {
  if (!fileType) return false;
  return fileType.startsWith('image/') || fileType === 'application/pdf';
}

export default function FileDetailPage() {
  const { id } = useParams();
  const { user } = useAuth();
  const [file, setFile] = useState(null);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(true);
  const [downloading, setDownloading] = useState(false);
  const [previewUrl, setPreviewUrl] = useState(null);

  useEffect(() => {
    Promise.all([
      getFileDetail(id),
      getDownloadLogs(id),
    ]).then(([fileRes, logRes]) => {
      const fileData = fileRes.data.data;
      setFile(fileData);
      setLogs(logRes.data.data);

      // 미리보기 가능한 파일이면 blob URL 생성
      if (isPreviewable(fileData.fileType)) {
        previewFile(id)
          .then((res) => {
            const url = window.URL.createObjectURL(new Blob([res.data], { type: fileData.fileType }));
            setPreviewUrl(url);
          })
          .catch(() => {});
      }
    }).catch(() => {
      toast.error('파일 정보를 불러올 수 없습니다');
    }).finally(() => setLoading(false));

    return () => {
      if (previewUrl) window.URL.revokeObjectURL(previewUrl);
    };
  }, [id]);

  const handleDownload = async () => {
    setDownloading(true);
    try {
      const res = await downloadFile(id);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', file.originalName);
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
      toast.success('다운로드 완료');

      // 새로고침하여 상태 반영
      const [fileRes, logRes] = await Promise.all([
        getFileDetail(id),
        getDownloadLogs(id),
      ]);
      setFile(fileRes.data.data);
      setLogs(logRes.data.data);
    } catch {
      toast.error('다운로드에 실패했습니다');
    } finally {
      setDownloading(false);
    }
  };

  if (loading) {
    return <div style={{ padding: 48, textAlign: 'center', color: '#9ca3af' }}>로딩 중...</div>;
  }

  if (!file) {
    return <div style={{ padding: 48, textAlign: 'center', color: '#9ca3af' }}>파일을 찾을 수 없습니다</div>;
  }

  const isUploader = file.uploader.id === user?.userId;

  return (
    <div>
      <h1 className="page-title">파일 상세</h1>

      {/* 파일 정보 */}
      <div className="card">
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16 }}>
          <div>
            <h2 style={{ fontSize: 20, fontWeight: 700, marginBottom: 16 }}>{file.originalName}</h2>
            <table style={{ fontSize: 14, lineHeight: 2 }}>
              <tbody>
                <tr><td style={{ color: '#6b7280', paddingRight: 24 }}>보낸 사람</td><td>{file.uploader.name} ({file.uploader.department})</td></tr>
                <tr><td style={{ color: '#6b7280', paddingRight: 24 }}>파일 크기</td><td>{formatSize(file.fileSize)}</td></tr>
                <tr><td style={{ color: '#6b7280', paddingRight: 24 }}>파일 형식</td><td>{file.fileType || '-'}</td></tr>
                <tr><td style={{ color: '#6b7280', paddingRight: 24 }}>업로드 시간</td><td>{formatDateTime(file.createdAt)}</td></tr>
                {file.expiresAt && <tr><td style={{ color: '#6b7280', paddingRight: 24 }}>만료일</td><td style={{ color: '#ef4444' }}>{formatExpiryDate(file.expiresAt)}</td></tr>}
                {file.memo && <tr><td style={{ color: '#6b7280', paddingRight: 24 }}>메모</td><td>{file.memo}</td></tr>}
              </tbody>
            </table>
          </div>
          <button className="btn btn-primary" onClick={handleDownload} disabled={downloading}
            style={{ fontSize: 16, padding: '12px 32px' }}>
            {downloading ? '다운로드 중...' : '다운로드'}
          </button>
        </div>
      </div>

      {/* 인라인 미리보기 */}
      {previewUrl && (
        <div className="card">
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>미리보기</h3>
          {file.fileType.startsWith('image/') ? (
            <img src={previewUrl} alt={file.originalName}
              style={{ maxWidth: '100%', maxHeight: 500, borderRadius: 8 }} />
          ) : file.fileType === 'application/pdf' ? (
            <iframe src={previewUrl} title={file.originalName}
              style={{ width: '100%', height: 600, border: 'none', borderRadius: 8 }} />
          ) : null}
        </div>
      )}

      {/* 수신자 상태 */}
      <div className="card">
        <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>수신자 현황</h3>
        <table className="log-table">
          <thead>
            <tr>
              <th>이름</th>
              <th>부서</th>
              <th>상태</th>
              <th>확인 시간</th>
            </tr>
          </thead>
          <tbody>
            {file.recipients.map((r) => (
              <tr key={r.id}>
                <td><strong>{r.name}</strong> <span style={{ color: '#9ca3af' }}>({r.employeeId})</span></td>
                <td>{r.department}</td>
                <td>
                  <span className={`download-status ${r.isDownloaded ? 'read' : 'unread'}`}>
                    {r.isDownloaded ? '확인' : '미확인'}
                  </span>
                </td>
                <td>{formatDateTime(r.firstDownloadedAt)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* 다운로드 로그 */}
      <div className="card">
        <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>다운로드 이력</h3>
        {logs.length === 0 ? (
          <div style={{ textAlign: 'center', color: '#9ca3af', padding: 24 }}>다운로드 이력이 없습니다</div>
        ) : (
          <table className="log-table">
            <thead>
              <tr>
                <th>다운로드한 사람</th>
                <th>부서</th>
                <th>시간</th>
                <th>IP</th>
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => (
                <tr key={log.id}>
                  <td><strong>{log.downloaderName}</strong> <span style={{ color: '#9ca3af' }}>({log.downloaderEmployeeId})</span></td>
                  <td>{log.department}</td>
                  <td>{formatDateTime(log.downloadedAt)}</td>
                  <td style={{ fontFamily: 'monospace', fontSize: 13 }}>{log.ipAddress}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
}

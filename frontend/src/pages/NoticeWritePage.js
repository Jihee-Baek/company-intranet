import React, { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { getNotice, createNotice, updateNotice } from '../api/notices';

export default function NoticeWritePage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEdit = !!id;

  const [title, setTitle] = useState('');
  const [content, setContent] = useState('');
  const [isPinned, setIsPinned] = useState(false);
  const [submitting, setSubmitting] = useState(false);

  useEffect(() => {
    if (!isEdit) return;
    getNotice(id)
      .then((res) => {
        const n = res.data.data;
        setTitle(n.title);
        setContent(n.content);
        setIsPinned(n.pinned);
      })
      .catch(() => {
        toast.error('공지사항을 불러올 수 없습니다');
        navigate('/notices');
      });
  }, [id, isEdit, navigate]);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (!title.trim()) { toast.error('제목을 입력해 주세요'); return; }
    if (!content.trim()) { toast.error('내용을 입력해 주세요'); return; }

    setSubmitting(true);
    const data = { title: title.trim(), content: content.trim(), pinned: isPinned };
    const request = isEdit ? updateNotice(id, data) : createNotice(data);

    request
      .then((res) => {
        toast.success(isEdit ? '공지사항이 수정되었습니다' : '공지사항이 등록되었습니다');
        navigate(`/notices/${res.data.data.id}`);
      })
      .catch(() => toast.error(isEdit ? '수정에 실패했습니다' : '등록에 실패했습니다'))
      .finally(() => setSubmitting(false));
  };

  return (
    <div>
      <h1 className="page-title">{isEdit ? '공지사항 수정' : '공지사항 작성'}</h1>

      <div className="card">
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label className="form-label">제목</label>
            <input
              className="form-control"
              placeholder="제목을 입력하세요"
              value={title}
              onChange={(e) => setTitle(e.target.value)}
              maxLength={200}
            />
          </div>

          <div className="form-group">
            <label className="form-label">내용</label>
            <textarea
              className="form-control"
              placeholder="내용을 입력하세요"
              value={content}
              onChange={(e) => setContent(e.target.value)}
              rows={12}
              style={{ resize: 'vertical' }}
            />
          </div>

          <div className="form-group" style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <input
              type="checkbox"
              id="isPinned"
              checked={isPinned}
              onChange={(e) => setIsPinned(e.target.checked)}
              style={{ width: 16, height: 16, cursor: 'pointer' }}
            />
            <label htmlFor="isPinned" style={{ cursor: 'pointer', margin: 0 }}>
              📌 상단에 고정
            </label>
          </div>

          <div style={{ display: 'flex', gap: 8, justifyContent: 'flex-end' }}>
            <button
              type="button"
              className="btn"
              onClick={() => navigate(isEdit ? `/notices/${id}` : '/notices')}
              disabled={submitting}
            >
              취소
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? '저장 중...' : isEdit ? '수정' : '등록'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

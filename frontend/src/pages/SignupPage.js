import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import toast from 'react-hot-toast';
import { signup } from '../api/auth';
import { getDepartments } from '../api/departments';

export default function SignupPage() {
  const [form, setForm] = useState({
    employeeId: '', name: '', departmentId: '', email: '', password: '', passwordConfirm: ''
  });
  const [departments, setDepartments] = useState([]);
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  useEffect(() => {
    getDepartments()
      .then(res => setDepartments(res.data))
      .catch(() => toast.error('부서 목록을 불러오지 못했습니다'));
  }, []);

  const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.password !== form.passwordConfirm) {
      toast.error('비밀번호가 일치하지 않습니다');
      return;
    }
    setLoading(true);
    try {
      const { passwordConfirm, departmentId, ...rest } = form;
      const data = { ...rest, departmentId: departmentId ? Number(departmentId) : null };
      await signup(data);
      toast.success('회원가입이 완료되었습니다. 로그인해주세요.');
      navigate('/login');
    } catch (err) {
      toast.error(err.response?.data?.message || '회원가입에 실패했습니다');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <h1 className="auth-title">회원가입</h1>
        <p className="auth-subtitle">FileHub 계정을 생성합니다</p>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>사번 *</label>
            <input className="form-control" name="employeeId" value={form.employeeId}
              onChange={handleChange} placeholder="예: EMP001" required />
          </div>
          <div className="form-group">
            <label>이름 *</label>
            <input className="form-control" name="name" value={form.name}
              onChange={handleChange} placeholder="홍길동" required />
          </div>
          <div className="form-group">
            <label>부서</label>
            <select className="form-control" name="departmentId" value={form.departmentId}
              onChange={handleChange}>
              <option value="">선택하세요</option>
              {departments.map(dept => (
                <option key={dept.id} value={dept.id}>{dept.name}</option>
              ))}
            </select>
          </div>
          <div className="form-group">
            <label>이메일</label>
            <input className="form-control" type="email" name="email" value={form.email}
              onChange={handleChange} placeholder="hong@company.com" />
          </div>
          <div className="form-group">
            <label>비밀번호 *</label>
            <input className="form-control" type="password" name="password" value={form.password}
              onChange={handleChange} placeholder="4자 이상" required minLength={4} />
          </div>
          <div className="form-group">
            <label>비밀번호 확인 *</label>
            <input className="form-control" type="password" name="passwordConfirm"
              value={form.passwordConfirm} onChange={handleChange}
              placeholder="비밀번호를 다시 입력하세요" required />
          </div>
          <button type="submit" className="btn btn-primary"
            style={{ width: '100%', marginTop: 8 }} disabled={loading}>
            {loading ? '가입 중...' : '회원가입'}
          </button>
        </form>
        <div className="auth-link">
          이미 계정이 있으신가요? <Link to="/login">로그인</Link>
        </div>
      </div>
    </div>
  );
}

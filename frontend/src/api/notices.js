import client from './client';

export const getNotices = (page = 0, size = 10) =>
  client.get('/notices', { params: { page, size } });

export const getNotice = (id) => client.get(`/notices/${id}`);

export const createNotice = (data) => client.post('/notices', data);

export const updateNotice = (id, data) => client.put(`/notices/${id}`, data);

export const deleteNotice = (id) => client.delete(`/notices/${id}`);

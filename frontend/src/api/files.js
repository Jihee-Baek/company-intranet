import client from './client';

export const uploadFile = (formData, onProgress) =>
  client.post('/files/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress: (e) => {
      if (onProgress && e.total) {
        onProgress(Math.round((e.loaded * 100) / e.total));
      }
    },
  });

export const getReceivedFiles = (page = 0, size = 20, keyword, sortBy, sortDir) =>
  client.get('/files/received', { params: { page, size, keyword, sortBy, sortDir } });

export const getSentFiles = (page = 0, size = 20, keyword, sortBy, sortDir) =>
  client.get('/files/sent', { params: { page, size, keyword, sortBy, sortDir } });

export const getFileDetail = (id) => client.get(`/files/${id}`);

export const downloadFile = (id) =>
  client.get(`/files/${id}/download`, { responseType: 'blob' });

export const previewFile = (id) =>
  client.get(`/files/${id}/preview`, { responseType: 'blob' });

export const getDownloadLogs = (id) => client.get(`/files/${id}/logs`);

export const getUnreadCount = () => client.get('/files/unread-count');

import client from './client';

export const searchUsers = (keyword) =>
  client.get('/users/search', { params: { keyword } });

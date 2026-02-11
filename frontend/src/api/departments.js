import client from './client';

export const getDepartments = () => client.get('/departments');

export const saveAuth = (username, password) => {
    const authString = btoa(`${username}:${password}`);
    localStorage.setItem('auth', authString);
};

export const getAuthHeader = () => {
    const auth = localStorage.getItem('auth');
    return auth ? { 'Authorization': `Basic ${auth}` } : {};
};

export const logout = () => {
    localStorage.removeItem('auth');
};

export const isAuthenticated = () => {
    return localStorage.getItem('auth') !== null;
};
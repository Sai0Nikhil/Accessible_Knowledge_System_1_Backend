import jwt from 'jsonwebtoken';

const SECRET_KEY = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM0987654321";

export function authenticateToken(req, res, next) {
  const token = req.headers['token'] || req.headers['Token'];

  if (!token) {
    return res.status(401).json({ code: 401, message: 'Authentication token is missing' });
  }

  try {
    const decoded = jwt.verify(token, SECRET_KEY);
    let roleVal = decoded.role;
    if (roleVal === 1 || roleVal === '1') roleVal = 'Student';
    else if (roleVal === 2 || roleVal === '2') roleVal = 'Librarian';
    else if (roleVal === 3 || roleVal === '3') roleVal = 'Admin';

    req.user = {
      email: decoded.username, // Spring Boot sets 'username' as the email
      role: roleVal
    };
    next();
  } catch (err) {
    return res.status(403).json({ code: 403, message: 'Token is invalid or expired' });
  }
}

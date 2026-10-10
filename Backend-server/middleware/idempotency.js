const db = require('../config/db');

module.exports = async (req, res, next) => {
  const operationId = req.headers['x-operation-id'];

  if (!operationId) {
    return next();
  }

  try {
    // Ensure the operation_receipts table exists dynamically to prevent errors
    await db.execute(`
      CREATE TABLE IF NOT EXISTS operation_receipts (
        id INT AUTO_INCREMENT PRIMARY KEY,
        operation_id VARCHAR(255) UNIQUE NOT NULL,
        endpoint VARCHAR(255) NOT NULL,
        response_body TEXT NOT NULL,
        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
      )
    `);

    const [existing] = await db.execute(
      'SELECT response_body FROM operation_receipts WHERE operation_id = ?',
      [operationId]
    );

    if (existing.length > 0) {
      return res.status(200).json(JSON.parse(existing[0].response_body));
    }

    const originalJson = res.json;
    res.json = function (body) {
      if (res.statusCode >= 200 && res.statusCode < 300) {
        db.execute(
          'INSERT INTO operation_receipts (operation_id, endpoint, response_body) VALUES (?, ?, ?)',
          [operationId, req.originalUrl, JSON.stringify(body)]
        ).catch(err => console.error('Failed to save receipt:', err));
      }
      return originalJson.call(this, body);
    };

    next();
  } catch (error) {
    console.error('Idempotency error:', error);
    next();
  }
};
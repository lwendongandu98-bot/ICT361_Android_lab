const db = require('../config/db');

module.exports = async (req, res, next) => {
  const operationId = req.headers['x-operation-id'];

  if (!operationId) {
    return next();
  }

  try {
    const [existing] = await db.execute(
      'SELECT response_body FROM operation_receipts WHERE operation_id = ?',
      [operationId]
    );

    if (existing.length > 0) {
      return res.status(200).json(existing[0].response_body);
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
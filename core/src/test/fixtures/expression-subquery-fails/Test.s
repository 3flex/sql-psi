CREATE TABLE test (
  _id	INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
  someString	TEXT
);

DELETE FROM test2 WHERE (SELECT someString, _id FROM test WHERE test._id = test2.testId) = ?;

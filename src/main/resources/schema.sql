CREATE TABLE IF NOT EXISTS t_user (
    id VARCHAR(36) PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100),
    age INT,
    phone VARCHAR(20),
    address VARCHAR(200)
);

CREATE INDEX idx_user_username ON t_user(username);
CREATE INDEX idx_user_age ON t_user(age);

CREATE TABLE IF NOT EXISTS t_order (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    order_no VARCHAR(50) NOT NULL,
    amount DECIMAL(10, 2),
    status INT,
    remark VARCHAR(500)
);

CREATE INDEX idx_order_user_id ON t_order(user_id);
CREATE INDEX idx_order_no ON t_order(order_no);
CREATE INDEX idx_order_status ON t_order(status);




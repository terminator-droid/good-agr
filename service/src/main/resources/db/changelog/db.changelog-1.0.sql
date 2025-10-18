

CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );


CREATE TABLE IF NOT EXISTS products (
                                        id BIGSERIAL PRIMARY KEY,
                                        title VARCHAR(500) NOT NULL,
    old_price VARCHAR(100),           -- Строковые цены из парсеров
    new_price VARCHAR(100),           -- Строковые цены из парсеров
    old_price_decimal DECIMAL(10,2),  -- Преобразованные цены для расчетов
    new_price_decimal DECIMAL(10,2),  -- Преобразованные цены для расчетов
    volume VARCHAR(200),              -- Объем товара
    ref VARCHAR(800) UNIQUE NOT NULL, -- URL ссылка (уникальный идентификатор)
    shop VARCHAR(50) NOT NULL,        -- SAMOKAT или LAVKA
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );



CREATE TABLE IF NOT EXISTS carts (
                                     id BIGSERIAL PRIMARY KEY,
                                     user_id BIGINT NOT NULL,
                                     created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                     FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
    );


CREATE TABLE IF NOT EXISTS cart_items (
                                          id BIGSERIAL PRIMARY KEY,
                                          cart_id BIGINT NOT NULL,
                                          product_id BIGINT NOT NULL,
                                          quantity INTEGER NOT NULL CHECK (quantity > 0),
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    UNIQUE (cart_id, product_id)
    );



CREATE INDEX idx_products_shop ON products(shop);
CREATE INDEX idx_products_title ON products(title);
CREATE INDEX idx_products_ref ON products(ref);
CREATE INDEX idx_products_updated_at ON products(updated_at);
CREATE INDEX idx_cart_items_cart_id ON cart_items(cart_id);
CREATE INDEX idx_carts_user_id ON carts(user_id);


CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';



CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();



INSERT INTO users (username, email, password) VALUES
    ('admin', 'admin@example.com', '$2a$10$9XnhFQwWN9oS0SsrNGvHGeF8RuvlNMmvhDt9jVN8WBwMqNlEQnmKe')  -- password: admin
    ON CONFLICT (username) DO NOTHING;



COMMENT ON TABLE products IS 'Товары, получаемые через парсинг Самокат и Лавка';
COMMENT ON COLUMN products.ref IS 'URL ссылка на товар - уникальный идентификатор из парсеров';
COMMENT ON COLUMN products.old_price IS 'Старая цена как строка из парсера';
COMMENT ON COLUMN products.new_price IS 'Новая цена как строка из парсера';
COMMENT ON COLUMN products.old_price_decimal IS 'Преобразованная старая цена для расчетов';
COMMENT ON COLUMN products.new_price_decimal IS 'Преобразованная новая цена для расчетов';

-- Add nullable name column to cart_item to align with BaseModel.name
ALTER TABLE cart_item ADD COLUMN name VARCHAR(255);


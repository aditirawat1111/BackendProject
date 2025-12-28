-- Add nullable name column to cart to align with BaseModel.name
ALTER TABLE cart ADD COLUMN name VARCHAR(255);


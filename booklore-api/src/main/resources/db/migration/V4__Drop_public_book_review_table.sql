ALTER TABLE IF EXISTS public_book_review DROP CONSTRAINT IF EXISTS FKq6akumxvld3gfxyxlilii6hno;
DROP TABLE IF EXISTS public_book_review;

ALTER TABLE book_metadata DROP COLUMN IF EXISTS reviews_locked;

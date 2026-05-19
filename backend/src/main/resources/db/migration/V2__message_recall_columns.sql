ALTER TABLE private_message
  ADD COLUMN recalled_at DATETIME DEFAULT NULL;

ALTER TABLE public_message
  ADD COLUMN recalled_at DATETIME DEFAULT NULL;

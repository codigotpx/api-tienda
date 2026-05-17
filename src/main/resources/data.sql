SET client_encoding = 'UTF8';

-- =============================================
-- 0. USUARIO ADMIN (password: Admin1234!)
-- =============================================
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username = 'admin@tienda.com');
DELETE FROM users WHERE username = 'admin@tienda.com';

INSERT INTO users (id, username, password, enabled, account_non_locked)
VALUES (
           'aaaaaaaa-0000-0000-0000-000000000001'::uuid,
           'admin@tienda.com',
           '$2b$10$QoQiv04y8Th91/Fp8/i1xOu8gQ61mbaRtFIqYQN5Kf8wrMMQPQNzq',
           true,
           true
       );

INSERT INTO user_roles (user_id, role)
VALUES ('aaaaaaaa-0000-0000-0000-000000000001'::uuid, 'ROLE_ADMIN');

-- =============================================
-- 1. CATEGORIAS (Ropa y Libros)
-- =============================================
INSERT INTO categories (id, name, description) VALUES
                                                   ('11111111-0000-0000-0000-000000000001'::uuid, 'Ropa', 'Incluye camisetas, jeans y zapatos'),
                                                   ('11111111-0000-0000-0000-000000000002'::uuid, 'Libros', 'Libros de programación y literatura')
    ON CONFLICT (name) DO NOTHING;

-- =============================================
-- 2. PRODUCTOS (Camisetas, Jeans, Zapatos, Libros)
-- =============================================
INSERT INTO products (id, sku, name, description, price, active, category_id, image_url) VALUES
                                                                                  -- Ropa
                                                                                  ('22222222-0000-0000-0000-000000000001'::uuid, 'SKU-ROPA-001', 'Camiseta Algodón Premium', 'Camiseta 100% algodón, azul', 29.99, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/camiseta-algodon-premium.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000007'::uuid, 'SKU-ROPA-005', 'Camiseta Polo Clásica', 'Camiseta tipo polo con cuello, 100% algodón', 35.00, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/camiseta-polo-clasica.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000008'::uuid, 'SKU-ROPA-006', 'Camiseta Oversize', 'Corte ancho de tendencia, color gris jaspe', 32.50, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/camiseta-oversize.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000002'::uuid, 'SKU-ROPA-002', 'Jeans Slim Fit', 'Pantalón vaquero corte moderno', 69.99, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/jeans-slim-fit.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000009'::uuid, 'SKU-ROPA-007', 'Jean Negro Regular', 'Jean color negro sólido, corte recto tradicional', 75.00, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/jean-negro-regular.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000010'::uuid, 'SKU-ROPA-008', 'Jean Azul Ripped', 'Jean azul con detalles desgastados y rotos', 79.90, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/jean-azul-ripped.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000003'::uuid, 'SKU-ROPA-003', 'Zapatos Casuales', 'Calzado de cuero sintético cómodo', 89.90, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/zapatos-casuales.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000004'::uuid, 'SKU-ROPA-004', 'Zapatos Deportivos', 'Zapatillas para correr con amortiguación', 110.00, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/zapatos-deportivos.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000011'::uuid, 'SKU-ROPA-009', 'Zapato de Vestir Oxford', 'Calzado formal de cuero negro con cordones', 120.00, true, '11111111-0000-0000-0000-000000000001'::uuid, 'http://localhost:8080/images/zapato-vestir-oxford.jpg'),
                                                                                  -- Libros
                                                                                  ('22222222-0000-0000-0000-000000000005'::uuid, 'SKU-LIB-001', 'Clean Code', 'Robert C. Martin - Manual de agilidad de software', 45.00, true, '11111111-0000-0000-0000-000000000002'::uuid, 'http://localhost:8080/images/clean-code.jpg'),
                                                                                  ('22222222-0000-0000-0000-000000000006'::uuid, 'SKU-LIB-002', 'The Pragmatic Programmer', 'Andrew Hunt - Tu camino a la maestría', 48.50, true, '11111111-0000-0000-0000-000000000002'::uuid, 'http://localhost:8080/images/the-pragmatic-programmer.jpg')
    ON CONFLICT (sku) DO NOTHING;

-- =============================================
-- 3. CLIENTES
-- =============================================
INSERT INTO customers (id, first_name, last_name, phone, email, status) VALUES
                                                                            ('33333333-0000-0000-0000-000000000001'::uuid, 'Carlos', 'Martinez', '+573001112233', 'carlos.martinez@email.com', 'ACTIVE'),
                                                                            ('33333333-0000-0000-0000-000000000002'::uuid, 'Ana', 'Lopez', '+573104445566', 'ana.lopez@email.com', 'ACTIVE')
    ON CONFLICT (email) DO NOTHING;

-- =============================================
-- 4. DIRECCIONES
-- =============================================
INSERT INTO addresses (id, street, city, state, zip, country, customer_id) VALUES
                                                                               ('44444444-0000-0000-0000-000000000001'::uuid, 'Cra 5 10-20', 'Valledupar', 'Cesar', '200001', 'Colombia', '33333333-0000-0000-0000-000000000001'::uuid),
                                                                               ('44444444-0000-0000-0000-000000000002'::uuid, 'Calle 80 55-30', 'Bogota', 'Cundinamarca', '110111', 'Colombia', '33333333-0000-0000-0000-000000000002'::uuid)
    ON CONFLICT DO NOTHING;

-- =============================================
-- 5. INVENTARIOS
-- =============================================
INSERT INTO inventories (id, available_stock, minimum_stock, product_id) VALUES
                                                                             ('55555555-0000-0000-0000-000000000001'::uuid, 100, 10, '22222222-0000-0000-0000-000000000001'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000002'::uuid, 50, 5, '22222222-0000-0000-0000-000000000002'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000003'::uuid, 40, 5, '22222222-0000-0000-0000-000000000003'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000004'::uuid, 30, 5, '22222222-0000-0000-0000-000000000004'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000005'::uuid, 25, 3, '22222222-0000-0000-0000-000000000005'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000006'::uuid, 20, 3, '22222222-0000-0000-0000-000000000006'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000007'::uuid, 60, 5, '22222222-0000-0000-0000-000000000007'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000008'::uuid, 45, 5, '22222222-0000-0000-0000-000000000008'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000009'::uuid, 30, 3, '22222222-0000-0000-0000-000000000009'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000010'::uuid, 25, 3, '22222222-0000-0000-0000-000000000010'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000011'::uuid, 15, 2, '22222222-0000-0000-0000-000000000011'::uuid)
    ON CONFLICT DO NOTHING;

-- =============================================
-- 6. ORDENES
-- =============================================
INSERT INTO orders (id, total, status, created_at, updated_at, customer_id, address_id) VALUES
    ('66666666-0000-0000-0000-000000000001'::uuid, 99.98, 'PAID', NOW() - INTERVAL '2 days', NOW(), '33333333-0000-0000-0000-000000000001'::uuid, '44444444-0000-0000-0000-000000000001'::uuid)
    ON CONFLICT DO NOTHING;

-- =============================================
-- 7. ITEMS DE ORDEN
-- =============================================
INSERT INTO order_items (id, quantity, unit_price, subtotal, order_id, product_id) VALUES
                                                                                       ('77777777-0000-0000-0000-000000000001'::uuid, 1, 29.99, 29.99, '66666666-0000-0000-0000-000000000001'::uuid, '22222222-0000-0000-0000-000000000001'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000002'::uuid, 1, 69.99, 69.99, '66666666-0000-0000-0000-000000000001'::uuid, '22222222-0000-0000-0000-000000000002'::uuid)
    ON CONFLICT DO NOTHING;
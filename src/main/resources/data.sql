SET client_encoding = 'UTF8';

-- =============================================
-- 0. USUARIO ADMIN  (password: Admin1234!)
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
-- 1. CATEGORIAS  (tabla real: categories)
-- =============================================
INSERT INTO categories (id, name, description) VALUES
                                                   ('11111111-0000-0000-0000-000000000001'::uuid, 'Electronica',  'Dispositivos electronicos y accesorios'),
                                                   ('11111111-0000-0000-0000-000000000002'::uuid, 'Ropa',         'Prendas de vestir para hombre y mujer'),
                                                   ('11111111-0000-0000-0000-000000000003'::uuid, 'Hogar',        'Articulos para el hogar y decoracion'),
                                                   ('11111111-0000-0000-0000-000000000004'::uuid, 'Deportes',     'Equipos y accesorios deportivos'),
                                                   ('11111111-0000-0000-0000-000000000005'::uuid, 'Alimentacion', 'Productos alimenticios y bebidas'),
                                                   ('11111111-0000-0000-0000-000000000006'::uuid, 'Libros',       'Libros, cuadernos y material de oficina')
ON CONFLICT (name) DO NOTHING;

-- =============================================
-- 2. PRODUCTOS  (tabla real: products)
-- =============================================
INSERT INTO products (id, sku, name, description, price, active, category_id) VALUES
                                                                                  ('22222222-0000-0000-0000-000000000001'::uuid, 'SKU-ELEC-001',  'Smartphone Galaxy X12',   'Telefono 5G 128 GB pantalla AMOLED 6.5',  1299.99, true, '11111111-0000-0000-0000-000000000001'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000002'::uuid, 'SKU-ELEC-002',  'Laptop UltraBook Pro 14', 'Procesador i7 16 GB RAM SSD 512 GB',       2499.00, true, '11111111-0000-0000-0000-000000000001'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000003'::uuid, 'SKU-ELEC-003',  'Auriculares BT X3',       'Cancelacion activa de ruido 30 h bateria',  189.99, true, '11111111-0000-0000-0000-000000000001'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000004'::uuid, 'SKU-ELEC-004',  'Smartwatch FitBand 5',    'Monitor cardiaco GPS resistente al agua',   249.50, true, '11111111-0000-0000-0000-000000000001'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000005'::uuid, 'SKU-ELEC-005',  'Tablet Vision 10',        'Pantalla FHD 64 GB Android 14',             399.00, true, '11111111-0000-0000-0000-000000000001'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000006'::uuid, 'SKU-ROPA-001',  'Camiseta Algodon',        'Talla M varios colores',                     29.99, true, '11111111-0000-0000-0000-000000000002'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000007'::uuid, 'SKU-ROPA-002',  'Jeans Slim Fit',          'Talla 32x32 corte moderno',                  69.99, true, '11111111-0000-0000-0000-000000000002'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000008'::uuid, 'SKU-ROPA-003',  'Chaqueta Impermeable',    'Talla L ideal para lluvia',                  89.00, true, '11111111-0000-0000-0000-000000000002'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000009'::uuid, 'SKU-HOGAR-001', 'Cafetera Espresso 1200W', '15 bares de presion deposito 1.2 L',        159.00, true, '11111111-0000-0000-0000-000000000003'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000010'::uuid, 'SKU-HOGAR-002', 'Set Sartenes Antiadh',    'Juego de 3 piezas libre de PFOA',            79.99, true, '11111111-0000-0000-0000-000000000003'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000011'::uuid, 'SKU-DEP-001',   'Bicicleta Montana 29',    '21 velocidades frenos de disco',            549.00, true, '11111111-0000-0000-0000-000000000004'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000012'::uuid, 'SKU-DEP-002',   'Zapatillas Running Air',  'Plantilla amortiguadora talla 42',           99.00, true, '11111111-0000-0000-0000-000000000004'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000013'::uuid, 'SKU-ALI-001',   'Pack Cafe Premium 1 kg',  '100 arabica tueste medio',                   22.50, true, '11111111-0000-0000-0000-000000000005'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000014'::uuid, 'SKU-ALI-002',   'Aceite de Oliva 750 ml',  'Acidez 0.2 grados origen Colombia',          18.00, true, '11111111-0000-0000-0000-000000000005'::uuid),
                                                                                  ('22222222-0000-0000-0000-000000000015'::uuid, 'SKU-LIB-001',   'Clean Code',              'Autor Robert C. Martin ingles',              45.00, true, '11111111-0000-0000-0000-000000000006'::uuid)
ON CONFLICT (sku) DO NOTHING;

-- =============================================
-- 3. CLIENTES  (tabla real: customers)
-- =============================================
INSERT INTO customers (id, first_name, last_name, phone, email, status) VALUES
                                                                            ('33333333-0000-0000-0000-000000000001'::uuid, 'Carlos',    'Martinez',  '+573001112233', 'carlos.martinez@email.com',   'ACTIVE'),
                                                                            ('33333333-0000-0000-0000-000000000002'::uuid, 'Ana',       'Lopez',     '+573104445566', 'ana.lopez@email.com',         'ACTIVE'),
                                                                            ('33333333-0000-0000-0000-000000000003'::uuid, 'Pedro',     'Garcia',    '+573207778899', 'pedro.garcia@email.com',      'ACTIVE'),
                                                                            ('33333333-0000-0000-0000-000000000004'::uuid, 'Sofia',     'Rodriguez', '+573318889900', 'sofia.rodriguez@email.com',   'ACTIVE'),
                                                                            ('33333333-0000-0000-0000-000000000005'::uuid, 'Miguel',    'Torres',    '+573429990011', 'miguel.torres@email.com',     'INACTIVE'),
                                                                            ('33333333-0000-0000-0000-000000000006'::uuid, 'Valentina', 'Herrera',   '+573531110022', 'valentina.herrera@email.com', 'ACTIVE')
ON CONFLICT (email) DO NOTHING;

-- =============================================
-- 4. DIRECCIONES  (tabla real: addresses)
-- =============================================
INSERT INTO addresses (id, street, city, state, zip, country, customer_id) VALUES
                                                                               ('44444444-0000-0000-0000-000000000001'::uuid, 'Cra 5 10-20',       'Valledupar',   'Cesar',           '200001', 'Colombia', '33333333-0000-0000-0000-000000000001'::uuid),
                                                                               ('44444444-0000-0000-0000-000000000002'::uuid, 'Calle 80 55-30',    'Bogota',       'Cundinamarca',    '110111', 'Colombia', '33333333-0000-0000-0000-000000000002'::uuid),
                                                                               ('44444444-0000-0000-0000-000000000003'::uuid, 'Av El Poblado 1-5', 'Medellin',     'Antioquia',       '050021', 'Colombia', '33333333-0000-0000-0000-000000000003'::uuid),
                                                                               ('44444444-0000-0000-0000-000000000004'::uuid, 'Cra 50 8-15',       'Barranquilla', 'Atlantico',       '080002', 'Colombia', '33333333-0000-0000-0000-000000000004'::uuid),
                                                                               ('44444444-0000-0000-0000-000000000005'::uuid, 'Calle 15 3-40',     'Cali',         'Valle del Cauca', '760001', 'Colombia', '33333333-0000-0000-0000-000000000005'::uuid),
                                                                               ('44444444-0000-0000-0000-000000000006'::uuid, 'Cra 12 22-10',      'Bucaramanga',  'Santander',       '680001', 'Colombia', '33333333-0000-0000-0000-000000000006'::uuid),
                                                                               ('44444444-0000-0000-0000-000000000007'::uuid, 'Calle 7 1-50',      'Valledupar',   'Cesar',           '200001', 'Colombia', '33333333-0000-0000-0000-000000000001'::uuid)
ON CONFLICT DO NOTHING;

-- =============================================
-- 5. INVENTARIOS  (tabla real: inventories)
-- =============================================
INSERT INTO inventories (id, available_stock, minimum_stock, product_id) VALUES
                                                                             ('55555555-0000-0000-0000-000000000001'::uuid, 120, 10, '22222222-0000-0000-0000-000000000001'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000002'::uuid,  45,  5, '22222222-0000-0000-0000-000000000002'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000003'::uuid, 200, 20, '22222222-0000-0000-0000-000000000003'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000004'::uuid,  80,  8, '22222222-0000-0000-0000-000000000004'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000005'::uuid,  60,  6, '22222222-0000-0000-0000-000000000005'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000006'::uuid, 350, 30, '22222222-0000-0000-0000-000000000006'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000007'::uuid, 180, 15, '22222222-0000-0000-0000-000000000007'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000008'::uuid,  90, 10, '22222222-0000-0000-0000-000000000008'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000009'::uuid,  75,  5, '22222222-0000-0000-0000-000000000009'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000010'::uuid, 110, 10, '22222222-0000-0000-0000-000000000010'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000011'::uuid,  30,  3, '22222222-0000-0000-0000-000000000011'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000012'::uuid, 250, 25, '22222222-0000-0000-0000-000000000012'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000013'::uuid, 400, 40, '22222222-0000-0000-0000-000000000013'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000014'::uuid, 300, 30, '22222222-0000-0000-0000-000000000014'::uuid),
                                                                             ('55555555-0000-0000-0000-000000000015'::uuid,  50,  5, '22222222-0000-0000-0000-000000000015'::uuid)
ON CONFLICT DO NOTHING;

-- =============================================
-- 6. ORDENES  (tabla real: orders)
-- =============================================
INSERT INTO orders (id, total, status, created_at, updated_at, customer_id, address_id) VALUES
                                                                                            ('66666666-0000-0000-0000-000000000001'::uuid, 1489.98, 'PAID',      NOW() - INTERVAL '10 days', NOW() - INTERVAL  '9 days', '33333333-0000-0000-0000-000000000001'::uuid, '44444444-0000-0000-0000-000000000001'::uuid),
                                                                                            ('66666666-0000-0000-0000-000000000002'::uuid, 2499.00, 'SHIPPED',   NOW() - INTERVAL  '7 days', NOW() - INTERVAL  '5 days', '33333333-0000-0000-0000-000000000002'::uuid, '44444444-0000-0000-0000-000000000002'::uuid),
                                                                                            ('66666666-0000-0000-0000-000000000003'::uuid,  159.00, 'DELIVERED', NOW() - INTERVAL '20 days', NOW() - INTERVAL '18 days', '33333333-0000-0000-0000-000000000003'::uuid, '44444444-0000-0000-0000-000000000003'::uuid),
                                                                                            ('66666666-0000-0000-0000-000000000004'::uuid,  549.00, 'CREATED',   NOW() - INTERVAL  '1 day',  NOW() - INTERVAL  '1 day',  '33333333-0000-0000-0000-000000000004'::uuid, '44444444-0000-0000-0000-000000000004'::uuid),
                                                                                            ('66666666-0000-0000-0000-000000000005'::uuid,  189.99, 'CANCELLED', NOW() - INTERVAL '15 days', NOW() - INTERVAL '14 days', '33333333-0000-0000-0000-000000000006'::uuid, '44444444-0000-0000-0000-000000000006'::uuid),
                                                                                            ('66666666-0000-0000-0000-000000000006'::uuid,  267.99, 'PAID',      NOW() - INTERVAL  '3 days', NOW() - INTERVAL  '2 days', '33333333-0000-0000-0000-000000000001'::uuid, '44444444-0000-0000-0000-000000000007'::uuid)
ON CONFLICT DO NOTHING;

-- =============================================
-- 7. ITEMS DE ORDEN  (tabla real: order_items)
-- =============================================
INSERT INTO order_items (id, quantity, unit_price, subtotal, order_id, product_id) VALUES
                                                                                       ('77777777-0000-0000-0000-000000000001'::uuid, 1, 1299.99, 1299.99, '66666666-0000-0000-0000-000000000001'::uuid, '22222222-0000-0000-0000-000000000001'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000002'::uuid, 1,  189.99,  189.99, '66666666-0000-0000-0000-000000000001'::uuid, '22222222-0000-0000-0000-000000000003'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000003'::uuid, 1, 2499.00, 2499.00, '66666666-0000-0000-0000-000000000002'::uuid, '22222222-0000-0000-0000-000000000002'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000004'::uuid, 1,  159.00,  159.00, '66666666-0000-0000-0000-000000000003'::uuid, '22222222-0000-0000-0000-000000000009'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000005'::uuid, 1,  549.00,  549.00, '66666666-0000-0000-0000-000000000004'::uuid, '22222222-0000-0000-0000-000000000011'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000006'::uuid, 1,  189.99,  189.99, '66666666-0000-0000-0000-000000000005'::uuid, '22222222-0000-0000-0000-000000000003'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000007'::uuid, 1,  249.50,  249.50, '66666666-0000-0000-0000-000000000006'::uuid, '22222222-0000-0000-0000-000000000004'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000008'::uuid, 1,   99.00,   99.00, '66666666-0000-0000-0000-000000000006'::uuid, '22222222-0000-0000-0000-000000000012'::uuid),
                                                                                       ('77777777-0000-0000-0000-000000000009'::uuid, 2,   29.99,   59.98, '66666666-0000-0000-0000-000000000006'::uuid, '22222222-0000-0000-0000-000000000006'::uuid)
ON CONFLICT DO NOTHING;

-- =============================================
-- 8. HISTORIAL  (tabla real: order_status_history)
-- =============================================
INSERT INTO order_status_history (id, notes, previous_status, new_status, changed_at, order_id) VALUES
                                                                                                    ('88888888-0000-0000-0000-000000000001'::uuid, 'Orden creada',              NULL,      'CREATED',   NOW() - INTERVAL '10 days', '66666666-0000-0000-0000-000000000001'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000002'::uuid, 'Pago confirmado',           'CREATED', 'PAID',      NOW() - INTERVAL  '9 days', '66666666-0000-0000-0000-000000000001'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000003'::uuid, 'Orden creada',              NULL,      'CREATED',   NOW() - INTERVAL  '7 days', '66666666-0000-0000-0000-000000000002'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000004'::uuid, 'Pago confirmado',           'CREATED', 'PAID',      NOW() - INTERVAL  '6 days', '66666666-0000-0000-0000-000000000002'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000005'::uuid, 'Enviado con transportista', 'PAID',    'SHIPPED',   NOW() - INTERVAL  '5 days', '66666666-0000-0000-0000-000000000002'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000006'::uuid, 'Orden creada',              NULL,      'CREATED',   NOW() - INTERVAL '20 days', '66666666-0000-0000-0000-000000000003'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000007'::uuid, 'Pago recibido',             'CREATED', 'PAID',      NOW() - INTERVAL '19 days', '66666666-0000-0000-0000-000000000003'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000008'::uuid, 'En camino',                 'PAID',    'SHIPPED',   NOW() - INTERVAL '19 days', '66666666-0000-0000-0000-000000000003'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000009'::uuid, 'Entregado al cliente',      'SHIPPED', 'DELIVERED', NOW() - INTERVAL '18 days', '66666666-0000-0000-0000-000000000003'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000010'::uuid, 'Orden creada',              NULL,      'CREATED',   NOW() - INTERVAL  '1 day',  '66666666-0000-0000-0000-000000000004'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000011'::uuid, 'Orden creada',              NULL,      'CREATED',   NOW() - INTERVAL '15 days', '66666666-0000-0000-0000-000000000005'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000012'::uuid, 'Cliente cancelo',           'CREATED', 'CANCELLED', NOW() - INTERVAL '14 days', '66666666-0000-0000-0000-000000000005'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000013'::uuid, 'Orden creada',              NULL,      'CREATED',   NOW() - INTERVAL  '3 days', '66666666-0000-0000-0000-000000000006'::uuid),
                                                                                                    ('88888888-0000-0000-0000-000000000014'::uuid, 'Pago confirmado',           'CREATED', 'PAID',      NOW() - INTERVAL  '2 days', '66666666-0000-0000-0000-000000000006'::uuid)
ON CONFLICT DO NOTHING;
INSERT INTO vendedores (id, nombre, tipo) VALUES
('v1', 'TecnoParts SAS', 'AUTORIZADO'),
('v2', 'Mario López', 'PARTICULAR'),
('v3', 'PCGlobal Ltda', 'AUTORIZADO');

INSERT INTO componentes (id, nombre, categoria, socket, wattaje_requerido, tipo_memoria, monto, moneda, vendedor_id, dias, estado_comercial) VALUES
('cpu-ryzen5', 'AMD Ryzen 5 5600', 'CPU', 'AM4', 65, NULL, 850000, 'COP', 'v1', 36, 'PUBLICADO'),
('mb-b550', 'ASUS B550M-A', 'MOTHERBOARD', 'AM4', 50, 'DDR4', 650000, 'COP', 'v1', 36, 'PUBLICADO'),
('ram-16gb', 'Corsair Vengeance 16GB', 'RAM', NULL, 10, 'DDR4', 320000, 'COP', 'v1', 36, 'PUBLICADO'),
('psu-650', 'EVGA 650W Bronze', 'PSU', NULL, 650, NULL, 420000, 'COP', 'v1', 36, 'PUBLICADO'),
('gpu-rtx4060', 'NVIDIA RTX 4060', 'GPU', NULL, 115, NULL, 1750000, 'COP', 'v2', NULL, 'PUBLICADO'),
('ssd-1tb', 'Samsung 980 1TB', 'STORAGE', NULL, 6, NULL, 450000, 'COP', 'v1', 36, 'PUBLICADO');

INSERT INTO unidades_inventario (id, numero_serie, estado, fecha_estimada_llegada, reserva_preventa_id, componente_id) VALUES
('550e8400-e29b-41d4-a716-446655440001', 'SN-CPU-0001', 'DISPONIBLE', NULL, NULL, 'cpu-ryzen5'),
('550e8400-e29b-41d4-a716-446655440002', 'SN-CPU-0002', 'DISPONIBLE', NULL, NULL, 'cpu-ryzen5'),
('550e8400-e29b-41d4-a716-446655440003', 'SN-MB-0001',  'DISPONIBLE', NULL, NULL, 'mb-b550'),
('550e8400-e29b-41d4-a716-446655440004', 'SN-RAM-0001', 'DISPONIBLE', NULL, NULL, 'ram-16gb'),
('550e8400-e29b-41d4-a716-446655440005', 'SN-PSU-0001', 'DISPONIBLE', NULL, NULL, 'psu-650'),
('550e8400-e29b-41d4-a716-446655440006', 'SN-GPU-0001', 'DISPONIBLE', NULL, NULL, 'gpu-rtx4060'),
('550e8400-e29b-41d4-a716-446655440007', 'SN-SSD-0001', 'DISPONIBLE', NULL, NULL, 'ssd-1tb');

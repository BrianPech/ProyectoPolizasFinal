-- =====================================================================
--  Base de datos: polizas_db
--  Sistema de gestión de pólizas de seguro vehicular
--  Generado a partir de basedatospolizas.sql + diagrama de clases UML
--  Compatible con MySQL 8 / MariaDB 10.4+ (XAMPP)
-- =====================================================================

DROP DATABASE IF EXISTS polizas_db;
CREATE DATABASE polizas_db
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE polizas_db;

SET NAMES utf8mb4;

-- ---------------------------------------------------------------------
--  SEGURIDAD
-- ---------------------------------------------------------------------
CREATE TABLE rol (
  id_rol       INT          NOT NULL AUTO_INCREMENT,
  nombre_rol   VARCHAR(50)  NOT NULL,
  descripcion  VARCHAR(150) NOT NULL,
  PRIMARY KEY (id_rol),
  UNIQUE KEY uq_rol_nombre (nombre_rol)
) ENGINE=InnoDB COMMENT='Roles de usuario';

CREATE TABLE usuario (
  id_usuario      INT          NOT NULL AUTO_INCREMENT,
  id_rol          INT          NOT NULL,
  nombre          VARCHAR(100) NOT NULL,
  usuario         VARCHAR(50)  NOT NULL,
  contrasena_hash VARCHAR(255) NOT NULL,
  estado          ENUM('Activo','Inactivo') NOT NULL DEFAULT 'Activo',
  PRIMARY KEY (id_usuario),
  UNIQUE KEY uq_usuario_usuario (usuario),
  CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol)
    REFERENCES rol (id_rol) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Cuentas de acceso de personal';

-- ---------------------------------------------------------------------
--  CATÁLOGOS
-- ---------------------------------------------------------------------
CREATE TABLE cliente (
  id_cliente     INT          NOT NULL AUTO_INCREMENT,
  tipo_persona   ENUM('Fisica','Moral') NOT NULL DEFAULT 'Fisica',
  nombre         VARCHAR(100) NOT NULL COMMENT 'Nombre o Razón Social',
  apellidos      VARCHAR(100) NULL     COMMENT 'Solo aplica a Persona Física',
  rfc            VARCHAR(13)  NOT NULL,
  telefono       VARCHAR(20)  NOT NULL,
  correo         VARCHAR(100) NOT NULL,
  direccion      TEXT         NOT NULL,
  fecha_registro DATE         NOT NULL DEFAULT (CURRENT_DATE),
  activo         BOOLEAN      NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_cliente),
  UNIQUE KEY uq_cliente_rfc (rfc),
  CONSTRAINT chk_cliente_rfc CHECK (CHAR_LENGTH(rfc) BETWEEN 12 AND 13),
  CONSTRAINT chk_cliente_apellidos CHECK (
    tipo_persona = 'Fisica' OR apellidos IS NULL
  )
) ENGINE=InnoDB COMMENT='Titulares y contratantes del seguro';

CREATE TABLE marca (
  id_marca     INT         NOT NULL AUTO_INCREMENT,
  nombre_marca VARCHAR(50) NOT NULL COMMENT 'Nissan, Toyota, Ford, etc.',
  pais_origen  VARCHAR(50) NULL,
  activa       BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_marca),
  UNIQUE KEY uq_marca_nombre (nombre_marca)
) ENGINE=InnoDB COMMENT='Catálogo de marcas automotrices';

CREATE TABLE vehiculo (
  id_vehiculo      INT         NOT NULL AUTO_INCREMENT,
  id_marca         INT         NOT NULL,
  placas           VARCHAR(10) NOT NULL,
  numero_serie_vin VARCHAR(17) NOT NULL,
  modelo           VARCHAR(50) NOT NULL,
  anio             INT         NOT NULL,
  color            VARCHAR(30) NOT NULL,
  uso              ENUM('Particular','Comercial') NOT NULL,
  activo           BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_vehiculo),
  UNIQUE KEY uq_vehiculo_placas (placas),
  UNIQUE KEY uq_vehiculo_vin (numero_serie_vin),
  CONSTRAINT chk_vehiculo_vin  CHECK (CHAR_LENGTH(numero_serie_vin) BETWEEN 10 AND 17),
  CONSTRAINT chk_vehiculo_anio CHECK (anio BETWEEN 1900 AND 2100),
  CONSTRAINT fk_vehiculo_marca FOREIGN KEY (id_marca)
    REFERENCES marca (id_marca) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Activos vehiculares independientes';

CREATE TABLE compania_aseguradora (
  id_aseguradora INT          NOT NULL AUTO_INCREMENT,
  nombre         VARCHAR(100) NOT NULL,
  rfc            VARCHAR(13)  NOT NULL,
  telefono       VARCHAR(20)  NOT NULL,
  correo         VARCHAR(100) NOT NULL,
  contacto       VARCHAR(100) NOT NULL,
  activa         BOOLEAN      NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_aseguradora),
  UNIQUE KEY uq_aseguradora_rfc (rfc)
) ENGINE=InnoDB COMMENT='Compañías matrices emisoras';

CREATE TABLE tipo_poliza (
  id_tipo_poliza INT          NOT NULL AUTO_INCREMENT,
  nombre_tipo    VARCHAR(50)  NOT NULL COMMENT 'Individual, Flotilla, Carga, Plataforma',
  descripcion    VARCHAR(200) NOT NULL,
  activa         BOOLEAN      NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_tipo_poliza),
  UNIQUE KEY uq_tipo_poliza_nombre (nombre_tipo)
) ENGINE=InnoDB COMMENT='Catálogo de modalidades de póliza';

CREATE TABLE cobertura (
  id_cobertura INT         NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(80) NOT NULL COMMENT 'RC, Daños Materiales, Robo Total, Gastos Médicos',
  descripcion  TEXT        NOT NULL,
  activa       BOOLEAN     NOT NULL DEFAULT TRUE,
  PRIMARY KEY (id_cobertura),
  UNIQUE KEY uq_cobertura_nombre (nombre)
) ENGINE=InnoDB COMMENT='Catálogo maestro de coberturas';

-- ---------------------------------------------------------------------
--  PÓLIZA Y COBERTURAS
-- ---------------------------------------------------------------------
CREATE TABLE poliza (
  id_poliza          INT           NOT NULL AUTO_INCREMENT,
  numero_poliza      VARCHAR(30)   NOT NULL,
  id_tipo_poliza     INT           NOT NULL,
  id_cliente         INT           NOT NULL,
  id_vehiculo        INT           NOT NULL,
  id_aseguradora     INT           NOT NULL,
  id_usuario         INT           NOT NULL COMMENT 'Agente vendedor (RF-41)',
  id_poliza_anterior INT           NULL     COMMENT 'Origen en renovaciones (RF-24)',
  periodicidad_pago  ENUM('Contado','Semestral','Trimestral','Mensual') NOT NULL DEFAULT 'Contado',
  fecha_emision      DATE          NOT NULL,
  fecha_inicio       DATE          NOT NULL,
  fecha_vencimiento  DATE          NOT NULL,
  prima_total        DECIMAL(10,2) NOT NULL DEFAULT 0,
  estado             ENUM('Activa','Por Vencer','Vencida','Cancelada') NOT NULL DEFAULT 'Activa',
  PRIMARY KEY (id_poliza),
  UNIQUE KEY uq_poliza_numero (numero_poliza),
  KEY idx_poliza_vencimiento (fecha_vencimiento),
  KEY idx_poliza_estado (estado),
  CONSTRAINT chk_poliza_fechas CHECK (fecha_vencimiento > fecha_inicio),
  CONSTRAINT chk_poliza_prima  CHECK (prima_total >= 0),
  CONSTRAINT fk_poliza_tipo FOREIGN KEY (id_tipo_poliza)
    REFERENCES tipo_poliza (id_tipo_poliza) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_poliza_cliente FOREIGN KEY (id_cliente)
    REFERENCES cliente (id_cliente) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_poliza_vehiculo FOREIGN KEY (id_vehiculo)
    REFERENCES vehiculo (id_vehiculo) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_poliza_aseguradora FOREIGN KEY (id_aseguradora)
    REFERENCES compania_aseguradora (id_aseguradora) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_poliza_agente FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_poliza_anterior FOREIGN KEY (id_poliza_anterior)
    REFERENCES poliza (id_poliza) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Contrato nuclear que vincula cliente, coche, aseguradora y agente';

CREATE TABLE poliza_cobertura (
  id_poliza_cobertura INT           NOT NULL AUTO_INCREMENT,
  id_poliza           INT           NOT NULL,
  id_cobertura        INT           NOT NULL,
  suma_asegurada      DECIMAL(12,2) NOT NULL,
  deducible           DECIMAL(5,2)  NOT NULL COMMENT 'Porcentaje deducible',
  prima_cobertura     DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'Costo asignado a esta cobertura',
  PRIMARY KEY (id_poliza_cobertura),
  UNIQUE KEY uq_poliza_cobertura (id_poliza, id_cobertura),
  CONSTRAINT chk_pc_suma      CHECK (suma_asegurada >= 0),
  CONSTRAINT chk_pc_deducible CHECK (deducible BETWEEN 0 AND 100),
  CONSTRAINT chk_pc_prima     CHECK (prima_cobertura >= 0),
  CONSTRAINT fk_pc_poliza FOREIGN KEY (id_poliza)
    REFERENCES poliza (id_poliza) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_pc_cobertura FOREIGN KEY (id_cobertura)
    REFERENCES cobertura (id_cobertura) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Relación 1 a N: Desglose de coberturas contratadas';

-- ---------------------------------------------------------------------
--  COBRANZA
-- ---------------------------------------------------------------------
CREATE TABLE pago (
  id_pago                 INT           NOT NULL AUTO_INCREMENT,
  id_poliza               INT           NOT NULL,
  id_usuario              INT           NULL COMMENT 'Cajero/agente que cobró (NULL mientras esté pendiente)',
  numero_cuota            INT           NOT NULL DEFAULT 1,
  monto                   DECIMAL(10,2) NOT NULL,
  fecha_vencimiento_cuota DATE          NOT NULL COMMENT 'Límite para pagar la cuota',
  fecha_pago              DATE          NULL     COMMENT 'Fecha real en que el cliente pagó',
  numero_recibo           VARCHAR(30)   NULL     COMMENT 'Folio emitido al cobrar',
  forma_pago              ENUM('Efectivo','Tarjeta','Transferencia') NULL,
  estado                  ENUM('Pendiente','Pagado','Vencido','Cancelado') NOT NULL DEFAULT 'Pendiente',
  PRIMARY KEY (id_pago),
  UNIQUE KEY uq_pago_recibo (numero_recibo),
  UNIQUE KEY uq_pago_cuota (id_poliza, numero_cuota),
  KEY idx_pago_estado_venc (estado, fecha_vencimiento_cuota),
  CONSTRAINT chk_pago_monto CHECK (monto > 0),
  CONSTRAINT chk_pago_cuota CHECK (numero_cuota >= 1),
  -- Un pago marcado como Pagado debe tener cajero, fecha, recibo y forma de pago
  CONSTRAINT chk_pago_cobrado CHECK (
    estado <> 'Pagado' OR
    (id_usuario IS NOT NULL AND fecha_pago IS NOT NULL
     AND numero_recibo IS NOT NULL AND forma_pago IS NOT NULL)
  ),
  CONSTRAINT fk_pago_poliza FOREIGN KEY (id_poliza)
    REFERENCES poliza (id_poliza) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_pago_cajero FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Control de cuotas y abonos';

CREATE TABLE aviso_vencimiento (
  id_aviso         INT         NOT NULL AUTO_INCREMENT,
  id_poliza        INT         NOT NULL,
  id_pago          INT         NULL COMMENT 'Cuota si es aviso de cobro; NULL si es de póliza',
  tipo_aviso       ENUM('Renovacion_Poliza','Cobro_Cuota') NOT NULL DEFAULT 'Renovacion_Poliza',
  fecha_generacion DATE        NOT NULL,
  dias_restantes   INT         NOT NULL,
  medio_envio      ENUM('Correo','Notificación en Pantalla') NOT NULL,
  estado           ENUM('Pendiente','Enviado','Contactado','Renovado') NOT NULL DEFAULT 'Pendiente',
  PRIMARY KEY (id_aviso),
  KEY idx_aviso_estado (estado),
  CONSTRAINT chk_aviso_tipo CHECK (
    (tipo_aviso = 'Cobro_Cuota'       AND id_pago IS NOT NULL) OR
    (tipo_aviso = 'Renovacion_Poliza' AND id_pago IS NULL)
  ),
  CONSTRAINT fk_aviso_poliza FOREIGN KEY (id_poliza)
    REFERENCES poliza (id_poliza) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_aviso_pago FOREIGN KEY (id_pago)
    REFERENCES pago (id_pago) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Seguimiento de alertas de vigencia y cobranza (RF-32 a RF-36)';

-- ---------------------------------------------------------------------
--  CANCELACIONES
-- ---------------------------------------------------------------------
CREATE TABLE cancelacion_poliza (
  id_cancelacion      INT           NOT NULL AUTO_INCREMENT,
  id_poliza           INT           NOT NULL COMMENT 'Solo 1 trámite por póliza',
  id_usuario_solicita INT           NOT NULL COMMENT 'Agente que levantó la solicitud',
  id_usuario_autoriza INT           NULL     COMMENT 'Administrador que resolvió (NULL mientras esté pendiente)',
  fecha_solicitud     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_resolucion    DATETIME      NULL,
  motivo              VARCHAR(255)  NOT NULL COMMENT 'Venta de unidad, cambio de aseguradora, mora, etc.',
  devolucion_prima    DECIMAL(10,2) NOT NULL DEFAULT 0 COMMENT 'Reembolso de prima no devengada',
  estado_solicitud    ENUM('Pendiente','Aprobada','Rechazada') NOT NULL DEFAULT 'Pendiente',
  PRIMARY KEY (id_cancelacion),
  UNIQUE KEY uq_cancelacion_poliza (id_poliza),
  CONSTRAINT chk_cancel_devolucion CHECK (devolucion_prima >= 0),
  CONSTRAINT chk_cancel_resuelta CHECK (
    estado_solicitud = 'Pendiente' OR
    (id_usuario_autoriza IS NOT NULL AND fecha_resolucion IS NOT NULL)
  ),
  CONSTRAINT fk_cancel_poliza FOREIGN KEY (id_poliza)
    REFERENCES poliza (id_poliza) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT fk_cancel_solicita FOREIGN KEY (id_usuario_solicita)
    REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT fk_cancel_autoriza FOREIGN KEY (id_usuario_autoriza)
    REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Flujo administrativo de solicitudes de cancelación (RF-22)';

-- ---------------------------------------------------------------------
--  REPORTES
-- ---------------------------------------------------------------------
CREATE TABLE reporte (
  id_reporte         INT         NOT NULL AUTO_INCREMENT,
  id_usuario         INT         NOT NULL,
  tipo               VARCHAR(50) NOT NULL,
  fecha_generacion   DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  parametros_filtros TEXT        NOT NULL,
  PRIMARY KEY (id_reporte),
  CONSTRAINT fk_reporte_usuario FOREIGN KEY (id_usuario)
    REFERENCES usuario (id_usuario) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB COMMENT='Historial de generación de reportes';

-- =====================================================================
--  DATOS INICIALES DE CATÁLOGOS
-- =====================================================================
INSERT INTO rol (nombre_rol, descripcion) VALUES
  ('Administrador',   'Acceso total; autoriza cancelaciones y administra catálogos'),
  ('Agente de Ventas','Emite pólizas, registra cobros y solicita cancelaciones');

INSERT INTO tipo_poliza (nombre_tipo, descripcion) VALUES
  ('Individual', 'Póliza para un solo vehículo particular'),
  ('Flotilla',   'Póliza para un conjunto de vehículos de una misma empresa'),
  ('Carga',      'Póliza para vehículos de transporte de mercancías'),
  ('Plataforma', 'Póliza para vehículos de plataformas de movilidad (Uber, DiDi, etc.)');

INSERT INTO cobertura (nombre, descripcion) VALUES
  ('Responsabilidad Civil', 'Daños a terceros en sus bienes o personas'),
  ('Daños Materiales',      'Daños al vehículo asegurado por colisión, vuelco u otros riesgos'),
  ('Robo Total',            'Pérdida total del vehículo por robo'),
  ('Gastos Médicos',        'Gastos médicos de los ocupantes del vehículo asegurado');

INSERT INTO marca (nombre_marca, pais_origen) VALUES
  ('Nissan',     'Japón'),
  ('Toyota',     'Japón'),
  ('Honda',      'Japón'),
  ('Mazda',      'Japón'),
  ('Ford',       'Estados Unidos'),
  ('Chevrolet',  'Estados Unidos'),
  ('Volkswagen', 'Alemania'),
  ('Kia',        'Corea del Sur'),
  ('Hyundai',    'Corea del Sur');

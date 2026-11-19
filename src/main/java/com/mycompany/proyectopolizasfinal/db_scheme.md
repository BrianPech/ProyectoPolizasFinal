// ========================================================
// PROYECTO: Sistema de Gestión de Pólizas (Modelo 3NF Profesional)
// HERRAMIENTA: dbdiagram.io (DBML)
// ========================================================

TableGroup Catalogos_Maestros {
  cliente
  marca
  vehiculo
  compania_aseguradora
  tipo_poliza
  cobertura
}

TableGroup Operacion_Polizas_Cobranza {
  poliza
  poliza_cobertura
  pago
  aviso_vencimiento
  cancelacion_poliza
}

TableGroup Seguridad_Auditoria {
  rol
  usuario
  reporte
}

// --------------------------------------------------------
// 1. CLIENTES (Soporta Persona Física y Moral)
// --------------------------------------------------------
Table cliente {
  id_cliente int [pk, increment]
  tipo_persona varchar(10) [default: 'Fisica', not null, note: 'ENUM: Fisica, Moral']
  nombre varchar(100) [not null, note: 'Nombre o Razon Social']
  apellidos varchar(100) [null, note: 'Opcional si es Persona Moral']
  rfc varchar(13) [unique, not null]
  telefono varchar(20) [not null]
  correo varchar(100) [not null]
  direccion text [not null]
  fecha_registro date [not null]
  activo boolean [default: true, not null]

  Note: 'Titulares y contratantes del seguro'
}

// --------------------------------------------------------
// 2. MARCAS (Catálogo normalizado)
// --------------------------------------------------------
Table marca {
  id_marca int [pk, increment]
  nombre_marca varchar(50) [unique, not null, note: 'Nissan, Toyota, Ford, etc.']
  pais_origen varchar(50)
  activa boolean [default: true, not null]

  Note: 'Catálogo de marcas automotrices'
}

// --------------------------------------------------------
// 3. VEHÍCULOS (Desacoplado de cliente; FK a marca)
// --------------------------------------------------------
Table vehiculo {
  id_vehiculo int [pk, increment]
  id_marca int [not null]
  placas varchar(10) [unique, not null]
  numero_serie_vin varchar(17) [unique, not null]
  modelo varchar(50) [not null]
  anio int [not null]
  color varchar(30) [not null]
  uso varchar(30) [not null, note: 'Particular o Comercial']
  activo boolean [default: true, not null]

  Note: 'Activos vehiculares independientes'
}

// --------------------------------------------------------
// 4. COMPAÑÍAS ASEGURADORAS
// --------------------------------------------------------
Table compania_aseguradora {
  id_aseguradora int [pk, increment]
  nombre varchar(100) [not null]
  rfc varchar(13) [unique, not null]
  telefono varchar(20) [not null]
  correo varchar(100) [not null]
  contacto varchar(100) [not null]
  activa boolean [default: true, not null]

  Note: 'Compañías matrices emisoras'
}

// --------------------------------------------------------
// 5. TIPO DE PÓLIZA (Catálogo para 3NF)
// --------------------------------------------------------
Table tipo_poliza {
  id_tipo_poliza int [pk, increment]
  nombre_tipo varchar(50) [unique, not null, note: 'Individual, Flotilla, Carga, Plataforma']
  descripcion varchar(200) [not null]
  activa boolean [default: true, not null]

  Note: 'Catálogo de modalidades de póliza'
}

// --------------------------------------------------------
// 6. COBERTURAS (Catálogo base maestro)
// --------------------------------------------------------
Table cobertura {
  id_cobertura int [pk, increment]
  nombre varchar(80) [not null, note: 'RC, Daños Materiales, Robo Total, Gastos Medicos']
  descripcion text [not null]
  activa boolean [default: true, not null]

  Note: 'Catálogo maestro de coberturas'
}

// --------------------------------------------------------
// 7. PÓLIZA (Nexo contractual completo)
// --------------------------------------------------------
Table poliza {
  id_poliza int [pk, increment]
  numero_poliza varchar(30) [unique, not null]
  id_tipo_poliza int [not null]
  id_cliente int [not null]
  id_vehiculo int [not null]
  id_aseguradora int [not null]
  id_usuario int [not null, note: 'Agente vendedor (RF-41)']
  id_poliza_anterior int [null, note: 'Origen en renovaciones (RF-24)']
  periodicidad_pago varchar(20) [default: 'Anual', not null, note: 'ENUM: Contado, Semestral, Trimestral, Mensual']
  fecha_emision date [not null]
  fecha_inicio date [not null]
  fecha_vencimiento date [not null]
  prima_total decimal(10,2) [not null]
  estado varchar(20) [default: 'Activa', not null, note: 'ENUM: Activa, Por Vencer, Vencida, Cancelada']

  Note: 'Contrato nuclear que vincula cliente, coche, aseguradora y agente'
}

// --------------------------------------------------------
// 8. PÓLIZA_COBERTURA (Detalle de coberturas por póliza en 3NF)
// --------------------------------------------------------
Table poliza_cobertura {
  id_poliza_cobertura int [pk, increment]
  id_poliza int [not null]
  id_cobertura int [not null]
  suma_asegurada decimal(12,2) [not null]
  deducible decimal(5,2) [not null, note: 'Porcentaje deducible']
  prima_cobertura decimal(10,2) [not null, default: 0.00, note: 'Costo asignado a esta cobertura']

  indexes {
    (id_poliza, id_cobertura) [unique]
  }

  Note: 'Relación 1 a N: Desglose de coberturas contratadas'
}

// --------------------------------------------------------
// 9. PAGOS (Cronograma y liquidación con ENUMs)
// --------------------------------------------------------
Table pago {
  id_pago int [pk, increment]
  id_poliza int [not null]
  id_usuario int [null, note: 'Cajero/agente que cobró (null mientras esté pendiente)']
  numero_cuota int [default: 1, not null, note: 'Cuota 1, 2, 3...']
  monto decimal(10,2) [not null]
  fecha_vencimiento_cuota date [not null, note: 'Límite para pagar la cuota']
  fecha_pago date [null, note: 'Fecha real en que el cliente pagó']
  numero_recibo varchar(30) [unique, null, note: 'Folio emitido al cobrar']
  forma_pago varchar(20) [null, note: 'ENUM: Efectivo, Tarjeta, Transferencia']
  estado varchar(20) [default: 'Pendiente', not null, note: 'ENUM: Pendiente, Pagado, Vencido, Cancelado']

  Note: 'Control de cuotas y abonos'
}

// --------------------------------------------------------
// 10. AVISOS DE VENCIMIENTO (Unificado: Pólizas y Cuotas)
// --------------------------------------------------------
Table aviso_vencimiento {
  id_aviso int [pk, increment]
  id_poliza int [not null]
  id_pago int [null, note: 'Apunta a la cuota si es aviso de cobro; NULL si es de póliza']
  tipo_aviso varchar(20) [default: 'Renovacion_Poliza', not null, note: 'ENUM: Renovacion_Poliza, Cobro_Cuota']
  fecha_generacion date [not null]
  dias_restantes int [not null]
  medio_envio varchar(50) [not null, note: 'Correo, Notificación en Pantalla']
  estado varchar(20) [default: 'Pendiente', not null, note: 'ENUM: Pendiente, Enviado, Contactado, Renovado']

  Note: 'Seguimiento de alertas de vigencia y cobranza (RF-32 a RF-36)'
}

// --------------------------------------------------------
// 11. CANCELACIÓN DE PÓLIZA (Flujo formal de workflow en 3NF)
// --------------------------------------------------------
Table cancelacion_poliza {
  id_cancelacion int [pk, increment]
  id_poliza int [unique, not null, note: 'Solo 1 trámite por póliza']
  id_usuario_solicita int [not null, note: 'Agente que levantó la solicitud']
  id_usuario_autoriza int [null, note: 'Administrador que resolvió (null mientras esté pendiente)']
  fecha_solicitud datetime [not null]
  fecha_resolucion datetime [null]
  motivo varchar(255) [not null, note: 'Venta de unidad, cambio de aseguradora, mora, etc.']
  devolucion_prima decimal(10,2) [default: 0.00, not null, note: 'Reembolso de prima no devengada al cliente']
  estado_solicitud varchar(20) [default: 'Pendiente', not null, note: 'ENUM: Pendiente, Aprobada, Rechazada']

  Note: 'Flujo administrativo de solicitudes de cancelación (RF-22)'
}

// --------------------------------------------------------
// 12. ROL (Catálogo de roles en 3NF)
// --------------------------------------------------------
Table rol {
  id_rol int [pk, increment]
  nombre_rol varchar(50) [unique, not null, note: 'Administrador, Agente de Ventas']
  descripcion varchar(150) [not null]

  Note: 'Roles de usuario'
}

// --------------------------------------------------------
// 13. USUARIOS (Operadores del sistema)
// --------------------------------------------------------
Table usuario {
  id_usuario int [pk, increment]
  id_rol int [not null]
  nombre varchar(100) [not null]
  usuario varchar(50) [unique, not null]
  contrasena_hash varchar(255) [not null]
  estado varchar(20) [default: 'Activo', not null, note: 'ENUM: Activo, Inactivo']

  Note: 'Cuentas de acceso de personal'
}

// --------------------------------------------------------
// 14. REPORTES (Auditoría de reportes emitidos)
// --------------------------------------------------------
Table reporte {
  id_reporte int [pk, increment]
  id_usuario int [not null]
  tipo varchar(50) [not null]
  fecha_generacion datetime [not null]
  parametros_filtros text [not null]

  Note: 'Historial de generación de reportes'
}

// ========================================================
// RELACIONES E INTEGRIDAD REFERENCIAL (FKs)
// ========================================================

// Marca -> Vehículo
Ref: vehiculo.id_marca > marca.id_marca [delete: restrict]

// Póliza y sus nexos
Ref: poliza.id_tipo_poliza > tipo_poliza.id_tipo_poliza [delete: restrict]
Ref: poliza.id_cliente > cliente.id_cliente [delete: restrict]
Ref: poliza.id_vehiculo > vehiculo.id_vehiculo [delete: restrict]
Ref: poliza.id_aseguradora > compania_aseguradora.id_aseguradora [delete: restrict]
Ref: poliza.id_usuario > usuario.id_usuario [delete: restrict]
Ref: poliza.id_poliza_anterior > poliza.id_poliza [delete: set null]

// Póliza - Coberturas (1 a N)
Ref: poliza_cobertura.id_poliza > poliza.id_poliza [delete: cascade]
Ref: poliza_cobertura.id_cobertura > cobertura.id_cobertura [delete: restrict]

// Pagos y Avisos
Ref: pago.id_poliza > poliza.id_poliza [delete: restrict]
Ref: pago.id_usuario > usuario.id_usuario [delete: restrict]

Ref: aviso_vencimiento.id_poliza > poliza.id_poliza [delete: cascade]
Ref: aviso_vencimiento.id_pago > pago.id_pago [delete: cascade]

// Cancelación de Póliza (Workflow de Solicitud y Aprobación)
Ref: cancelacion_poliza.id_poliza > poliza.id_poliza [delete: cascade]
Ref: cancelacion_poliza.id_usuario_solicita > usuario.id_usuario [delete: restrict]
Ref: cancelacion_poliza.id_usuario_autoriza > usuario.id_usuario [delete: restrict]

// Seguridad y Auditoría
Ref: usuario.id_rol > rol.id_rol [delete: restrict]
Ref: reporte.id_usuario > usuario.id_usuario [delete: restrict]
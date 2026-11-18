/*
 * Frontend de Veterinaria: consume la API REST de la misma aplicación.
 *
 *   RF-01  POST /api/mascotas               registrar mascota
 *   RF-02  GET  /api/mascotas/{id}          consultar mascota con sus citas
 *   RF-03  POST /api/citas                  agendar cita
 *   RF-04  PUT  /api/citas/{id}/estado      cambiar el estado de una cita
 *   RF-05  PUT  /api/mascotas/{id}          actualizar datos del propietario
 *
 * Las reglas de negocio viven en el servidor: este archivo solo muestra el
 * mensaje que devuelve la API ({"mensaje": "..."}) cuando rechaza una operación.
 */
(() => {
  'use strict';

  const CLAVE_RECIENTES = 'veterinaria.recientes';
  const MAX_RECIENTES = 6;

  const formatoFecha = new Intl.DateTimeFormat('es-CO', {
    weekday: 'long', day: 'numeric', month: 'long', year: 'numeric',
    hour: 'numeric', minute: '2-digit'
  });

  const ETIQUETA_ESTADO = { PENDIENTE: 'Pendiente', ATENDIDA: 'Atendida', CANCELADA: 'Cancelada' };

  let mascotaActualId = null;

  // ---------- Utilidades ----------

  const $ = (selector, raiz = document) => raiz.querySelector(selector);

  /** Crea un elemento DOM. Todo el texto entra como texto, nunca como HTML. */
  function h(etiqueta, atributos = {}, ...hijos) {
    const el = document.createElement(etiqueta);
    for (const [clave, valor] of Object.entries(atributos)) {
      if (valor === false || valor == null) continue;
      if (clave === 'class') el.className = valor;
      else if (clave.startsWith('on')) el.addEventListener(clave.slice(2), valor);
      else el.setAttribute(clave, valor === true ? '' : valor);
    }
    for (const hijo of hijos.flat()) {
      if (hijo == null || hijo === false) continue;
      el.append(hijo);
    }
    return el;
  }

  function aviso(texto, tipo = 'ok') {
    const el = h('div', { class: `aviso aviso-${tipo}`, role: tipo === 'error' ? 'alert' : null },
      h('p', {}, texto),
      h('button', { type: 'button', 'aria-label': 'Cerrar aviso', onclick: () => el.remove() }, '×'));
    $('#avisos').append(el);
    setTimeout(() => el.remove(), tipo === 'error' ? 9000 : 5000);
  }

  class ErrorApi extends Error {
    constructor(mensaje, estado) {
      super(mensaje);
      this.estado = estado;
    }
  }

  async function api(ruta, { metodo = 'GET', cuerpo } = {}) {
    let respuesta;
    try {
      respuesta = await fetch(ruta, {
        method: metodo,
        headers: cuerpo ? { 'Content-Type': 'application/json' } : {},
        body: cuerpo ? JSON.stringify(cuerpo) : undefined
      });
    } catch (e) {
      throw new ErrorApi('No hay conexión con el servidor. Verifica que la aplicación esté en marcha.', 0);
    }
    const texto = await respuesta.text();
    let datos = null;
    if (texto) {
      try { datos = JSON.parse(texto); } catch (e) { datos = null; }
    }
    if (!respuesta.ok) {
      const mensaje = datos && datos.mensaje
        ? datos.mensaje
        : `No se pudo completar la operación (HTTP ${respuesta.status}).`;
      throw new ErrorApi(mensaje, respuesta.status);
    }
    return datos;
  }

  /** Ejecuta una acción con el botón deshabilitado para evitar envíos dobles. */
  async function conBoton(boton, accion) {
    if (boton) boton.disabled = true;
    try {
      await accion();
    } catch (e) {
      aviso(e.message, 'error');
    } finally {
      if (boton) boton.disabled = false;
    }
  }

  const texto = (valor) => {
    const limpio = (valor || '').trim();
    return limpio === '' ? null : limpio;
  };

  function fechaLocalActual() {
    const d = new Date();
    d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
    return d.toISOString().slice(0, 16);
  }

  /** "2022-03-10" -> "10 de marzo de 2022 (3 años)". Se separa a mano para evitar desfases de zona horaria. */
  function describirNacimiento(iso) {
    if (!iso) return null;
    const [a, m, d] = iso.split('-').map(Number);
    const nacimiento = new Date(a, m - 1, d);
    const hoy = new Date();
    let meses = (hoy.getFullYear() - a) * 12 + (hoy.getMonth() - (m - 1));
    if (hoy.getDate() < d) meses -= 1;
    let edad = '';
    if (meses >= 24) edad = `${Math.floor(meses / 12)} años`;
    else if (meses >= 12) edad = '1 año';
    else if (meses >= 1) edad = `${meses} ${meses === 1 ? 'mes' : 'meses'}`;
    else if (meses >= 0) edad = 'menos de un mes';
    const fecha = new Intl.DateTimeFormat('es-CO', { day: 'numeric', month: 'long', year: 'numeric' }).format(nacimiento);
    return edad ? `${fecha} (${edad})` : fecha;
  }

  // ---------- Recientes (la API no lista mascotas, así que se recuerdan aquí) ----------

  function leerRecientes() {
    try {
      const lista = JSON.parse(localStorage.getItem(CLAVE_RECIENTES) || '[]');
      return Array.isArray(lista) ? lista : [];
    } catch (e) {
      return [];
    }
  }

  function recordar(mascota) {
    const lista = leerRecientes().filter((m) => m.id !== mascota.id);
    lista.unshift({ id: mascota.id, nombre: mascota.nombre });
    try {
      localStorage.setItem(CLAVE_RECIENTES, JSON.stringify(lista.slice(0, MAX_RECIENTES)));
    } catch (e) { /* sin almacenamiento: la lista simplemente no se guarda */ }
    pintarRecientes();
  }

  function pintarRecientes() {
    const lista = leerRecientes();
    const ul = $('#recientes');
    ul.replaceChildren(...lista.map((m) =>
      h('li', {}, h('button', { type: 'button', onclick: () => cargarFicha(m.id, { enfocar: true }) },
        h('strong', {}, `#${m.id}`), m.nombre))));
    $('#recientes-vacio').hidden = lista.length > 0;
  }

  // ---------- RF-02: consultar y pintar la ficha ----------

  async function cargarFicha(id, { enfocar = false } = {}) {
    try {
      const mascota = await api(`/api/mascotas/${encodeURIComponent(id)}`);
      mascotaActualId = mascota.id;
      pintarFicha(mascota);
      recordar(mascota);
      history.replaceState(null, '', `#mascota-${mascota.id}`);
      if (enfocar) $('#ficha').focus();
    } catch (e) {
      if (e.estado === 404) {
        mascotaActualId = null;
        pintarNoEncontrada(id, e.message);
        if (enfocar) $('#ficha').focus();
      } else {
        aviso(e.message, 'error');
      }
    }
  }

  function pintarNoEncontrada(id, mensaje) {
    $('#ficha').replaceChildren(
      h('div', { class: 'vacio error' },
        h('h2', {}, `No existe una mascota con el ID ${id}`),
        h('p', {}, `${mensaje}. Revisa el número o registra una mascota nueva en el formulario de la izquierda.`)));
  }

  function pintarFicha(m) {
    const subtitulo = [m.especie, m.raza].filter(Boolean).join(', ');
    const nacimiento = describirNacimiento(m.fechaNacimiento);
    const citas = [...(m.citas || [])].sort((a, b) => String(a.fecha).localeCompare(String(b.fecha)));

    $('#ficha').replaceChildren(
      h('header', { class: 'ficha-cabecera' },
        h('div', { class: 'tag', role: 'img', 'aria-label': `ID ${m.id}` }, String(m.id), h('small', {}, 'ID')),
        h('div', {},
          h('h2', {}, m.nombre),
          h('p', { class: 'ficha-sub' }, subtitulo),
          nacimiento ? h('p', { class: 'ficha-sub' }, `Nació el ${nacimiento}`) : null)),
      panelPropietario(m),
      panelCitas(m, citas),
      panelAgendar(m));
  }

  // ---------- RF-05: actualizar datos del propietario ----------

  function panelPropietario(m) {
    const vista = h('div', {},
      h('dl', { class: 'datos' },
        h('dt', {}, 'Propietario'), h('dd', {}, m.nombrePropietario),
        h('dt', {}, 'Teléfono'), h('dd', {}, m.telefonoPropietario)));
    const botonEditar = h('button', { type: 'button', class: 'btn btn-secundario btn-chico' }, 'Actualizar datos de contacto');
    vista.append(h('div', { class: 'acciones' }, botonEditar));

    const form = h('form', { class: 'formulario', hidden: true, novalidate: true },
      campo('p-nombre', 'Nombre del propietario', 'nombrePropietario', m.nombrePropietario),
      campo('p-telefono', 'Teléfono del propietario', 'telefonoPropietario', m.telefonoPropietario, 'tel'),
      h('div', { class: 'acciones' },
        h('button', { type: 'submit', class: 'btn btn-primario' }, 'Guardar cambios'),
        h('button', { type: 'button', class: 'btn btn-secundario', onclick: () => alternar(false) }, 'Cancelar')));

    function alternar(editando) {
      form.hidden = !editando;
      vista.hidden = editando;
      if (editando) $('input', form).focus();
      else botonEditar.focus();
    }
    botonEditar.addEventListener('click', () => alternar(true));

    form.addEventListener('submit', (ev) => {
      ev.preventDefault();
      conBoton($('button[type=submit]', form), async () => {
        await api(`/api/mascotas/${m.id}`, {
          metodo: 'PUT',
          cuerpo: {
            nombrePropietario: form.elements.nombrePropietario.value.trim(),
            telefonoPropietario: form.elements.telefonoPropietario.value.trim()
          }
        });
        aviso('Datos del propietario actualizados.');
        await cargarFicha(m.id);
      });
    });

    return h('section', { class: 'panel', 'aria-labelledby': 'ficha-prop' },
      h('h3', { id: 'ficha-prop' }, 'Datos del propietario'), vista, form);
  }

  function campo(id, etiqueta, nombre, valor = '', tipo = 'text') {
    return h('div', { class: 'campo' },
      h('label', { for: id }, etiqueta),
      h('input', { id, name: nombre, type: tipo, value: valor, autocomplete: 'off' }));
  }

  // ---------- RF-04: citas y cambio de estado ----------

  function panelCitas(m, citas) {
    const contenido = citas.length === 0
      ? h('p', { class: 'nota' }, 'Esta mascota todavía no tiene citas.')
      : h('ul', { class: 'citas' }, citas.map(tarjetaCita));
    return h('section', { class: 'panel', 'aria-labelledby': 'ficha-citas' },
      h('h3', { id: 'ficha-citas' }, `Citas (${citas.length})`), contenido);
  }

  function tarjetaCita(c) {
    const estado = (c.estado || '').toUpperCase();
    const clase = estado.toLowerCase();
    const etiqueta = ETIQUETA_ESTADO[estado] || estado;
    const fecha = c.fecha ? formatoFecha.format(new Date(c.fecha)) : 'Sin fecha';

    const botonAtendida = h('button', {
      type: 'button', class: 'btn btn-secundario btn-chico', disabled: estado === 'ATENDIDA',
      onclick: (ev) => cambiarEstado(c.id, 'ATENDIDA', ev.currentTarget, 'Cita marcada como atendida.')
    }, 'Marcar como atendida');

    const botonCancelar = h('button', {
      type: 'button', class: 'btn btn-peligro btn-chico', disabled: estado === 'CANCELADA',
      onclick: (ev) => cambiarEstado(c.id, 'CANCELADA', ev.currentTarget, 'Cita cancelada.')
    }, 'Cancelar cita');

    return h('li', { class: `cita cita-${clase}` },
      h('div', { class: 'cita-linea' },
        h('span', { class: 'cita-fecha' }, fecha),
        h('span', { class: `estado estado-${clase}` }, etiqueta)),
      h('p', { class: 'cita-detalle' },
        [c.motivo, c.veterinarioAsignado ? `con ${c.veterinarioAsignado}` : null].filter(Boolean).join(', ')),
      h('div', { class: 'acciones' }, botonAtendida, botonCancelar));
  }

  function cambiarEstado(idCita, nuevoEstado, boton, mensajeOk) {
    return conBoton(boton, async () => {
      await api(`/api/citas/${idCita}/estado`, { metodo: 'PUT', cuerpo: { estado: nuevoEstado } });
      aviso(mensajeOk);
      await cargarFicha(mascotaActualId);
    });
  }

  // ---------- RF-03: agendar cita ----------

  function panelAgendar(m) {
    const form = h('form', { class: 'formulario', novalidate: true },
      h('div', { class: 'campo' },
        h('label', { for: 'c-fecha' }, 'Fecha y hora ', h('span', { class: 'req' }, 'obligatorio')),
        h('input', { id: 'c-fecha', name: 'fecha', type: 'datetime-local', min: fechaLocalActual() })),
      h('div', { class: 'campo' },
        h('label', { for: 'c-motivo' }, 'Motivo'),
        h('input', { id: 'c-motivo', name: 'motivo', list: 'lista-motivos', autocomplete: 'off' }),
        h('datalist', { id: 'lista-motivos' },
          h('option', { value: 'Vacunación' }), h('option', { value: 'Control' }), h('option', { value: 'Cirugía' }))),
      h('div', { class: 'campo' },
        h('label', { for: 'c-vet' }, 'Veterinario asignado ', h('span', { class: 'req' }, 'obligatorio')),
        h('input', { id: 'c-vet', name: 'veterinarioAsignado', autocomplete: 'off' })),
      h('div', { class: 'acciones' },
        h('button', { type: 'submit', class: 'btn btn-primario' }, 'Agendar cita')));

    form.addEventListener('submit', (ev) => {
      ev.preventDefault();
      const f = form.elements;
      conBoton($('button[type=submit]', form), async () => {
        await api('/api/citas', {
          metodo: 'POST',
          cuerpo: {
            fecha: f.fecha.value ? `${f.fecha.value}:00` : null,
            motivo: texto(f.motivo.value),
            veterinarioAsignado: texto(f.veterinarioAsignado.value),
            mascota: { id: m.id }
          }
        });
        aviso('Cita agendada en estado pendiente.');
        await cargarFicha(m.id);
      });
    });

    return h('section', { class: 'panel', 'aria-labelledby': 'ficha-agendar' },
      h('h3', { id: 'ficha-agendar' }, 'Agendar una cita'), form);
  }

  // ---------- RF-01: registrar mascota ----------

  function iniciarRegistro() {
    const form = $('#form-registrar');
    form.elements.fechaNacimiento.max = fechaLocalActual().slice(0, 10);

    form.addEventListener('submit', (ev) => {
      ev.preventDefault();
      const f = form.elements;
      conBoton($('button[type=submit]', form), async () => {
        const creada = await api('/api/mascotas', {
          metodo: 'POST',
          cuerpo: {
            nombre: texto(f.nombre.value),
            especie: texto(f.especie.value),
            raza: texto(f.raza.value),
            fechaNacimiento: f.fechaNacimiento.value || null,
            nombrePropietario: texto(f.nombrePropietario.value),
            telefonoPropietario: texto(f.telefonoPropietario.value)
          }
        });
        aviso(`Mascota registrada con el ID ${creada.id}.`);
        form.reset();
        await cargarFicha(creada.id, { enfocar: true });
      });
    });
  }

  // ---------- Búsqueda por ID ----------

  function iniciarBusqueda() {
    $('#form-buscar').addEventListener('submit', (ev) => {
      ev.preventDefault();
      const id = $('#buscar-id').value.trim();
      if (!/^\d+$/.test(id) || Number(id) < 1) {
        aviso('Escribe un ID válido: un número entero mayor que 0.', 'error');
        return;
      }
      cargarFicha(id, { enfocar: true });
    });
  }

  // ---------- Arranque ----------

  document.addEventListener('DOMContentLoaded', () => {
    iniciarBusqueda();
    iniciarRegistro();
    pintarRecientes();
    const enlace = /^#mascota-(\d+)$/.exec(location.hash);
    if (enlace) cargarFicha(enlace[1]);
  });
})();

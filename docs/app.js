/* VitaCerca: prototipo académico; NO es un sistema de seguridad real. */
(()=>{
 'use strict';
 const KEY='vitacerca-watch-family-demo-v2';
 const now=()=>Date.now();
 const initial=()=>({incident:null,history:[]});
 let state=initial();
 try { const x=JSON.parse(localStorage.getItem(KEY)||'null'); if(x&&Array.isArray(x.history)) state=x; } catch(_){}
 const channel=typeof BroadcastChannel==='function'?new BroadcastChannel(KEY):null;
 const $=q=>document.querySelector(q);
 const view=new URLSearchParams(location.search).get('vista')==='reloj'?'reloj':'familia';
 let lastRendered='';
 const stamp=t=>new Date(t).toLocaleTimeString('es-CL',{hour:'2-digit',minute:'2-digit'});
 const esc=s=>String(s).replace(/[&<>"']/g,c=>({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
 function add(text,level='info'){state.history.unshift({text,time:now(),level});state.history=state.history.slice(0,30);}
 function persist(){try{localStorage.setItem(KEY,JSON.stringify(state));}catch(_){} channel?.postMessage('update');render();}
 function sync(){try {const x=JSON.parse(localStorage.getItem(KEY)||'null'); if(x&&Array.isArray(x.history))state=x;}catch(_){} render();}
 window.addEventListener('storage',e=>{if(e.key===KEY)sync()});
 if(channel)channel.onmessage=sync;
 const contentByLevel={rojo:{label:'ROJA · SOS',title:'Solicitud de ayuda',info:'El botón SOS fue activado en el prototipo.'},naranja:{label:'NARANJA · CAÍDA',title:'Posible caída',info:'No se registró una respuesta a la consulta de bienestar.'},amarillo:{label:'AMARILLA',title:'Aviso preventivo',info:'Se simuló una señal que merece revisión.'}};
 function fall(){state.incident={level:'naranja',status:'countdown',deadline:now()+12000,at:now()};add('Posible caída: consultando bienestar','naranja');persist()}
 function sos(){state.incident={level:'rojo',status:'active',at:now()};add('SOS manual activado','rojo');persist()}
 function yellow(){state.incident={level:'amarillo',status:'active',at:now()};add('Aviso preventivo','amarillo');persist()}
 function fine(){if(state.incident){add('El usuario confirmó que está bien','verde');state.incident=null;}else add('El usuario pulsó «Estoy bien»','verde');persist()}
 function acknowledge(){if(!state.incident||state.incident.status==='countdown')return;state.incident.status='ack';add('La red de apoyo confirmó «Voy en camino»','verde');persist()}
 function resolve(){if(!state.incident)return;add('Evento cerrado por la red de apoyo','verde');state.incident=null;persist()}
 function advance(){if(state.incident?.status!=='countdown')return;state.incident.status='active';delete state.incident.deadline;add('Sin respuesta: se muestra alerta naranja','naranja');persist()}
 function tick(){if(state.incident?.status==='countdown'&&state.incident.deadline<=now())advance(); else if(state.incident?.status==='countdown')render();}
 function render(){
  if(view==='reloj'){
    const a=state.incident;
    const status=$('#watch-status'),detail=$('#watch-description');
    status.classList.toggle('alert',!!a);
    if(!a){status.textContent='ESTOY BIEN';detail.textContent='Sin alertas activas';}
    else if(a.status==='countdown'){status.textContent='¿ESTÁS BIEN?';detail.textContent='Posible caída · '+Math.max(0,Math.ceil((a.deadline-now())/1000))+' s para responder';}
    else if(a.status==='ack'){status.textContent='AYUDA CONFIRMADA';detail.textContent='Tu red de apoyo confirmó que va en camino (simulación)';}
    else{status.textContent=a.level==='rojo'?'SOS ACTIVADO':a.level==='amarillo'?'AVISO ACTIVO':'ALERTA DE CAÍDA';detail.textContent='Aviso visible para la red de apoyo (misma sesión)';}
    $('#watch-context').textContent='Vista de reloj simulada. No usa sensores ni envía avisos a otros dispositivos.';
  } else {
    const a=state.incident;
    const hero=$('#hero-state');
    hero.classList.toggle('is-alert',!!a);
    hero.innerHTML=a?`<span class="hero-tag">● ALERTA ${esc(a.level.toUpperCase())}</span><h2>${a.status==='ack'?'La ayuda fue confirmada':a.status==='countdown'?'Consultando el bienestar de Marta':'Marta podría necesitar ayuda'}</h2><p>Evento simulado. Ningún familiar externo ha recibido una notificación real.</p>`:'<span class="hero-tag">✓ SIN ALERTAS ACTIVAS</span><h2>Todo en orden por ahora</h2><p>Esta pantalla representa la experiencia de un familiar. Los indicadores son de demostración.</p>';
    const host=$('#family-incident');
    if(!a){host.innerHTML='<p class="muted">No hay alertas activas. Simula una caída desde el reloj o utiliza los controles de prueba.</p>'}
    else{const d=contentByLevel[a.level],remaining=Math.max(0,Math.ceil((a.deadline-now())/1000));let actions='';
      if(a.status==='countdown')actions=`<button class="secondary" data-action="fine">Confirmar «Estoy bien»</button><button class="primary" data-action="advance">Omitir espera (${remaining}s)</button>`;
      else if(a.status==='ack')actions='<button class="primary" data-action="resolve">Cerrar evento</button>';
      else actions='<button class="primary" data-action="ack">✓ Voy en camino</button><button class="secondary" data-action="resolve">Cerrar evento</button>';
      host.innerHTML=`<div class="alert-panel ${a.level==='rojo'?'red':a.level==='amarillo'?'yellow':''} ${a.status==='ack'?'confirmed':''}"><span class="alert-level">${d.label}</span><h3>${esc(d.title)}</h3><p>${esc(d.info)}</p><p><strong>Estado:</strong> ${a.status==='countdown'?'Esperando confirmación de bienestar':a.status==='ack'?'Respuesta coordinada (simulada)':'Requiere atención de la red de apoyo'}</p><div class="actions">${actions}</div></div>`;
    }
    const hist=$('#history-list');
    hist.innerHTML=state.history.length?'<div class="history">'+state.history.map(e=>`<div class="history-entry"><div>${({rojo:'🔴',naranja:'🟠',amarillo:'🟡',verde:'🟢',info:'🔵'}[e.level]||'•')} ${esc(e.text)}</div><small>${stamp(e.time)}</small></div>`).join('')+'</div>':'<div class="empty-history">Todavía no hay eventos. Prueba el botón SOS.</div>';
  }
 }
 document.addEventListener('click',e=>{
   const b=e.target.closest('[data-action]');if(!b)return;
   switch(b.dataset.action){case 'fall':fall();break;case 'sos':sos();break;case 'yellow':yellow();break;case 'fine':fine();break;case 'ack':acknowledge();break;case 'resolve':resolve();break;case 'advance':advance();break;case 'clear':state.history=[];persist();break;case 'reset':state=initial();persist();break;}
 });
 $('#view-family').hidden=view!=='familia';$('#view-watch').hidden=view!=='reloj';
 $('#link-family').classList.toggle('active',view==='familia');$('#link-watch').classList.toggle('active',view==='reloj');
 document.title='VitaCerca · '+(view==='reloj'?'Smartwatch':'Red de apoyo');
 setInterval(tick,300);render();
 if('serviceWorker'in navigator&&location.protocol.startsWith('http'))navigator.serviceWorker.register('./sw.js').catch(()=>{});
})();

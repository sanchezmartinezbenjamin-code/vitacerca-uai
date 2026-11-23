package cl.vitacerca.demo

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import org.json.JSONArray

/**
 * VitaCerca: prototipo autónomo para Wear OS.
 * NO se conecta a sensores, personas, teléfonos o emergencias reales.
 */
class MainActivity : Activity() {
    private val bg = Color.rgb(14, 28, 40)
    private val mint = Color.rgb(33, 142, 104)
    private val red = Color.rgb(216, 76, 81)
    private val ink = Color.WHITE
    private val soft = Color.rgb(177, 200, 208)
    private var mode = "reloj"
    private var incident: String? = null
    private var incidentStatus = ""
    private var deadline = 0L
    private val history = mutableListOf<String>()
    private val handler = Handler(Looper.getMainLooper())
    private var lastSecond = -1L

    private val refreshLoop = object : Runnable {
        override fun run() {
            if (incidentStatus == "esperando") {
                val remaining = ((deadline - System.currentTimeMillis() + 999) / 1000).coerceAtLeast(0)
                if (remaining == 0L) {
                    incidentStatus = "activa"
                    add("Alerta naranja: sin respuesta")
                    render()
                } else if (remaining != lastSecond) {
                    lastSecond = remaining
                    render()
                }
            }
            handler.postDelayed(this, 350)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val p = getSharedPreferences("vitacerca_demo", MODE_PRIVATE)
        mode = p.getString("mode", "reloj") ?: "reloj"
        incident = p.getString("incident", null)
        incidentStatus = p.getString("incidentStatus", "") ?: ""
        deadline = p.getLong("deadline", 0)
        try {
            val a = JSONArray(p.getString("history", "[]"))
            for (i in 0 until a.length()) history.add(a.getString(i))
        } catch (_: Exception) { }
        render()
        handler.post(refreshLoop)
    }
    override fun onDestroy() { handler.removeCallbacks(refreshLoop); super.onDestroy() }
    private fun store() {
        val p = getSharedPreferences("vitacerca_demo", MODE_PRIVATE).edit()
        p.putString("mode", mode).putString("incident", incident)
            .putString("incidentStatus", incidentStatus).putLong("deadline", deadline)
            .putString("history", JSONArray(history).toString()).apply()
    }
    private fun add(item: String) { history.add(0, item); if (history.size > 12) history.removeAt(history.lastIndex);store() }
    private fun startSOS() { incident = "roja"; incidentStatus = "activa"; deadline = 0; add("SOS activado (demo)"); render() }
    private fun startFall() { incident = "naranja"; incidentStatus = "esperando"; deadline = System.currentTimeMillis() + 12000L; add("Posible caída (demo)"); render() }
    private fun fine() { incident = null; incidentStatus = ""; deadline = 0; add("Estoy bien confirmado"); render() }
    private fun acknowledge() { if (incident != null && incidentStatus != "esperando") { incidentStatus = "ayuda";add("La familia confirmó ayuda");render() } }
    private fun close() { incident = null;incidentStatus = "";deadline = 0;add("Evento cerrado");render() }
    private fun reset() { incident = null;incidentStatus = "";deadline = 0;history.clear();store();render() }

    private fun shape(color: Int, radius: Float = 20f): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = radius }

    private fun text(s: String, size: Float, color: Int = ink, bold: Boolean = false): TextView =
        TextView(this).apply {
            text = s; textSize = size; setTextColor(color); gravity = Gravity.CENTER
            if (bold) setTypeface(null, Typeface.BOLD)
        }

    private fun button(s: String, color: Int, onClick: () -> Unit): Button =
        Button(this).apply {
            text = s; textSize = 13f; isAllCaps = false; setTextColor(Color.WHITE)
            background = shape(color, 22f)
            setOnClickListener { onClick() }
        }

    private fun gap(root: LinearLayout, px: Int = 8) {
        root.addView(View(this), LinearLayout.LayoutParams(1, dp(px)))
    }
    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()
    private fun addButton(root: LinearLayout, label: String, color: Int, onClick: () -> Unit) {
        val b = button(label, color, onClick)
        root.addView(b, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)))
        gap(root, 7)
    }

    private fun render() {
        val scroll = ScrollView(this).apply { setBackgroundColor(bg); isFillViewport = true }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(24), dp(18), dp(24), dp(32))
        }
        scroll.addView(root)
        root.addView(text("✚ VitaCerca", 17f, Color.rgb(132, 225, 182), true))
        root.addView(text("PROTOTIPO · NO EMERGENCIAS", 9f, soft))
        gap(root, 12)
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL;gravity=Gravity.CENTER }
        val watchBtn = button("Pulsera", if(mode == "reloj") mint else Color.DKGRAY) { mode="reloj";store();render() }
        val familyBtn = button("Red de apoyo", if(mode == "familia") mint else Color.DKGRAY) { mode="familia";store();render() }
        row.addView(watchBtn, LinearLayout.LayoutParams(0,dp(39),1f))
        row.addView(familyBtn, LinearLayout.LayoutParams(0,dp(39),1.3f))
        root.addView(row)
        gap(root, 12)
        if(mode == "reloj") {
            val title = when {
                incident==null -> "ESTOY BIEN"
                incidentStatus=="esperando" -> "¿ESTÁS BIEN?"
                incidentStatus=="ayuda" -> "AYUDA CONFIRMADA"
                incident=="roja" -> "SOS ACTIVADO"
                else -> "ALERTA DE CAÍDA"
            }
            root.addView(text(title, 18f, if(incident==null) Color.rgb(117,224,166) else Color.rgb(255,175,153), true))
            val detail = if(incidentStatus=="esperando") {
                "Responde en ${((deadline-System.currentTimeMillis()+999)/1000).coerceAtLeast(0)} s"
            } else if(incidentStatus=="ayuda") "La red confirmó ayuda (demo)"
            else if(incident==null) "Sin alertas activas" else "Evento simulado"
            root.addView(text(detail, 11f, soft))
            gap(root)
            addButton(root, "✚ SOS", red) { startSOS() }
            addButton(root, "✓ ESTOY BIEN", mint) { fine() }
            addButton(root, "↘ Simular caída", Color.rgb(55,82,101)) { startFall() }
        } else {
            root.addView(text("RED DE APOYO", 16f, Color.rgb(117,224,166), true))
            root.addView(text("Marta Pérez · 76 años", 11f, soft))
            gap(root)
            val level = when(incident){null->"Sin alertas";"roja"->"ALERTA ROJA · SOS";else->"ALERTA NARANJA"}
            root.addView(text(level, 16f, if(incident==null) Color.rgb(117,224,166) else Color.rgb(255,175,153), true))
            val state = when(incidentStatus) {"esperando"->"Esperando respuesta del usuario";"ayuda"->"Ayuda confirmada (demo)";"activa"->"Requiere atención";else->"Estado simulado estable"}
            root.addView(text(state, 11f, soft))
            gap(root)
            if(incident != null && incidentStatus != "esperando") {
                if(incidentStatus != "ayuda") addButton(root, "✓ Voy en camino", mint) { acknowledge() }
                addButton(root, "Cerrar evento", Color.rgb(57,81,99)) { close() }
            } else if(incidentStatus=="esperando") {
                addButton(root, "Omitir espera", Color.rgb(57,81,99)) { incidentStatus="activa";add("Sin respuesta (demo)");render() }
            } else {
                addButton(root, "Simular SOS", red) { startSOS() }
            }
            root.addView(text("HISTORIAL (LOCAL)", 10f, soft, true))
            history.take(3).forEach { root.addView(text("• $it", 10f, soft)) }
        }
        gap(root, 3)
        val clear = button("↻ Reiniciar", Color.rgb(42,60,72)) { reset() }
        root.addView(clear, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(38)))
        setContentView(scroll)
    }
}

package com.omarea.vtools

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val shell by lazy { KeepShell(true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val root = ScrollView(this)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 48, 48, 48)
        }

        val gpuInfo = SceneJNI.getVulkanDeviceInfoMap()
        val freq = SceneJNI.getKernelPropLong("/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq")

        val header = TextView(this).apply {
            text = "Scene Reconstructed\nGPU: ${gpuInfo["deviceName"] ?: "Unknown"} (API: ${gpuInfo["deviceApi"]})\nCPU0 Freq: $freq kHz"
            textSize = 16f
            setPadding(0, 0, 0, 32)
        }
        container.addView(header)

        val toggles = listOf(
            "Show Background ANR" to "settings put secure anr_show_background",
            "Hide Error Dialogs" to "settings put global hide_error_dialogs",
            "Force Resizable Activities" to "settings put global force_resizable_activities",
            "Vulkan HWUI Renderer (skiavk)" to "setprop debug.hwui.renderer skiavk #"
        )

        for ((label, baseCmd) in toggles) {
            val sw = Switch(this).apply {
                text = label
                setPadding(0, 24, 0, 24)
                setOnCheckedChangeListener { _, isChecked ->
                    val state = if (isChecked) "1" else "0"
                    Thread { shell.doCmdSync("$baseCmd $state") }.start()
                }
            }
            container.addView(sw)
        }

        val actions = listOf(
            "Run FSTRIM" to "sm fstrim",
            "Compile Speed-Profile" to "cmd package compile -m speed-profile -a",
            "Compile Everything" to "cmd package compile -m everything -a"
        )

        for ((label, cmd) in actions) {
            val btn = Button(this).apply {
                text = label
                setOnClickListener {
                    Thread {
                        val res = shell.doCmdSync(cmd)
                        runOnUiThread {
                            Toast.makeText(this@MainActivity, res.ifEmpty { "Done" }, Toast.LENGTH_SHORT).show()
                        }
                    }.start()
                }
            }
            container.addView(btn)
        }

        root.addView(container)
        setContentView(root)
    }
}

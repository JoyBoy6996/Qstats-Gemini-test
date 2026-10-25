package com.omarea.vtools

import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

class KeepShell(useRoot: Boolean = true) {
    private var process: Process? = Runtime.getRuntime().exec(if (useRoot) "su" else "sh")
    private var stdin = DataOutputStream(process!!.outputStream)
    private var stdout = BufferedReader(InputStreamReader(process!!.inputStream))

    @Synchronized
    fun doCmdSync(cmd: String): String {
        val out = StringBuilder()
        val token = "--SCENE_EOF_${System.currentTimeMillis()}--"
        try {
            stdin.writeBytes("$cmd\necho $token\n")
            stdin.flush()
            var line: String?
            while (stdout.readLine().also { line = it } != null) {
                if (line == token) break
                out.append(line).append("\n")
            }
        } catch (e: Exception) {
            return "Error: ${e.message}"
        }
        return out.toString().trim()
    }
}

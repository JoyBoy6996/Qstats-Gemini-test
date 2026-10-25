package com.omarea.vtools

object SceneJNI {
    init {
        System.loadLibrary("native-lib")
    }
    external fun getKernelPropLong(path: String): Long
    external fun getVulkanDeviceInfoMap(): HashMap<String, String>
}

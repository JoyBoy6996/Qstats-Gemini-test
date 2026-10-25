#include <jni.h>
#include <cstdio>
#include <cstdlib>
#include <cstring>
#include <vulkan/vulkan.h>

extern "C" JNIEXPORT jlong JNICALL
Java_com_omarea_vtools_SceneJNI_getKernelPropLong(JNIEnv *env, jobject /* thiz */, jstring path) {
    const char *cpath = env->GetStringUTFChars(path, nullptr);
    if (!cpath) return -1;
    FILE *fp = fopen(cpath, "r");
    env->ReleaseStringUTFChars(path, cpath);
    if (!fp) return -1;
    long val = -1;
    fscanf(fp, "%ld", &val);
    fclose(fp);
    return val;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_omarea_vtools_SceneJNI_getVulkanDeviceInfoMap(JNIEnv *env, jobject /* thiz */) {
    jclass mapClass = env->FindClass("java/util/HashMap");
    jmethodID init = env->GetMethodID(mapClass, "<init>", "()V");
    jmethodID put = env->GetMethodID(mapClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    jobject hashMap = env->NewObject(mapClass, init);

    auto putStr = [&](const char *k, const char *v) {
        env->CallObjectMethod(hashMap, put, env->NewStringUTF(k), env->NewStringUTF(v));
    };

    VkApplicationInfo appInfo = {};
    appInfo.sType = VK_STRUCTURE_TYPE_APPLICATION_INFO;
    appInfo.pApplicationName = "vtools";
    appInfo.apiVersion = VK_API_VERSION_1_0;

    VkInstanceCreateInfo createInfo = {};
    createInfo.sType = VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO;
    createInfo.pApplicationInfo = &appInfo;

    VkInstance instance;
    if (vkCreateInstance(&createInfo, nullptr, &instance) != VK_SUCCESS) {
        putStr("error", "vkCreateInstance failed");
        return hashMap;
    }

    uint32_t deviceCount = 0;
    vkEnumeratePhysicalDevices(instance, &deviceCount, nullptr);
    if (deviceCount == 0) {
        putStr("error", "no physical device");
        vkDestroyInstance(instance, nullptr);
        return hashMap;
    }

    VkPhysicalDevice devices[deviceCount];
    vkEnumeratePhysicalDevices(instance, &deviceCount, devices);

    VkPhysicalDeviceProperties props;
    vkGetPhysicalDeviceProperties(devices[0], &props);

    char buf[128];
    putStr("deviceName", props.deviceName);
    snprintf(buf, sizeof(buf), "%u.%u.%u",
             VK_VERSION_MAJOR(props.apiVersion),
             VK_VERSION_MINOR(props.apiVersion),
             VK_VERSION_PATCH(props.apiVersion));
    putStr("deviceApi", buf);

    snprintf(buf, sizeof(buf), "0x%X", props.vendorID);
    putStr("vendorId", buf);

    snprintf(buf, sizeof(buf), "0x%X", props.deviceID);
    putStr("deviceId", buf);

    const char *typeStr = "Other";
    switch (props.deviceType) {
        case VK_PHYSICAL_DEVICE_TYPE_INTEGRATED_GPU: typeStr = "Integrated GPU"; break;
        case VK_PHYSICAL_DEVICE_TYPE_DISCRETE_GPU:   typeStr = "Discrete GPU"; break;
        case VK_PHYSICAL_DEVICE_TYPE_VIRTUAL_GPU:    typeStr = "Virtual GPU"; break;
        default: break;
    }
    putStr("deviceType", typeStr);

    vkDestroyInstance(instance, nullptr);
    return hashMap;
}

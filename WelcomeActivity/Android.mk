LOCAL_PATH := $(call my-dir)

# $(shell date +%Y-%m-%d > $(LOCAL_PATH)/assets/Version)

include $(CLEAR_VARS)

LOCAL_SRC_FILES := $(call all-java-files-under, src)

LOCAL_PACKAGE_NAME := Printer
LOCAL_CERTIFICATE := platform
#LOCAL_CERTIFICATE := shared

#LOCAL_REQUIRED_MODULES := libUsbSerial_jni
LOCAL_JNI_SHARED_LIBRARIES := libHardware_jni

#LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_MODULE_TAGS := optional
LOCAL_STATIC_JAVA_LIBRARIES := zxingcore corelib

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)

LOCAL_PREBUILT_STATIC_JAVA_LIBRARIES := zxingcore:libs/zxingcore.jar \
		corelib:libs/corelibrary.jar

include $(BUILD_MULTI_PREBUILT)

include $(call all-makefiles-under,$(LOCAL_PATH))

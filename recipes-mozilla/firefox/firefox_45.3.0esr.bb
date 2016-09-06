# Copyright (C) 2009-2015, O.S. Systems Software Ltda. All Rights Reserved
# Released under the MIT license (see packages/COPYING)

DESCRIPTION ?= "Browser made by mozilla"
DEPENDS += "alsa-lib curl startup-notification libevent cairo libnotify \
            virtual/libgl pulseaudio yasm-native icu"

LICENSE = "MPLv2 | GPLv2+ | LGPLv2.1+"
LIC_FILES_CHKSUM = "file://toolkit/content/license.html;endline=39;md5=f7e14664a6dca6a06efe93d70f711c0e"

SRC_URI = "https://archive.mozilla.org/pub/firefox/releases/${PV}/source/firefox-${PV}.source.tar.xz;name=archive \
           file://mozilla-firefox.png \
           file://mozilla-firefox.desktop \
           file://vendor.js \
           file://fix-python-path.patch \
           file://0001-Fix-a-broken-build-option-with-gl-provider.patch \
           file://0002-Fix-a-build-error-on-enabling-both-Gtk-2-and-EGL.patch \
           file://mozconfig-45esr \
           "

SRC_URI[archive.md5sum] = "616b65d9a6c053f6380d68655eb97c48"
SRC_URI[archive.sha256sum] = "922233c65c0aabd05371974c289495119c28d72fc7f8b06a22b58c5f70f8b8f7"

PR = "r0"
S = "${WORKDIR}/firefox-45.3.0esr"
# MOZ_APP_BASE_VERSION should be incremented after a release
MOZ_APP_BASE_VERSION = "45.3.0"

inherit mozilla

export MOZCONFIG = "${WORKDIR}/mozconfig-45esr"

EXTRA_OEMAKE += "installdir=${libdir}/${PN}-${MOZ_APP_BASE_VERSION}"

ARM_INSTRUCTION_SET = "arm"

MOZ_ENABLE_WAYLAND ??= "${@base_contains('DISTRO_FEATURES', 'wayland', '1', '0', d)}"
EXTRA_OECONF += "${@base_conditional('MOZ_ENABLE_WAYLAND', '1', \
             '--enable-default-toolkit=cairo-gtk3 --with-gl-provider=EGL', \
             '--enable-default-toolkit=cairo-gtk2', \
             d)}"
DEPENDS += "${@base_conditional('MOZ_ENABLE_WAYLAND', '1', 'gtk+3', '', d)}"
SRC_URI += "${@base_conditional('MOZ_ENABLE_WAYLAND', '1', \
           'file://wayland-patches/0001-Initial-patch-from-https-stransky.fedorapeople.org-f.patch \
            file://wayland-patches/0002-gdk_x11_get_server_time-fix.patch \
            file://wayland-patches/0003-Fixed-gdk_x11_get_server_time-for-wayland.patch \
            file://wayland-patches/0004-Install-popup_take_focus_filter-to-actual-GdkWindow.patch \
            file://wayland-patches/0005-Fixed-nsWindow-GetLastUserInputTime.patch \
            file://wayland-patches/0008-GLLibraryEGL-Use-wl_display-to-get-EGLDisplay-on-Way.patch \
            file://wayland-patches/0009-Use-wl_egl_window-as-a-native-EGL-window-on-Wayland.patch \
            file://wayland-patches/0010-Disable-query-EGL_EXTENSIONS.patch \
            file://wayland-patches/0011-Wayland-Detect-existence-of-wayland-libraries.patch \
            file://wayland-patches/0012-Add-AC_TRY_LINK-for-libwayland-egl.patch \
            file://wayland-patches/0013-Wayland-Resize-wl_egl_window-when-the-nsWindow-is-re.patch \
           ', \
           '', d)}"

do_install_append() {
    install -d ${D}${datadir}/applications
    install -d ${D}${datadir}/pixmaps

    install -m 0644 ${WORKDIR}/mozilla-firefox.desktop ${D}${datadir}/applications/
    install -m 0644 ${WORKDIR}/mozilla-firefox.png ${D}${datadir}/pixmaps/
    install -m 0644 ${WORKDIR}/vendor.js ${D}${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/defaults/pref/

    # Fix ownership of files
    chown root:root -R ${D}${datadir}
    chown root:root -R ${D}${libdir}
}

FILES_${PN} = "${bindir}/${PN} \
               ${datadir}/applications/ \
               ${datadir}/pixmaps/ \
               ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/* \
               ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/.autoreg \
               ${bindir}/defaults"
FILES_${PN}-dev += "${datadir}/idl ${bindir}/${PN}-config ${libdir}/${PN}-devel-*"
FILES_${PN}-staticdev += "${libdir}/${PN}-devel-*/sdk/lib/*.a"
FILES_${PN}-dbg += "${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/.debug \
                    ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/*/.debug \
                    ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/*/*/.debug \
                    ${libdir}/${PN}-${MOZ_APP_BASE_VERSION}/*/*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/*/.debug \
                    ${libdir}/${PN}-devel-*/*/*/*/.debug \
                    ${bindir}/.debug"

# We don't build XUL as system shared lib, so we can mark all libs as private
PRIVATE_LIBS = "libmozjs.so \
                libxpcom.so \
                libnspr4.so \
                libxul.so \
                libmozalloc.so \
                libplc4.so \
                libplds4.so \
                liblgpllibs.so \
                libmozgtk.so"

# mark libraries also provided by nss as private too
PRIVATE_LIBS += " \
    libfreebl3.so \
    libnss3.so \
    libnssckbi.so \
    libsmime3.so \
    libnssutil3.so \
    libnssdbm3.so \
    libssl3.so \
    libsoftokn3.so \
"
#!/bin/bash
set -e

# Set ANDROID_NDK_HOME here
ANDROID_NDK_HOME=/home/fakelog/Android/Sdk/ndk/27.0.12077973/

if [ -z "$ANDROID_NDK_HOME" ]; then
    echo "Error: ANDROID_NDK_HOME is not set."
    exit 1
fi

API_LEVEL=24

LIBVPX_PATH="$(pwd)/third_party/libvpx"
DIST_DIR="$(pwd)/libvpx_build"

HOST_OS=$(uname -s | tr '[:upper:]' '[:lower:]')
PREBUILT_HOST="${HOST_OS}-x86_64"

echo "Using NDK: $ANDROID_NDK_HOME"
echo "Build Platform: $PREBUILT_HOST"

build_vpx() {
    ABI=$1
    TARGET_NAME=$2

    echo "========================================"
    echo "Building for $ABI ($TARGET_NAME)..."
    echo "========================================"

    PREFIX="$DIST_DIR/$ABI"

    TOOLCHAIN="$ANDROID_NDK_HOME/toolchains/llvm/prebuilt/$PREBUILT_HOST"

    case $ABI in
        arm64-v8a)
            ARCH_TRIPLE="aarch64-linux-android"
            EXTRA_CFLAGS="-O3 -march=armv8-a"
            ;;
        armeabi-v7a)
            ARCH_TRIPLE="armv7a-linux-androideabi"
            EXTRA_CFLAGS="-Os -march=armv7-a -mfloat-abi=softfp -mfpu=neon -mthumb"
            ;;
        x86_64)
            ARCH_TRIPLE="x86_64-linux-android"
            EXTRA_CFLAGS="-O3 -march=x86-64 -msse4.2 -mpopcnt -m64 -fPIC"
            EXTRA_CONFIGURE_FLAGS="--disable-mmx --disable-sse --disable-sse2 --disable-sse3 --disable-ssse3 --disable-sse4_1 --disable-avx --disable-avx2"
            ;;
        x86)
            ARCH_TRIPLE="i686-linux-android"
            EXTRA_CFLAGS="-O3 -march=i686 -msse3 -mfpmath=sse -m32 -fPIC"
            ;;
    esac

    CC_BIN="$TOOLCHAIN/bin/${ARCH_TRIPLE}${API_LEVEL}-clang"
    CXX_BIN="$TOOLCHAIN/bin/${ARCH_TRIPLE}${API_LEVEL}-clang++"

    export CC="$CC_BIN"
    export CXX="$CXX_BIN"
    export AS="$CC_BIN"
    export LD="$CC_BIN"
    export AR="$TOOLCHAIN/bin/llvm-ar"
    export STRIP="$TOOLCHAIN/bin/llvm-strip"
    export NM="$TOOLCHAIN/bin/llvm-nm"

    cd "$LIBVPX_PATH"

    make clean > /dev/null 2>&1 || true

    ./configure \
        --target=$TARGET_NAME \
        --prefix="$PREFIX" \
        --disable-examples \
        --disable-tools \
        --disable-docs \
        --disable-unit-tests \
        --enable-pic \
        --enable-static \
        --disable-shared \
        --enable-small \
        --enable-realtime-only \
        --enable-vp8 \
        --enable-vp9 \
        --disable-vp8-encoder \
        --disable-vp9-encoder \
        --disable-webm-io \
        --force-target=android \
        --extra-cflags="-fPIC $EXTRA_CFLAGS" \
        $EXTRA_CONFIGURE_FLAGS

    make -j$(nproc) install

    cd ..
}

build_vpx "arm64-v8a" "arm64-android-gcc"
build_vpx "armeabi-v7a" "armv7-android-gcc --enable-neon --disable-neon-asm"
build_vpx "x86_64" "x86_64-android-gcc"

echo ""
echo "SUCCESS! Libs are in $DIST_DIR"
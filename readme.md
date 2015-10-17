# [FL3D](https://github.com/floatlearning/fl3d) [![Build Status](https://travis-ci.org/floatlearning/fl3d.svg?branch=master)](https://travis-ci.org/floatlearning/fl3d)

FL3D is a lightweight 3D engine for Android devices supporting [OpenGL ES 2.0](https://www.khronos.org/opengles/2_X/). The focus of this library is to abstract away some of the obscure commands required for OpenGL rendering, without sacrificing performance on low-power devices. This project is designed for [Android Studio](http://developer.android.com/tools/studio/index.html) and uses the standard [Gradle](http://gradle.org/) build system with some additional functionality in separate modules.

# About

FL3D was designed to support simple 3D rendering in custom applications where a complete out-of-the-box solution such as [Unity 3D](http://unity3d.com/) or [libGDX](https://github.com/libgdx/libgdx) might be overkill. The intention is to provide a means to add a small amount of efficient 3D rendering (such as in a single view) to an Android application without having to learn OpenGL.

This was originally intended to be used for a project, but was set aside in favor of another approach. We opted to open-source it in the hopes that it could be useful to others, but we have no long-term maintenance in mind for this project. That said, feel free to [open an issue](/../../issues/) if you notice a glaring issue, or contact this project's developer ([Steve Richey](https://github.com/steverichey)) if you have a question.

# Modules

This repository contains several modules, namely:

* app: A small test application which uses FL3D to render the device camera to an OpenGL texture. If you import this entire repository into Android Studio, this is the module that will be built and deployed.
* fileio: A single class providing some simple file manipulation tools, primarily intended for saving images to file while handling issues that may arise during I/O operations.
* fl3d: The core FL3D module. All other modules provide additional functionality that you may find useful, but only this one is required. This module has no dependencies.
* glcameraview: An implementation of a simple `FL3DSurfaceView` which receives texture data from the device camera.
* threadedcamera: Runs the device camera in a dedicated thread, with some optimizations and convenience methods we've developed.

# License

&copy; 2015 [Float Mobile Learning](http://floatlearning.com/). Shared under an [MIT license](https://en.wikipedia.org/wiki/MIT_License). See [license.md](./license.md) for details.

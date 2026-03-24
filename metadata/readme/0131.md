# ApkBuilder

- ApkBuilder is a lightweight Android Project builder CLI.

# Documentation

- See project.yml docs at [Docs](https://github.com/silvadev13/ApkBuilder/blob/main/docs/config.rst)

## Features

- [x] APK Compilation
- [ ] AAB Support
- [x] Java
- [x] Kotlin
- [ ] R8 / ProGuard
- [x] ViewBinding
- [ ] Jetpack Compose (not supported yet)

## System dependencies

- `python`
- `aapt2`
- `javac`
- `kotlinc`
- `d8`
- `apksigner`

## Python dependencies

- `pip install pyyaml`

## Building an APK

To compile your project, use the builder module:

example:

```
python -m cli.builder example/
```

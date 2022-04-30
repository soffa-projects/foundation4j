# Changelog

Tous les changements notables du projet sont documentés dans ce fichier :
All notable changes to this project will be documented in this file.

Le format utilisé se base sur [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
et le versioning du projet respect les règles  [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [0.10.2] - 2022-04-30

### Added

- Check JWT shape before proceeding to then actual validation

## [0.10.0] - 2022-04-29

### OpenTelemetry integration

## [0.9.15] - 2022-04-25

### Added

- More ID generator algorithms

## [0.9.11] - 2022-04-25

### Changed

- Columns and tables names escaping (PostgreSQL)

## [0.9.9] - 2022-04-22

### Changed

- Update IDs alrogithm

## [0.9.8] - 2022-04-22

### Changed

- Dependencies update
 
## [0.9.7] - 2022-04-21

### Changed

- Use full version of UUID to avoid collisions when generating ids.
 
## [0.9.6] - 2022-04-20

### Added

- Ping database on bootstrap

### Changed

- AuthManager lazy loading

## [0.9.5] - 2022-04-16

### Fixed

- ClassLoader when generated dynamic resource implementation

### Security

- Various dependencies update

## [0.9.4] - 2022-03-13

### Added

- `@Feature` annotation for feature activations
- Custom properties are now documented in `additional-spring-configuration-metadata`
- Applications must now use `application-default.yml` instead of `application.yml`

## [0.9.3] - 2022-03-13

### Added

- Spring boot api subset

### Removed

- Samples are now in a dedicated repository https://github.com/soffalabs/foundation4j-samples

## [0.9.2] - 2022-03-09

### Added

- `UseCase` annotation added
- `BindOperation` renamed to `Bind`

## [0.9.1] - 2022-03-10

### Added

- Reboot

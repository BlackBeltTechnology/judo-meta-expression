# AGENTS.md — `osgi-itest/src/test/resources/etc`

Pax Exam container configuration files for logging and Maven artifact resolution.

| File | Purpose |
| --- | --- |
| `org.ops4j.pax.logging.cfg` | Pax Logging configuration configuring Log4j2 appenders (Console, RollingFile, PaxOsgi) and root log levels for Karaf OSGi integration tests. |
| `org.ops4j.pax.url.mvn.cfg` | Pax URL Maven configuration defining local repository paths, remote repositories, SSL certificate checks, and settings.xml resolution. |

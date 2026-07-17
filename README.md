# Sean's Surf & Skate Co. — Seal Security Demo (Maven / Java)

A small but real-looking e-commerce storefront ("Sean's Surf & Skate Co.") built with
Spring Boot. It ships with **known-vulnerable open-source dependencies** so it can be used
to demonstrate how [Seal Security](https://www.sealsecurity.io) remediates CVEs with
backported "sealed" patches — **without upgrading a single package version.**

## The vulnerability

The storefront's newsletter / "Deal Alerts" sign-up runs the submitted value through
SnakeYAML's `Yaml.load()`:

```java
Yaml yaml = new Yaml();
Object parsed = yaml.load(name);   // untrusted input
```

`org.yaml:snakeyaml:1.33` is vulnerable to **CVE-2022-1471** (arbitrary object
instantiation on untrusted input). Pasting a crafted SnakeYAML payload into the sign-up
field achieves remote code execution and replaces the store with a **"You've Been PWNED"**
page. The exploit payload lives in the companion repo
[`yaml-payload`](https://github.com/seal-sean-org/yaml-payload).

The `pom.xml` also pins several other intentionally-vulnerable libraries (jackson-databind,
commons-text / Text4Shell, log4j-core / Log4Shell, spring-core, and more) so a scan lights
up with real findings.

## Run it locally

```bash
mvn clean package
java -jar target/maven-demo-1.0.0.jar
# open http://localhost:8080
```

To reach it from a browser over a public URL (e.g. for a recorded demo), expose port 8080
with ngrok: `ngrok http 8080`.

## Remediating with Seal

This repo is committed in its **vulnerable, pre-remediation state on purpose** — there is
no Seal step in CI yet. In the demo, Seal's GitHub integration opens a pull request that
adds the remediation workflow; once merged (and `SEAL_TOKEN` is set as an Actions secret),
the build applies sealed patches (e.g. `snakeyaml 1.33 → 1.33+sp1`) at build time. Same
versions on the surface, same passing tests — CVEs gone.

## CI secrets (optional, for the GitHub Actions run path)

| Secret | Purpose |
| --- | --- |
| `SEAL_TOKEN` | Seal artifact-server / CLI access token (Production) |
| `NGROK_TOKEN` | ngrok auth token, to expose the running app from CI |
| `NGROK_DOMAIN` | your reserved ngrok domain (e.g. `your-name.ngrok-free.dev`) |

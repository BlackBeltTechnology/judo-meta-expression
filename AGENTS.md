# judo-meta-expression — module agent doctrine

## Module purpose

`judo-meta-expression` owns the estate's *expression* metamodel — the typed,
metamodel-independent tree that a validated JQL statement is compiled into
before any runtime (RDBMS query, mapper, validator) ever sees it. The Ecore
model (`nsURI http://blackbelt.hu/judo/meta/expression`) roots everything at the
abstract `Expression` interface and splits it into twelve subpackages —
`constant`, `variable`, `operator`, `numeric`, `logical`, `string`,
`enumeration`, `object`, `collection`, `custom`, `temporal`, `binding` — each
with its own nsURI. What the trees express is a *statically typed* value
computation: the type is carried by the node class, not by a runtime tag, so
`IntegerArithmeticExpression`, `DecimalRoundExpression`, `StringConstant`,
`Literal`, `CountExpression`, `IterableExpression`/`FilteringExpression`/
`WindowingExpression`, `ExtractDateExpression`, `SwitchExpression` +
`SwitchCase`, and the `Sequence`/`StaticSequence`/`ObjectSequence` family each
implement exactly the marker interfaces (`IntegerExpression`,
`DecimalExpression`, `LogicalExpression`, `StringExpression`, `DateExpression`,
`TimestampExpression`, `TimeExpression`, `ObjectExpression`,
`CollectionExpression`, `CustomExpression`) that describe the value they yield.
Operators are closed enumerations (`IntegerOperator`, `DecimalOperator`,
`LogicalOperator`, `NumericComparator`, `StringComparator`, `ObjectComparator`,
`EnumerationComparator`, `TemporalOperator`, `SequenceOperator`, and the
`*Aggregator` enums), so an illegal operator is unrepresentable rather than
merely invalid. Named references into the *underlying* data model are indirected
through `ElementName` and its concrete `TypeName` / `MeasureName` /
`TypeNameExpression`, never through direct EMF references — that indirection is
what makes the metamodel reusable across ASM, PSM and ESM.

The `binding` subpackage is the join back to a concrete model: `Binding` and its
`AttributeBinding` / `ReferenceBinding` / `FilterBinding` subclasses, tagged by
`AttributeBindingRole` and `ReferenceBindingRole`, declare which expression
computes which attribute, reference, or filter of which named type — i.e. the
expression model is not a free-floating AST, it is an assignment of expressions
to model features.

Metamodel independence is enforced by one type:
`hu.blackbelt.judo.meta.expression.adapters.ModelAdapter<NE, P extends NE, E
extends P, C extends NE, PTE, RTE, TO extends NE, TA, TR, S, M, U>` — twelve
type parameters (namespace element, primitive/enumeration/complex type,
primitive and reference typed elements, transfer object + its attributes and
relations, sequence, measure, unit). `ModelAdapter` is declared in the `.ecore`
itself as an `EClass` with `instanceClassName` pointing at that interface, so
the model can hold an adapter without depending on any concrete metamodel;
`judo-meta-expression-asm`, `-psm` and `-esm` each supply an implementation.

What it ships beyond the metamodel: the EMF-generated Java API in `src-gen/`
(regenerated from `expression.genmodel` by an MWE2 workflow); the hand-written
runtime `ExpressionUtils` (Guava `LoadingCache`-backed model queries),
`ExpressionEvaluator`, `ExpressionModel` loading, and the two validation
engines — Epsilon EVL (16 `.evl` scripts, one per subpackage concern, driven by
`ExpressionEpsilonValidatorExecutor` / `ReflectiveExpressionValidatorFactory`)
and the Java/Zeta rules under `validation/rules/` fronted by
`ExpressionZetaValidator`; the `MeasureAdapter` layer that makes numeric and
temporal expressions unit-aware; the `ExpressionTransformer` JQL front end; and
three delivery shapes — Eclipse plugins, Eclipse features + P2 site, and a
standalone OSGi bundle.

**Repository:** BlackBeltTechnology/judo-meta-expression ·
**Artifact:** `hu.blackbelt.judo.meta:hu.blackbelt.judo.meta.expression`
(packaging `pom`, version `${revision}` = `1.0.5-SNAPSHOT`) ·
**License:** EPL-2.0 · **Java:** 21 · **Build:** Maven 3.9.9+ / Eclipse Tycho 4.0.13.

## Reactor map

The root `pom.xml` declares its `<modules>` inside the `modules` profile, which
is active unless `-DskipModules=true` is passed. Build order is the declared
order — `model` produces the artifact every other module consumes.

<modules>
  <module>model</module>
  <module>adapter-measure</module>
  <module>builder-jql</module>
  <module>model-test</module>
  <module>osgi</module>
  <module>osgi-itest</module>
  <module>feature-model</module>
  <module>feature-builder-jql</module>
  <module>feature-adapter-measure</module>
  <module>site</module>
</modules>

| Module | Artifact / packaging | What it contributes |
|---|---|---|
| `model` | `…expression.model`, `eclipse-plugin` | The metamodel and everything derived from it: `model/expression.ecore` + `.genmodel`, the MWE2 workflow that regenerates `src-gen/`, the `ModelAdapter` contract, the 16 EVL constraint scripts, and the hand-written runtime (`ExpressionUtils`, `ExpressionEvaluator`, `ExpressionValidator`, `ExpressionEpsilonValidatorExecutor`, `ExpressionZetaValidator` and the `validation/rules/` Zeta rule set). Every other reactor module consumes or repackages this one. |
| `adapter-measure` | `…expression.model.adapter.measure`, `eclipse-plugin` | Makes numeric and temporal expressions unit-aware. `MeasureAdapter` resolves a `MeasureName` and unit against a pluggable `MeasureProvider` (satisfied by `judo-meta-measure`), computes the dimension of an arithmetic tree as base measures with exponents, decides unit convertibility, and handles duration measures for temporal arithmetic; `MeasureChangedHandler` invalidates its caches when the measure model changes underneath it. |
| `builder-jql` | `…expression.builder.jql`, `eclipse-plugin` | The JQL front end. `ExpressionTransformer` walks a `judo-meta-jql` AST and emits Expression metamodel instances, dispatching through per-category function transformers (41 `*Transformer` classes under `function/collection`, `function/temporal`, `function/string`, `function/numeric`, `function/object`, `function/variable`, plus `operation/`, `constant/`, `expression/`). `ExpressionBuildingVariableResolver` supplies lambda/variable scoping during the walk and `ExpressionMeasureProvider` supplies measure resolution. |
| `model-test` | `…expression.model.test`, `jar` | JUnit 5 proof that the runtime behaves, run against the published API rather than internals: `ExpressionValidatorTest`, the per-subpackage `validation/mock/*ValidationTest` suites built on `MockModelAdapter`, `MeasureAdapterTest` / `MeasureChangeAdapterTest` over `DummyMeasureProvider`, and `ExpressionValidationPerformanceTest`. `MinimalExpressionFactory` and `ExpressionModelForTest` construct fixtures. |
| `osgi` | `…expression.osgi`, `bundle` (Felix) | Repackages `model` for non-Eclipse OSGi containers. The bundle plugin embeds `../model/model` as `meta/expression` and `../model/src/main/epsilon/validations` as `validations`, and ships `ExpressionModelBundleTracker`, which discovers bundles carrying an `Expression-Models` manifest header and registers their models as OSGi services. |
| `osgi-itest` | `…expression.osgi.itest`, `jar` | Pax Exam integration tests that boot an Apache Karaf 4.4.7 container and assert the bundle actually resolves there — `ExpressionBundleITest` checks bundle activation and service registration, the check unit tests structurally cannot make. |
| `feature-model` | `…expression.feature`, `eclipse-feature` | Installable Eclipse feature wrapping the core `model` plugin. Pure descriptor module — no sources. |
| `feature-builder-jql` | `…expression.builder.jql.feature`, `eclipse-feature` | Installable Eclipse feature wrapping the `builder-jql` plugin. Pure descriptor module — no sources. |
| `feature-adapter-measure` | `…expression.adapter.measure.feature`, `eclipse-feature` | Installable Eclipse feature wrapping the `adapter-measure` plugin. Pure descriptor module — no sources. |
| `site` | `…expression.site`, `eclipse-repository` | Builds the P2 update site aggregating all three features, published for "Install New Software" and consumed as a P2 repository by downstream Tycho builds. Its category P2 URLs carry hardcoded versions (see `update-category-versions`). |

## Build commands

A Maven wrapper is present (`./mvnw`); prefer it over a repo-root `mvn`.

```bash
# Full build (requires JDK 21, Maven 3.9.9+)
mvn clean install

# Run unit tests only
mvn test

# Run a single test class
mvn test -pl model-test -Dtest=ExpressionValidatorTest

# Full verification including OSGi integration tests
mvn verify

# Update site category versions
mvn clean install -P update-category-versions -f site/pom.xml
```

> **Note:** Maven Wrapper is available: `./mvnw clean install`

### Maven profiles

| Profile | Purpose |
|---------|---------|
| `modules` | Activates all submodules (default when `skipModules` is not set). Disable with `-DskipModules=true`. |
| `sign-artifacts` | GPG signs artifacts for release |
| `release-dummy` | Deploys to a dummy local repository |
| `release-judong` | Deploys to internal Nexus (nexus.judo.technology) |
| `release-central` | Deploys to Maven Central with Javadoc and source JARs |
| `update-category-versions` | Update site category P2 repository URLs (not active by default) |
| `generate-github-asciidoc-diagrams` | Generates diagram images for GitHub rendering |
| `update-source-code-license` | Updates license headers across source files |
| `list-ius` | Lists resolved Eclipse installable units; activated by `-DlistEclipseUnits=true` |

## Technology stack

### Core technologies
- **Eclipse EMF** (2.21+) — metamodel definition and code generation from Ecore
- **Eclipse Tycho** (4.0.13) — Maven integration for Eclipse plugin builds
- **Epsilon Runtime** (2.8.0) — EVL (Epsilon Validation Language) for model constraint validation
- **Xtext** (2.39.0) — DSL processing support
- **MWE2** (2.13.0) — Modeling Workflow Engine for code generation
- **OSGi** (Apache Felix) — modular runtime framework
- **Guava** (30.0-jre) — caching (`LoadingCache` in `ExpressionUtils`) and collections

### Build & quality
- **Maven** 3.9.9+ with Maven Wrapper (`mvnw`)
- **JUnit 5** (Jupiter) — unit testing
- **Mockito** — mocking in tests
- **PAX Exam** (4.13.5) + **Apache Karaf** (4.4.7) — OSGi integration testing
- **JaCoCo** — code coverage
- **SonarQube** — code quality analysis (sonar.judo.technology)
- **Logback** (1.5.12) + **SLF4J** (2.0.16) — logging
- **Lombok** (1.18.34) — used only in non-Tycho modules
- **Judo Zeta** validation framework (`annotations`, `common`, `validation-core`)

### External metamodel dependencies
- **judo-meta-measure** — measure/unit metamodel (consumed by `adapter-measure`)
- **judo-meta-jql** — JQL query language metamodel (consumed by `builder-jql`)

## Architecture pointers

- `model/model/expression.ecore` — the metamodel: `Expression` root plus the twelve subpackages, 31+ top-level EClasses, the `ModelAdapter` `EClass` with `instanceClassName`, and the `LocalDate` / `LocalDateTime` / `LocalTime` EDataTypes.
- `model/model/expression.genmodel` — controls EMF Java code generation parameters.
- `model/src/workflow/generateModel.mwe2` — MWE2 workflow driving EMF code generation into `src-gen/`.
- `model/src/main/epsilon/validations/expression/*.evl` — the 16 EVL constraint scripts (`expression`, `attribute`, `attributeBinding`, `referenceBinding`, `filterBinding`, `collection`, `constant`, `custom`, `enumeration`, `logical`, `measured`, `numeric`, `object`, `string`, `temporal`, `_importExpression`).
- `model/src/main/java/.../adapters/ModelAdapter.java` — the twelve-parameter metamodel-independence contract; read this before touching any downstream adapter.
- `model/META-INF/MANIFEST.MF` — Eclipse/OSGi bundle manifest for the core model plugin: exported packages and `Require-Bundle` dependencies.
- `osgi/pom.xml` (`maven-bundle-plugin` `<instructions>`) — OSGi bundle manifest generation and the `Include-Resource` embedding of model + validations.
- The root `pom.xml` is the aggregator and parent POM: `${revision}` version property, dependency management, plugin management, and every profile listed above.
- `logback-test.xml` at the repo root is the shared test logging configuration.
- [README.md](README.md) — project overview and quick start.
- [CONTRIBUTING.md](CONTRIBUTING.md) — contribution guidelines, code style, build verification, and the Tycho dependency rules.
- [.github/CIFLOW.md](.github/CIFLOW.md) — CI/CD pipeline and branching strategy.
- `docs/validation/` — Java (Zeta) and Epsilon validation framework guides.

## Development environment

**Required:**
- Java 21 JDK (recommended: Zulu distribution via SdkMan)
- Maven 3.9.9+

**Recommended:**
- SdkMan for Java version management: `sdk use java 21.x.y-zulu`
- Eclipse IDE with: m2e, Epsilon, Modeling Tools, XTend, XText, MWE/MWE2

## Git workflow

- **Main Branch:** `develop`
- **Production Branch:** `master`
- **Versioning:** `1.0.5-SNAPSHOT` (current), semantic versioning with GitFlow
- **Branch Naming:** `feature/JNG-XXX_summary`, `bugfix/JNG-XXX_summary`, `release/X.Y-betaN`, `hotfix/JNG-XXX_summary`
- **Commit Format:** Conventional Commits (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`)
- **Requirement:** Every commit must reference a JIRA ticket (`JNG-XXX`)

## Scope guard — invariants an edit must not break

1. **Never edit `src-gen/` directories** — these contain EMF-generated code that is regenerated on every build. Hand-written code belongs in `src/main/java/`.
2. **Do not modify build configuration** (`pom.xml`, `feature.xml`, `site.xml`, `MANIFEST.MF`) without explicit human approval.
3. **Tycho vs Maven dependencies** — Tycho plugin-to-plugin dependencies go in `MANIFEST.MF` (`Require-Bundle`), not in `pom.xml`. See the dependency rules in [CONTRIBUTING.md](CONTRIBUTING.md).
4. **Version duality** — Maven uses `-SNAPSHOT`, Eclipse uses `.qualifier`. Tycho reconciles these automatically during the build; never hand-sync them.
5. **Lombok is not used in Eclipse plugins** — Tycho does not support Lombok generation. All Eclipse plugin source is generated or hand-written.
6. **Expression metamodel packages** are fixed: `binding`, `collection`, `constant`, `custom`, `enumeration`, `logical`, `numeric`, `object`, `operator`, `string`, `temporal`, `variable` — each in its own EMF subpackage with its own nsURI under `hu.blackbelt.judo.meta.expression`.
7. **`ModelAdapter` is the metamodel-independence seam** — twelve type parameters, implemented by downstream adapters (`-asm`, `-psm`, `-esm`) to connect expressions to specific model types. Changing its signature breaks every adapter in the estate.
8. **`Expression-Models` manifest header** — bundles carrying it are discovered by `ExpressionModelBundleTracker` and registered as OSGi services. The header name is a contract with downstream bundles.

## Code instructions

1. First think through the problem, read the codebase for relevant files.
2. Before you make any major changes, check in with me and I will verify the plan.
3. Please every step of the way just give me a high level explanation of what changes you made.
4. Make every task and code change you do as simple as possible. We want to avoid making any massive or complex changes. Every change should impact as little code as possible. Everything is about simplicity.
5. Maintain a documentation file that describes how the architecture of the app works inside and out.
6. Never speculate about code you have not opened. If the user references a specific file, you MUST read the file before answering. Make sure to investigate and read relevant files BEFORE answering questions about the codebase. Never make any claims about code before investigating unless you are certain of the correct answer - give grounded and hallucination-free answers.
7. For implementation use TDD (Test-Driven Development): write or update tests first to define the expected behaviour, verify they fail, then write the minimal implementation to make them pass.
8. Use DRY (Don't Repeat Yourself): extract reusable logic into separate classes, utilities, or components. If the same pattern appears in multiple places, refactor it into a shared helper.

<!-- dox-doctrine -->
## Documentation Update Protocol (WRITE discipline)

Per-directory `AGENTS.md` files form a tree. Each directory `AGENTS.md` is the
per-file record for the files in that directory. This module-root `AGENTS.md`
holds doctrine + architecture pointers only — never a per-file index.

**Keep the root lean.** This file loads into every agent turn — every byte costs
tokens on every turn. A verbose root file buries the rules the model must follow
(signal dilution) and measurably degrades adherence; a lean file keeps doctrine
salient. Default assumption: your update does NOT belong in the root — route it
by the table below.

**Route every doc update by kind:**

| Kind of update | Goes in |
|---|---|
| New file in a directory, or its per-file detail / change history | Nearest directory `AGENTS.md`. Add a `` | `<basename>` | <purpose> | `` row, path-alphabetical. |
| Data flow, protocol, architecture rationale | `docs/architecture.md` or a `docs/<topic>.md` |
| End-user / developer setup | `README.md` |
| Cross-cutting rule every agent needs every turn (rare) | this module-root `AGENTS.md` |

**Read before editing (chain walk).** Before editing a file, read the nearest
`AGENTS.md` chain root→leaf so you know the file's recorded purpose, contracts,
and change history. Do not edit blind.

**Update after editing (closeout pass).** After changing a file, update its row
in the nearest directory `AGENTS.md`: find the file's row, update its purpose in
place; if absent, add it in path-alphabetical order. New directory → scaffold
its `AGENTS.md`. One row per file. The purpose carries a one-line summary, key
exported symbols, contracts/invariants, and `See change: <id>` history.

**Row style (caveman).** Short declarative fragments. Drop articles. Subject →
verb → object, present tense. One fact per row. Prefer concrete tokens (paths,
symbols, env vars) over prose. Keep identifiers verbatim.

**Size rule — split an over-large directory `AGENTS.md` file-based.** pi
auto-injects a directory `AGENTS.md` on every turn when cwd sits at/below it, so
an over-large directory `AGENTS.md` is not supported. Split it file-based: a row
exceeding the length threshold promotes to a per-file `<File>.AGENTS.md`
sidecar carrying that file's full detail (including every `See change:`). The
sidecar is pull-only — its name is not `AGENTS.md`, so pi never auto-injects it
— yet it stays search-indexed (`agents` doc_type). The directory `AGENTS.md`
keeps a one-line summary plus a `→ see `<File>.AGENTS.md`` pointer. Rows within
the threshold stay verbatim (lossless).

## Finding docs (READ discipline)

`kb_*` tools are faster and cheaper than raw search — they return a one-line
purpose + key exports per file, not raw bytes. **This fires on the ACTION, not
the intent** — before you `grep`/`rg` for a symbol, `cat`/read a file to learn
what it does, or chase an import, the kb call goes first. It fires **even
mid-task when you already know the file**; knowing the file does not exempt you.
When your reflex is the left column, run the right column instead:

| You're about to… | Do this FIRST instead |
|---|---|
| `grep -rn "SymbolName" src/` — find where a fn / type / const lives | `kb_search --doc-type agents "SymbolName"` — tree indexes key exports per file |
| `grep -rn "feature\|topic" src/` — how does X work / where's X handled | `kb_search "feature topic"` |
| `cat` / read a file just to learn its purpose before editing | `kb agents <path>` — one-line purpose + exports + change history |
| chase imports / callers across files | `kb_neighbors <path\|heading>` |
| read one doc section in full | `kb_get <path> <section>` |

**Fall-through (explicit):** if the kb call returns nothing relevant, `rg` /
source read is allowed — then add the missing directory `AGENTS.md` row per the
WRITE discipline. kb does NOT replace grep; it goes first.

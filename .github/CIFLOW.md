# CI/CD Flow and Branch Strategy

This document describes the Git branching model and CI/CD pipeline for the judo-meta-expression project. The versioning policy follows [GitFlow](https://www.atlassian.com/git/tutorials/comparing-workflows/gitflow-workflow).

## Branches

The project maintains several branch types, each serving a distinct purpose in the release lifecycle:

```mermaid
gitDiagram
    commit id: "init"
    branch develop
    checkout develop
    commit id: "dev-1"
    branch feature/JNG-1
    commit id: "feat-1a"
    commit id: "feat-1b"
    checkout develop
    merge feature/JNG-1
    branch feature/JNG-2
    commit id: "feat-2a"
    checkout develop
    merge feature/JNG-2
    branch release/1.0-beta1
    commit id: "rc-1"
    branch bugfix/JNG-4
    commit id: "fix-4"
    checkout release/1.0-beta1
    merge bugfix/JNG-4
    checkout master
    merge release/1.0-beta1 id: "v1.0"
    checkout develop
    merge release/1.0-beta1
```

| Branch Pattern | Base | Purpose |
|---------------|------|---------|
| `develop` | — | Main development branch; contains the latest sources for the active version |
| `feature/JNG-XXX_summary` | `develop` | New features for the active version |
| `release/X.Y-betaN` | `develop` | Release stabilization branches (the `release/` prefix is reserved for CI) |
| `bugfix/JNG-XXX_summary` | `release/*` | Bug fixes applied to release branches; must also be applied to newer release and develop branches |
| `support/JNG-XXX_summary` | `release/*` | Minor changes for a previous release; merged back to the release branch on update |
| `hotfix/JNG-XXX_summary` | `master` | Urgent fixes applied to both release and master branches |
| `master` | — | Latest released production sources |

> **Important:** There is no commit without a JIRA ticket number. Every commit and pull request must include `JNG-XXX`.

## Version Numbers

Versions follow semantic versioning with these rules:

| Event | Version Action |
|-------|---------------|
| Start a `feature/` branch | No version change |
| Start a `release/` branch from `develop` | Increment 2nd number on `develop` |
| Start a `bugfix/` branch | No version change (applied during release testing) |
| Start a `support/` branch | Increment 3rd number |
| Start a `hotfix/` branch | Increment 4th number |

## GitHub Actions Workflows

### build.yml — Main Build Pipeline

This is the primary workflow, triggered on pushes to `develop` and pull requests targeting `develop`, `master`, `increment/*`, or `release/*`.

```mermaid
flowchart TD
    TRIGGER["Push on develop<br/>or PR on develop / master /<br/>increment/* / release/*"]
    TRIGGER --> CHECK{Branch type?}
    CHECK -->|master, release/*| RELEASE_VER["Set version from pom.xml<br/>(without -SNAPSHOT)"]
    CHECK -->|develop, increment/*| DEV_VER["Set version:<br/>major.minor.qualifier.date_commitId_branch"]
    RELEASE_VER --> BUILD["Build and deploy<br/>to Nexus"]
    DEV_VER --> BUILD
    BUILD --> TAG["Create git tag<br/>v&lt;version&gt;"]
    TAG --> BRANCH_CHECK{Branch type?}
    BRANCH_CHECK -->|increment/*, release/*| MERGE_TAG["Create tag<br/>merge-pr/&lt;version&gt;"]
    MERGE_TAG --> TRIGGER_MERGE["Trigger<br/>merge-pr-tagged.yml"]
    BRANCH_CHECK -->|develop| CHANGELOG["Build changelog"]
    CHANGELOG --> GH_RELEASE["Create GitHub release<br/>(prerelease)"]
```

### merge-pr-tagged.yml — PR Merge Automation

Triggered when a `merge-pr/*` tag is pushed. Routes the merge based on version format:

```mermaid
flowchart TD
    TRIGGER["Push on merge-pr/* tag"]
    TRIGGER --> EXTRACT["Extract version from tag"]
    EXTRACT --> FORMAT_CHECK{Version format?}
    FORMAT_CHECK -->|major.minor.qualifier| MERGE_MASTER["Merge PR to master"]
    MERGE_MASTER --> TRIGGER_RELEASE["Trigger<br/>create-release-on-master.yml"]
    FORMAT_CHECK -->|other| SQUASH_DEV["Squash PR to develop"]
    SQUASH_DEV --> TRIGGER_BUILD["Trigger build.yml"]
    TRIGGER_RELEASE --> CLEANUP["Delete merge-pr/&lt;version&gt; tag"]
    TRIGGER_BUILD --> CLEANUP
```

### create-release-on-master.yml — Production Release

Triggered on pushes to `master`. Creates the final GitHub release with a generated changelog.

```mermaid
flowchart TD
    TRIGGER["Push on master"]
    TRIGGER --> VERSION["Get version from tag"]
    VERSION --> CHANGELOG["Build changelog"]
    CHANGELOG --> RELEASE["Create GitHub release<br/>(latest)"]
```

### release.yml — Manual Release Trigger

Manually triggered with a version parameter. Creates pull requests for both the release and the next development version:

```mermaid
flowchart TD
    TRIGGER["Manual trigger<br/>with version (or 'auto')"]
    TRIGGER --> VERSION_CHECK{Version = 'auto'?}
    VERSION_CHECK -->|yes| FROM_POM["Read version from pom.xml<br/>(strip -SNAPSHOT)"]
    VERSION_CHECK -->|no| GIVEN["Use given version"]
    FROM_POM --> CALC["Calculate next version<br/>(qualifier + 1)"]
    GIVEN --> CALC
    CALC --> PR_MASTER["Create PR on master<br/>with release version"]
    CALC --> PR_DEVELOP["Create PR on develop<br/>with next version"]
    PR_MASTER --> BUILD1["Trigger build.yml"]
    PR_DEVELOP --> BUILD2["Trigger build.yml"]
```

### Other Workflows

| Workflow | Purpose |
|----------|---------|
| `build-dependabot.yml` | Separate build pipeline for Dependabot PRs |
| `bump-version.yml` | Automated version bumping |
| `delete-old-draft-releases.yml` | Cleanup stale draft releases |
| `jira-description-to-pr.yml` | Copies JIRA issue description into PR body |
| `sync-labels.yml` | Synchronizes GitHub labels |

## Overall CI/CD Flow

```mermaid
flowchart LR
    subgraph "Development"
        DEV[develop branch]
        FEAT[feature branches]
    end
    subgraph "Release"
        REL[release branches]
        MASTER[master branch]
    end
    subgraph "Artifacts"
        NEXUS[Nexus Repository]
        GH[GitHub Releases]
    end

    FEAT -->|merge| DEV
    DEV -->|build.yml| NEXUS
    DEV -->|prerelease| GH
    DEV -->|release.yml| REL
    REL -->|merge-pr-tagged| MASTER
    MASTER -->|create-release-on-master| GH
    REL -->|build.yml| NEXUS
```

## Development Workflow

For issue tracking, the project uses [JIRA](https://blackbelt.atlassian.net/jira/dashboards).

1. Pick a JIRA ticket (e.g. `JNG-1234`)
2. Create a feature branch: `feature/JNG-1234_short_description`
3. Develop and commit (every commit references the ticket)
4. Push and create a PR targeting `develop`
5. CI builds and deploys a snapshot to Nexus
6. On merge, a prerelease is created on GitHub

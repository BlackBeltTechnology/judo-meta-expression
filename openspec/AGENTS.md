# OpenSpec Agent Instructions

This document provides instructions for AI assistants working with OpenSpec in the judo-meta-expression project.

## Quick Reference

### Commands

```bash
# List all changes
openspec list

# List all specs
openspec list --specs

# Show a specific change
openspec show <change-id>

# Validate a change
openspec validate <change-id> --strict

# Apply a change (after approval)
openspec apply <change-id>
```

### Directory Structure

```
openspec/
├── AGENTS.md           # This file
├── project.md          # Project conventions
├── specs/              # Approved specifications
│   └── <capability>/
│       └── spec.md
└── changes/            # Change proposals
    └── <change-id>/
        ├── proposal.md
        ├── tasks.md
        ├── design.md   # Optional, for complex changes
        └── specs/
            └── <capability>/
                └── spec.md
```

## Creating Proposals

### 1. Choose a Change ID

Use verb-led, lowercase, hyphenated IDs:
- `add-zeta-validation`
- `fix-type-resolution`
- `update-binding-rules`

### 2. Create Proposal Files

Required files:
- `proposal.md` - Summary, motivation, scope, approach
- `tasks.md` - Ordered list of implementation tasks

Optional files:
- `design.md` - Architectural details for complex changes
- `specs/<capability>/spec.md` - Formal specification deltas

### 3. Proposal Structure

```markdown
# <Title>

## Summary
One paragraph describing the change.

## Motivation
Why this change is needed.

## Scope
### In Scope
- What will be done

### Out of Scope
- What will NOT be done

## Technical Approach
How the change will be implemented.

## Success Criteria
How to verify the change is complete.

## Risks and Mitigations
Potential issues and how to address them.
```

### 4. Tasks Structure

```markdown
# Tasks: <Title>

## Phase 1: <Phase Name>

### 1.1 <Task Group>
- [ ] Task 1
- [ ] Task 2

## Phase 2: <Phase Name>
...

## Dependencies
Which phases depend on others.

## Estimated Effort
| Phase | Tasks | Effort |
|-------|-------|--------|
| 1     | 5     | Small  |
```

## Validation Rules

1. All proposals must have `proposal.md` and `tasks.md`
2. Constraint names must use constants, not string literals
3. Test coverage required for all validation rules
4. Documentation must be updated for new features

## Project-Specific Guidelines

### Expression Model Validation

When adding validation rules:

1. Match EVL constraint names exactly
2. Use `ValidationConstants` for all names
3. Implement both constraint and critique patterns
4. Add parameterized tests (EVL + Java)

### Dependencies

Key dependencies for validation work:
- `judo-zeta-validation-core` - Core validation framework
- `judo-zeta-annotations` - Validation annotations
- `epsilon-runtime-execution` - EVL execution

### Testing Patterns

Follow the ESM testing pattern:
1. `AbstractExpressionValidationTest` base class
2. `ValidatorType` enum (EVL, JAVA)
3. `@ParameterizedTest` with `@EnumSource`
4. Identical assertions for both validators

## References

- [Judo Zeta Documentation](https://github.com/BlackBeltTechnology/judo-zeta/tree/develop/docs)
- [ESM Validation Implementation](../../judo-meta-esm/model/src/main/java/hu/blackbelt/judo/meta/esm/validation/)
- [Epsilon EVL Reference](https://eclipse.dev/epsilon/doc/evl/)

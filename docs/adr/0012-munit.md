# 0012 — MUnit as test framework

- Status: Accepted (2026-05)

## Context

The original codebase had no test framework — every "test" was an ad-hoc
`object … extends App` with a `main`. `sbt test` ran nothing.

Available Scala test frameworks: ScalaTest, MUnit, Specs2, Weaver, ZIO Test.

## Decision

Use **MUnit** (`org.scalameta::munit`) plus **`munit-cats-effect`** for
suites that need `IO`.

## Consequences

- Lightweight (single `FunSuite` style), fast, fewer DSLs to learn than
  ScalaTest.
- First-class Cats Effect support via `munit-cats-effect`.
- Familiar to anyone who's used JUnit / xUnit.
- Loses ScalaTest's matcher ecosystem — acceptable, MUnit's `assertEquals`
  + `intercept` cover ~95% of what we need.

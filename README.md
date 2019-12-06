# semanticversion

A Java library for parsing and comparing [semantic versions](https://semver.org/).

[![Actions Status](https://github.com/gravitation1/semanticversion/workflows/Java%20CI/badge.svg)](https://github.com/gravitation1/semanticversion/actions)

[Packages can be found on GitHub](https://github.com/gravitation1/semanticversion/packages/)

## Usage
    import io.github.gravitation1.SemanticVersion;

    ...

    SemanticVersion semanticVersionA = SemanticVersion.from("1.2.3");
    SemanticVersion semanticVersionB = SemanticVersion.from("9.2.3");

    if (semanticVersionA.compareTo(semanticVersionB) == 0) {
        // These versions are equal.
    } else if (semanticVersionA.compareTo(semanticVersionB) > 0) {
        // semanticVersionA is of higher precedence.
    } else {
        // semanticVersionB is of higher precedence.
    }

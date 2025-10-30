{
  description = "FRC Team 766 Robot Code 2025";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    flake-utils.url = "github:numtide/flake-utils";
  };

  outputs = { self, nixpkgs, flake-utils }:
    flake-utils.lib.eachDefaultSystem (system:
      let
        pkgs = import nixpkgs {
          inherit system;
          config.allowUnfree = false;  # Keep it pure, no proprietary stuff
        };

        # Pin Java version to 21 (LTS)
        jdk = pkgs.jdk21;

        # Gradle wrapper will handle Gradle itself, but we need Java
        buildInputs = [
          jdk
          pkgs.git  # Needed for gversion plugin
          pkgs.which  # Gradle sometimes needs this
          pkgs.ps  # For process management
        ];

        # Custom build scripts
        frc-build = pkgs.writeShellScriptBin "frc-build" ''
          exec ./gradlew build --no-daemon "$@"
        '';

        frc-format = pkgs.writeShellScriptBin "frc-format" ''
          exec ./gradlew spotlessApply --no-daemon "$@"
        '';

        frc-test = pkgs.writeShellScriptBin "frc-test" ''
          exec ./gradlew test --no-daemon "$@"
        '';

        frc-deploy = pkgs.writeShellScriptBin "frc-deploy" ''
          exec ./gradlew deploy --no-daemon "$@"
        '';

        frc-clean = pkgs.writeShellScriptBin "frc-clean" ''
          exec ./gradlew clean --no-daemon "$@"
        '';

        # Set up a pure shell environment
        shellHook = ''
          # Ensure we're using the Nix-provided Java
          export JAVA_HOME="${jdk}"
          export PATH="${jdk}/bin:$PATH"
          
          # Gradle configuration - disable daemon to avoid hanging
          export GRADLE_USER_HOME="$PWD/.gradle-nix"
          export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=-Xmx2g"
          
          # Create a local gradle.properties in the Nix gradle home to ensure daemon is off
          mkdir -p "$GRADLE_USER_HOME"
          cat > "$GRADLE_USER_HOME/gradle.properties" << 'NIXGRADLEPROPS'
org.gradle.daemon=false
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m
NIXGRADLEPROPS
          
          # Create a wrapper for gradlew that always uses --no-daemon
          alias gradlew='./gradlew --no-daemon'
          
          # Make Gradle use our Java
          export org_gradle_java_home="${jdk}"
          
          # Ensure temp directory exists and is writable
          export TMPDIR="''${TMPDIR:-/tmp}"
          mkdir -p "$TMPDIR"
          
          # Create gradle.properties if it doesn't exist
          if [ ! -f gradle.properties ]; then
            cat > gradle.properties << 'GRADLEPROPS'
# Disable daemon for Nix compatibility and reproducible builds
org.gradle.daemon=false

# JVM settings
org.gradle.jvmargs=-Xmx2g -XX:MaxMetaspaceSize=512m

# Enable parallel builds
org.gradle.parallel=true

# Configure caching
org.gradle.caching=true
GRADLEPROPS
            echo "Created gradle.properties with daemon disabled"
          else
            # Ensure daemon is disabled even if file exists
            if ! grep -q "org.gradle.daemon=false" gradle.properties 2>/dev/null; then
              echo "" >> gradle.properties
              echo "# Added by Nix flake for compatibility" >> gradle.properties
              echo "org.gradle.daemon=false" >> gradle.properties
              echo "Updated gradle.properties to disable daemon"
            fi
          fi
          
          # Update .gitignore if needed
          if ! grep -q ".gradle-nix/" .gitignore 2>/dev/null; then
            cat >> .gitignore << 'GITIGNORE'

# Nix-specific
.gradle-nix/
.direnv/
GITIGNORE
            echo "Updated .gitignore for Nix"
          fi
          
          echo "FRC Robot Development Environment"
          echo "=================================="
          echo "Java version: $(java -version 2>&1 | head -n 1)"
          echo "Gradle daemon: disabled (configured in gradle.properties)"
          echo ""
          echo "Custom commands available:"
          echo "  frc-build   - Build the project"
          echo "  frc-format  - Auto-fix code formatting"
          echo "  frc-test    - Run tests"
          echo "  frc-deploy  - Deploy to robot"
          echo "  frc-clean   - Clean build artifacts"
          echo ""
          echo "Or use ./gradlew directly for other tasks"
        '';

      in
      {
        # Development shell
        devShells.default = pkgs.mkShell {
          buildInputs = buildInputs ++ [
            frc-build
            frc-format
            frc-test
            frc-deploy
            frc-clean
          ];
          
          inherit shellHook;
          
          # Prevent system Java from interfering
          JAVA_HOME = "${jdk}";
          
          # Tell Gradle to use our Java explicitly
          org_gradle_java_home = "${jdk}";
          
          # Disable Gradle daemon by default
          GRADLE_OPTS = "-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=-Xmx2g";
        };

        # Build package - requires network access for Gradle dependencies
        packages.default = pkgs.stdenv.mkDerivation {
          pname = "frc-robot-2025";
          version = "2025.0.0";
          
          src = ./.;
          
          nativeBuildInputs = buildInputs;
          
          # Disable sandbox to allow network access for Gradle
          __noChroot = true;
          
          buildPhase = ''
            export JAVA_HOME="${jdk}"
            export GRADLE_USER_HOME="$TMPDIR/gradle"
            export GRADLE_OPTS="-Dorg.gradle.daemon=false -Dorg.gradle.jvmargs=-Xmx2g"
            
            # Run the build
            ./gradlew build -x test --stacktrace
          '';
          
          installPhase = ''
            mkdir -p $out/lib
            cp -r build/libs/*.jar $out/lib/
            
            # Create a wrapper script
            mkdir -p $out/bin
            cat > $out/bin/frc-robot << EOF
#!/bin/sh
exec ${jdk}/bin/java -jar $out/lib/2025-all.jar "\$@"
EOF
            chmod +x $out/bin/frc-robot
          '';
        };

        # Formatter for this flake itself
        formatter = pkgs.nixpkgs-fmt;
      }
    );
}

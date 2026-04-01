{
  description = "Booty - simplified MyExpenses fork build environment";

  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
  };

  outputs = { self, nixpkgs }:
    let
      system = "x86_64-linux";
      pkgs = import nixpkgs {
        inherit system;
        config = {
          android_sdk.accept_license = true;
          allowUnfree = true;
        };
      };

      androidComposition = pkgs.androidenv.composeAndroidPackages {
        platformVersions = [ "36" ];
        buildToolsVersions = [ "36.0.0" ];
        includeNDK = false;
        includeSources = false;
        includeSystemImages = false;
        includeEmulator = false;
      };

      androidSdk = androidComposition.androidsdk;

      fhs = pkgs.buildFHSEnv {
        name = "booty-build";
        targetPkgs = p: [
          androidSdk
          p.jdk21
          p.git
          p.curl
          p.jq
          p.bash
          p.coreutils
          p.gnugrep
          p.gnused
          p.findutils
          p.zlib
          p.ncurses5
          p.stdenv.cc.cc.lib
        ];
        profile = ''
          export ANDROID_HOME="${androidSdk}/libexec/android-sdk"
          export ANDROID_SDK_ROOT="${androidSdk}/libexec/android-sdk"
          export JAVA_HOME="${pkgs.jdk21}"
        '';
        runScript = "bash";
      };
    in
    {
      devShells.${system}.default = pkgs.mkShell {
        buildInputs = [ fhs ];
        shellHook = ''
          echo "Booty build environment ready."
          echo "Run 'booty-build' to enter the FHS env with Android SDK + JDK 21."
          echo "Or use 'booty-build -c \"./gradlew assembleExternRelease\"' to build directly."
        '';
      };

      packages.${system}.default = fhs;
    };
}

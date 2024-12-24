{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";
    android-nixpkgs.url = "github:HPRIOR/android-nixpkgs/d144e1aff31d45e92ee981e04d871b000fd791f9";
  };

  outputs = { self, nixpkgs, android-nixpkgs, ... }:
    let
      system = "x86_64-linux";

      pkgs = import nixpkgs {
        system = system;
      };

      android-sdk = (android-nixpkgs.sdk.${system} (sdkPkgs: with sdkPkgs; [
        cmdline-tools-latest
        platform-tools
        build-tools-35-0-0
        platforms-android-35
        build-tools-34-0-0
      ]));
    in
    {
      devShells."${system}".default =
        pkgs.mkShell {
          packages = with pkgs; [
            android-sdk
            libxcrypt-legacy
            libGL
            gtk3
            jdk17
          ];

          ANDROID_HOME = "${android-sdk}/share/android-sdk";
          GRADLE_OPTS = "-Dorg.gradle.project.android.aapt2FromMavenOverride=${android-sdk}/share/android-sdk/build-tools/35.0.0/aapt2";

          shellHook = ''
            # Add NIX_LDFLAGS to LD_LIBRARY_PATH
            lib_paths=($(echo $NIX_LDFLAGS | grep -oP '(?<=-rpath\s| -L)[^ ]+'))
            lib_paths_str=$(IFS=:; echo "''${lib_paths[*]}")
            export LD_LIBRARY_PATH="$lib_paths_str:$LD_LIBRARY_PATH"

            export XDG_DATA_DIRS="$XDG_DATA_DIRS:${pkgs.gtk3}/share/gsettings-schemas/gtk+3-3.24.43"

            export PROGUARD_JAVA_HOME=${pkgs.jdk17}/lib/openjdk
          '';
        };
    };
}

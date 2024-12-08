{
  inputs = {
    nixpkgs.url = "github:nixos/nixpkgs/nixpkgs-unstable";
  };

  outputs = { self, nixpkgs, ... }:
    let
      system = "x86_64-linux";

      pkgs = import nixpkgs {
        system = system;
      };
    in
    {
      devShells."${system}".default =
        let
          pkgs = import nixpkgs {
            system = system;
          };
        in
        pkgs.mkShell {
          packages = with pkgs; [
            libxcrypt-legacy
            libGL
            gtk3
            jdk17
          ];

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

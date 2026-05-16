{ pkgs, ... }:

{
  # Only install openapi-generator-cli
  packages = [ 
    pkgs.openapi-generator-cli 
  ];
}

#!/bin/bash
set -e

# Configuration
SCHEMA_URL="https://gitlab.com/gitlab-org/gitlab/-/raw/master/doc/api/openapi/openapi_v3.yaml?ref_type=heads"
SCHEMA_FILE="openapi.yaml"
PACKAGE_NAME="com.bieniucieniu.ballscraper.gitlab"

# Ensure we are in the module directory
cd "$(dirname "$0")"

echo "Downloading GitLab OpenAPI schema..."
curl -sL "$SCHEMA_URL" -o "$SCHEMA_FILE"

echo "Generating client using openapi-generator-cli..."
openapi-generator-cli generate \
    -i "$SCHEMA_FILE" \
    -g kotlin \
    --library ktor \
    -o ./api/ \
    --additional-properties=serializationLibrary=kotlinx_serialization,packageName=$PACKAGE_NAME,enumPropertyNaming=original \
    --skip-validate-spec


echo "Generation complete."

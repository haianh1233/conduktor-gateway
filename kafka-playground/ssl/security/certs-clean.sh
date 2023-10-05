#!/bin/bash

# Reference: https://github.com/confluentinc/cp-demo/blob/7.5.0-post/scripts/security

set -o nounset \
    -o errexit
#    -o verbose
#    -o xtrace

# Cleanup files
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null && pwd )"
(cd "${DIR}" && rm -f *.crt *.csr *_creds *.jks *.srl *.key *.pem *.der *.p12)
(cd "${DIR}" && rm -fr keypair/)
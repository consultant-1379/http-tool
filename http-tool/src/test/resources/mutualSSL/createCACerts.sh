#!/bin/sh
openssl genrsa -out ${1}.key 2048
openssl req -new -key ${1}.key -out ${1}.csr
openssl x509 -req -days 3652 -in ${1}.csr -signkey ${1}.key -out ${1}.crt -extensions v3_ca -extfile /opt/bitnami/common/openssl/openssl.cnf
openssl pkcs12 -export -in ${1}.crt -inkey ${1}.key -out ${1}.p12 -name ${1}


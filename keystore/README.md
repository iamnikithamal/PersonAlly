# Keystore for PersonAlly

This directory should contain the keystore file `personally.jks` for signing release APKs.

## Generation

The keystore will be automatically generated during the first CI build using the following command:

```bash
keytool -genkeypair -v \
  -keystore personally.jks \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000 \
  -alias personally \
  -storepass personally2024 \
  -keypass personally2024 \
  -dname "CN=PersonAlly,OU=Development,O=PersonAlly,L=Unknown,ST=Unknown,C=US"
```

## Credentials

- Store Password: `personally2024`
- Key Alias: `personally`
- Key Password: `personally2024`

**Note:** This is an open-source project and these credentials are publicly known.

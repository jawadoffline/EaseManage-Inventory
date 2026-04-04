# SSL Certificate Setup

For production, generate a keystore:

```bash
keytool -genkeypair -alias easemanage -keyalg RSA -keysize 2048 \
  -storetype PKCS12 -keystore keystore.p12 -validity 365 \
  -storepass changeit -dname "CN=easemanage.com"
```

Then set these environment variables:
- SERVER_SSL_ENABLED=true
- SERVER_SSL_KEY_STORE=classpath:ssl/keystore.p12
- SERVER_SSL_KEY_STORE_PASSWORD=changeit
- SERVER_SSL_KEY_ALIAS=easemanage

Or add to application-prod.yml:
```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:ssl/keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD:changeit}
    key-alias: easemanage
```

https://learn.hashicorp.com/vault/?track=identity-access-management#getting-started

```
export VAULT_ADDR='http://0.0.0.0:8200'
brew install vault
docker-compose up -d
vault status
vault login
vault secrets list
```

https://learn.hashicorp.com/vault/developer/sm-dynamic-secrets
https://www.vaultproject.io/docs/secrets/databases/mysql-maria.html

```
vault secrets enable database

vault write database/config/my-mysql-database     plugin_name=mysql-database-plugin     connection_url="{{username}}:{{password}}@tcp(`docker inspect vault-playground_mysql_1 | jq -r '.[0].NetworkSettings.Networks["vault-playground_default"].IPAddress'`:3306)/"     allowed_roles="my-role"     username="root"     password="mysql"

vault write database/roles/my-role \
    db_name=my-mysql-database \
    creation_statements="CREATE USER '{{name}}'@'%' IDENTIFIED BY '{{password}}';GRANT SELECT ON *.* TO '{{name}}'@'%';" \
    default_ttl="60s" \
    max_ttl="300s"

```

N.B. default TTL is 60 seconds so we rapidly rotate creds


# app roles
https://learn.hashicorp.com/vault/identity-access-management/iam-authentication

```
 vault auth enable approle

cat << EOF > jenkins-pol.hcl
# Login with AppRole
path "auth/approle/login" {
  capabilities = [ "create", "read" ]
}

# Read mysql dynamic creds
path "database/creds/my-role" {
  capabilities = [ "read" ]
}
EOF

vault policy write jenkins jenkins-pol.hcl

vault write auth/approle/role/jenkins policies="jenkins"

vault read auth/approle/role/jenkins

# copy role_id to java program
vault read auth/approle/role/jenkins/role-id

# copy secret_id to java program
vault write -f auth/approle/role/jenkins/secret-id
```

# borrow some test data for the docker mysql container from: http://zetcode.com/db/jdbi/

```
cat << EOF > cars_mysql.sql
DROP TABLE IF EXISTS Cars;
CREATE TABLE Cars(Id INT PRIMARY KEY AUTO_INCREMENT, 
                  Name TEXT, Price INT) ENGINE=InnoDB;
                  
INSERT INTO Cars(Name, Price) VALUES('Audi', 52642);
INSERT INTO Cars(Name, Price) VALUES('Mercedes', 57127);
INSERT INTO Cars(Name, Price) VALUES('Skoda', 9000);
INSERT INTO Cars(Name, Price) VALUES('Volvo', 29000);
INSERT INTO Cars(Name, Price) VALUES('Bentley', 350000);
INSERT INTO Cars(Name, Price) VALUES('Citroen', 21000);
INSERT INTO Cars(Name, Price) VALUES('Hummer', 41400);
INSERT INTO Cars(Name, Price) VALUES('Volkswagen', 21600);
EOF
```

# watch vault making new users..
```
while true; do mysql --skip-column-names -u root -h 127.0.0.1 -pmysql -f < <(echo "select user from mysql.user;") | grep my-role; sleep 1; done
```
